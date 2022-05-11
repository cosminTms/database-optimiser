package sjdb;

import java.util.*;
import java.util.stream.Collectors;

public class Optimiser implements PlanVisitor{
    private Catalogue catalogue;

    // set of all Predicates used in the WHERE clause of the query,
    // all these must be satisfied in the final optimised query plan
    private Set<Predicate> predicatesToSatisfy;

    // set of all Attributes used in PROJECTIONS and SELECTIONS operators,
    // all these need to be used somewhere in the final optimised query plan
    private Set<Attribute> requiredAttributes;

    // set of all relations in the FROM clause of the query,
    // each element will represent a leaf in the optimised query plan
    private Set<Scan> queriedRelations;

    // use an Estimator to create output Relations for the newly created operators
    // in the final optimised query plan
    private Estimator estimator;

    private Operator initialPlan;


    /**
     * Construct an instance of Optimiser by passing a Catalogue object as parameter
     *
     * @param catalogue
     */
    public Optimiser(Catalogue catalogue){
        this.catalogue = catalogue;
        estimator = new Estimator();
    }

    @Override
    /**
     *  When visiting a Scan operator, add a fresh new Scan operator to the set of relations
     */
    public void visit(Scan op) {
        NamedRelation relation = (NamedRelation) op.getRelation();
        queriedRelations.add(new Scan(relation));
    }

    @Override
    /**
     *  When visiting a Project operator, add all attributes to be projected to the set of requiredAttributes
     *
     */
    public void visit(Project op) {
        List<Attribute> attributesUsed = op.getAttributes();
        requiredAttributes.addAll(attributesUsed);
    }

    @Override
    /**
     * When visiting a Select operator, add its predicate to the set of predicates to be satisfied.
     * This set of predicates would be iterated over and predicates removed from it until all predicates
     * would appear (and thus be satisfied) in the final optimised query plan.
     *
     * Additionally, the attributes used by the predicate would need to be added to the set of required attributes.
     * In this fashion, only necessary attributes are kept and the load of unnecessary attributes
     * would be lifted from the memory buffers.
     */
    public void visit(Select op) {
        Predicate predicate = op.getPredicate();
        predicatesToSatisfy.add(predicate);

        if (predicate.equalsValue()){
            // if predicate is of the form atr=val
            requiredAttributes.add(predicate.getLeftAttribute());

        } else{
            // if predicate is of the form atr=atr
            requiredAttributes.add(predicate.getLeftAttribute());
            requiredAttributes.add(predicate.getRightAttribute());
        }
    }

    @Override
    public void visit(Product op) {
    }

    @Override
    public void visit(Join op) {
    }

    public Operator optimise(Operator plan){
        // initialise sets as new for a new optimisation plan
        predicatesToSatisfy = new HashSet<>();
        requiredAttributes = new HashSet<>();
        queriedRelations = new HashSet<>();
        initialPlan = plan;

        // make this Optimiser object visit each node in the initial canonical query plan
        // in a depth-first traversal, parse the canonical query plan by taking from
        // Select operators -> Predicate used + Attributes used
        // Project operators -> Attributes used
        // Scan operators -> Relations used
        plan.accept(this);

        List<Operator> leafOperations = createLeafBlocks();

        // try all possible ways of ordering the remaining predicates to be satisfied
        // in this way, choose the ordering with the least total cost of processed tuples
        List<Predicate> remainingPredicates = new ArrayList<>();
        remainingPredicates.addAll(predicatesToSatisfy);

        List<List<Predicate>> orderingsOfRemainingPredicates = createPredicateOrderings(remainingPredicates);

        // given the above predicate ordering,
        // determine which order is the most efficient in terms of overall processed tuples
        Operator optimisedPlan = createOptimalQueryPlan(leafOperations, orderingsOfRemainingPredicates);

        return optimisedPlan;
    }


    /**
     * For each relation used in the initial query plan (appeared in the FROM clause):
     * build on top of it a chain of Select operations, that satisfy some predicates involving this relation only (if exist).
     * Additionally, pass this chain of operations to a Project operator that makes use of some required attributes
     * involving this relation only
     *
     * The Operators in the returned list would behave as "leaf" blocks of nodes in the optimised plan.
     * They would need to join or multiply with each other in order to satisfy the remaining unsatisfied yet predicates.
     *
     * @return list of operations of the form: Project( Select( Scan(Relation(R)), Predicate()), [requiredAttributes])
     */
    public List<Operator> createLeafBlocks() {
        List<Operator> leafBlocks = new ArrayList<>();

        for (Scan relation : queriedRelations) {
            Operator leafBlock = relation;

            // Construct chain of Select operators with predicates involving single relations
            Iterator<Predicate> iter = predicatesToSatisfy.iterator();
            while (iter.hasNext()){
                Predicate predicate = iter.next();

                // update output relation for the current operator
                if (leafBlock.getOutput() == null) {
                    leafBlock.accept(estimator);
                }

                if (predicate.equalsValue()) {
                    // atr = val
                    if (leafBlock.getOutput().getAttributes().contains(predicate.getLeftAttribute())){
                        leafBlock = new Select(leafBlock, predicate);
                        iter.remove();
                    }
                } else{
                    // atr = atr
                    if (leafBlock.getOutput().getAttributes().contains(predicate.getLeftAttribute()) &&
                            leafBlock.getOutput().getAttributes().contains(predicate.getRightAttribute())){
                        leafBlock = new Select(leafBlock, predicate);
                        iter.remove();
                    }
                }
            }

            // Construct final Project operator for the chain of selects above,
            // it would include only necessary attributes, thus eliminating redundant memory load
            updateAttributes();

            // udate output relation before processing the operator
            if (leafBlock.getOutput() == null){
                leafBlock.accept(estimator);
            }

            List<Attribute> attributesFromRelation = leafBlock.getOutput().getAttributes();
            List<Attribute> finalAttributesRemainig = new ArrayList<>();

            for (Attribute attribute : requiredAttributes){
                for (Attribute atrInRelation : attributesFromRelation){
                    if (attribute.getName().equals(atrInRelation.getName())){
                        finalAttributesRemainig.add(atrInRelation);
                    }
                }
            }

            // if there is any attribute for projecting the Select/Scan operations
            if (finalAttributesRemainig.size() > 0){
                leafBlock = new Project(leafBlock, finalAttributesRemainig);
            }

            if (leafBlock.getOutput() == null){
                leafBlock.accept(estimator);
            }

            leafBlocks.add(leafBlock);
        }

        return  leafBlocks;
    }

    public void updateAttributes(){
        Set<Attribute> attributesStillRequired = new HashSet<>();

        if (initialPlan instanceof Project) {
            attributesStillRequired.addAll(((Project) initialPlan).getAttributes());
        }

        for (Predicate predicate : predicatesToSatisfy) {
            if (predicate.equalsValue()) {
                attributesStillRequired.add(predicate.getLeftAttribute());
            } else{
                attributesStillRequired.add(predicate.getLeftAttribute());
                attributesStillRequired.add(predicate.getRightAttribute());
            }
        }

        requiredAttributes =  attributesStillRequired;
    }

    public List<List<Predicate>> createPredicateOrderings(List<Predicate> remainingPredicates){

        if (remainingPredicates.isEmpty()){
            List<List<Predicate>> res = new ArrayList<>();
            res.add(new ArrayList<>());
            return res;
        }

        Predicate predicate = remainingPredicates.remove(0);
        List<List<Predicate>> result = new ArrayList<>();
        List<List<Predicate>> orderings = createPredicateOrderings(remainingPredicates);

        for (List<Predicate> order : orderings){
            for (int i=0; i<=order.size(); i++){
                List<Predicate> x = new ArrayList<>(order);
                x.add(i, predicate);
                result.add(x);
            }
        }

        return result;
    }

    public Operator createOptimalQueryPlan(List<Operator> leafBlocks, List<List<Predicate>> predicateOrderings){
        int minCost = Integer.MAX_VALUE;
        Operator optimalPlan = null;

        for (List<Predicate> order : predicateOrderings){
            Operator potentialPlan = createPotentialOptimalPlan(leafBlocks, order);

            int currentCost = estimator.totalCost(potentialPlan);

            if (currentCost < minCost){
                minCost = currentCost;
                optimalPlan = potentialPlan;
            }
        }

        return optimalPlan;
    }

    public Operator createPotentialOptimalPlan(List<Operator> leafBlocks, List<Predicate> predicateOrder){
        if (leafBlocks.size() == 1){
            Operator plan = leafBlocks.get(0);
            if (plan.getOutput() == null){
                plan.accept(estimator);
            }
            return plan;
        }

        Operator potentialPlan = null;

        Iterator<Predicate> iter = predicateOrder.iterator();
        while (iter.hasNext()){
            Predicate predicate = iter.next();

            Attribute leftAttribute = predicate.getLeftAttribute();
            Attribute rightAttribute = predicate.getRightAttribute();

            Operator leftOperator = getBlockCorrespondingToAttribute(leafBlocks, leftAttribute);
            Operator rightOperator = getBlockCorrespondingToAttribute(leafBlocks, rightAttribute);

            if (leftOperator != null && rightOperator == null){
                potentialPlan = new Select(leftOperator, predicate);
                leafBlocks.remove(leftOperator);
                iter.remove();
            } else if (leftOperator == null && rightOperator != null){
                potentialPlan = new Select(rightOperator, predicate);
                leafBlocks.remove(rightOperator);
                iter.remove();
            }

            if (leftOperator != null && rightOperator != null) {
                potentialPlan = new Join(leftOperator, rightOperator, predicate);
                leafBlocks.remove(leftOperator);
                leafBlocks.remove(rightOperator);
                iter.remove();
            }

            if (potentialPlan.getOutput() == null){
                potentialPlan.accept(estimator);
            }

            Set<Attribute> attributesStillRequired = getRemainingAttributes(predicateOrder);

            if (attributesStillRequired.size() == potentialPlan.getOutput().getAttributes().size() &&
                    potentialPlan.getOutput().getAttributes().containsAll(attributesStillRequired)){
                leafBlocks.add(potentialPlan);
            } else{
                List<Attribute> necessaryAttributes = potentialPlan.getOutput().getAttributes()
                        .stream()
                        .filter(attribute -> attributesStillRequired.contains(attribute))
                        .collect(Collectors.toList());

                if (necessaryAttributes.size() == 0){
                    leafBlocks.add(potentialPlan);
                } else{
                    potentialPlan = new Project(potentialPlan, necessaryAttributes);
                    potentialPlan.accept(estimator);
                    leafBlocks.add(potentialPlan);
                }
            }

        }

        // while there are multiple unjoined block nodes and every predicate has been satisfied
        // join the node blocks together via cartesian product
        while (leafBlocks.size() > 1){
            Operator blockNode1 = leafBlocks.get(0);
            Operator blockNode2 = leafBlocks.get(1);
            Operator product = new Product(blockNode1, blockNode2);
            product.accept(estimator);

            leafBlocks.remove(blockNode1);
            leafBlocks.remove(blockNode2);
            leafBlocks.add(product);
        }

        return leafBlocks.get(0);
    }

    public Set<Attribute> getRemainingAttributes(List<Predicate> predicatesToBeSatisfied){
        Set<Attribute> attributesStillRequired = new HashSet<>();

        if (initialPlan instanceof Project) {
            attributesStillRequired.addAll(((Project) initialPlan).getAttributes());
        }

        for (Predicate predicate : predicatesToBeSatisfied) {
            if (predicate.equalsValue()) {
                attributesStillRequired.add(predicate.getLeftAttribute());
            } else{
                attributesStillRequired.add(predicate.getLeftAttribute());
                attributesStillRequired.add(predicate.getRightAttribute());
            }
        }

        return attributesStillRequired;
    }

    public Operator getBlockCorrespondingToAttribute(List<Operator> operationBlocks, Attribute attribute){
        for (Operator operator : operationBlocks){
            if (operator.getOutput() == null){
                operator.accept(estimator);
            }
            if (operator.getOutput().getAttributes().contains(attribute)){
                return operator;
            }
        }

        return null;
    }

}

package sjdb;

import java.util.*;

public class Estimator implements PlanVisitor {

	// total cost of processing a query expressed in the overall number of tuples
	private int cost;


	public Estimator() {
		// empty constructor
	}


	public int totalCost(Operator plan){
		cost = 0;
		plan.accept(this);
		return cost;
	}

	/* 
	 * Create output relation on Scan operator
	 *
	 * Example implementation of visit method for Scan operators.
	 */
	public void visit(Scan op) {
		Relation input = op.getRelation();
		Relation output = new Relation(input.getTupleCount());
		
		Iterator<Attribute> iter = input.getAttributes().iterator();
		while (iter.hasNext()) {
			output.addAttribute(new Attribute(iter.next()));
		}
		
		op.setOutput(output);
		cost += output.getTupleCount();
	}

	/**
	 * Create output Relation on Project(ion) operator
	 *
	 * Maintain the same tuple count as in the input relation
	 *
	 * Keep only the attribute objects from the input relation
	 * that have the same name as in the projected attribute set
	 *
	 * @param op Project operator to be visited
	 */
	public void visit(Project op) {
		Relation input = op.getInput().getOutput();
		Relation output = new Relation(input.getTupleCount());

		for (Attribute atr : op.getAttributes()){
			for (Attribute inputAtr : input.getAttributes()){
				if (atr.getName().equals(inputAtr.getName())){
					output.addAttribute(inputAtr);
				}
			}
		}

		op.setOutput(output);
		cost += output.getTupleCount();
	}
	
	public void visit(Select op) {
		Relation input = op.getInput().getOutput();
		Predicate predicate = op.getPredicate();
		Relation output;

		/**
		 *  if predicate is of the form atr=val
		 */
		if (predicate.equalsValue()){
			String leftAtrName = predicate.getLeftAttribute().getName();
			int valueCount = -1;

			List<Attribute> attributes = new ArrayList<>();

			Iterator<Attribute> iter = input.getAttributes().iterator();
			while (iter.hasNext()) {
				Attribute currentAtr = iter.next();
				if (currentAtr.getName().equals(leftAtrName)) {
					Attribute selectedAttribute = new Attribute(leftAtrName, 1);
					attributes.add(selectedAttribute);
					valueCount = currentAtr.getValueCount();
				} else{
					attributes.add(currentAtr);
				}
			}

			double probability;

			if (valueCount != -1) {
				probability = 1.0 / valueCount;
			} else {
				probability = 1;
			}

			int resultingTuples = (int) Math.ceil(probability * input.getTupleCount());
			output = new Relation(resultingTuples);
			for (Attribute atr : attributes) {
				if (atr.getValueCount() > resultingTuples){
					output.addAttribute(new Attribute(atr.getName(), resultingTuples));
				} else {
					output.addAttribute(atr);
				}
			}
		} else{
			/**
			 * For cases when atr=atr
			 */
			String leftAtrName = predicate.getLeftAttribute().getName();
			String rightAtrName = predicate.getRightAttribute().getName();

			int leftValueCount = -1;
			int rightValueCount = -1;

			List<Attribute> attributes = new ArrayList<>();

			Iterator<Attribute> iter = input.getAttributes().iterator();
			while (iter.hasNext()) {
				Attribute currentAtr = iter.next();
				if (currentAtr.getName().equals(leftAtrName)) {
					leftValueCount = currentAtr.getValueCount();
				} else if (currentAtr.getName().equals(rightAtrName)) {
					rightValueCount = currentAtr.getValueCount();
				}

				attributes.add(currentAtr);
			}

			double probability;

			if (leftValueCount == -1 || rightValueCount == -1){
				probability = 1;
			} else {
				probability = 1.0 / Math.max(leftValueCount, rightValueCount);
			}

			int resultingTuples = (int) Math.ceil(probability * input.getTupleCount());
			output = new Relation(resultingTuples);
			for (Attribute atr : attributes){
				if (atr.getName().equals(leftAtrName)){
					output.addAttribute(new Attribute(leftAtrName, Math.min(leftValueCount, rightValueCount)));
				} else if (atr.getName().equals(rightAtrName)){
					output.addAttribute(new Attribute(rightAtrName, Math.min(leftValueCount, rightValueCount)));
				} else{
					if (atr.getValueCount() > resultingTuples){
						output.addAttribute(new Attribute(atr.getName(), resultingTuples));
					} else{
						output.addAttribute(atr);
					}
				}
			}
		}

		op.setOutput(output);
		cost += output.getTupleCount();
	}
	
	public void visit(Product op) {
		Relation leftInput = op.getLeft().getOutput();
		Relation rightInput = op.getRight().getOutput();

		Relation output = new Relation(leftInput.getTupleCount() * rightInput.getTupleCount());

		Iterator<Attribute> leftIter = leftInput.getAttributes().iterator();
		while (leftIter.hasNext()) {
			Attribute currentAtr = leftIter.next();
			output.addAttribute(currentAtr);
		}

		Iterator<Attribute> RightIter = rightInput.getAttributes().iterator();
		while (RightIter.hasNext()) {
			Attribute currentAtr = RightIter.next();
			output.addAttribute(currentAtr);
		}

		op.setOutput(output);
		cost += output.getTupleCount();
	}
	
	public void visit(Join op) {
		Relation leftInput = op.getLeft().getOutput();
		Relation rightInput = op.getRight().getOutput();

		Predicate predicate = op.getPredicate();

		Attribute leftAtr = predicate.getLeftAttribute();
		Attribute rightAtr = predicate.getRightAttribute();

		int leftAtrCount = -1;
		int rightAtrCount = -1;
		List<Attribute> attributes = new ArrayList<>();

		for (Attribute atr : leftInput.getAttributes()){
			if (atr.getName().equals(leftAtr.getName())){
				leftAtrCount = atr.getValueCount();
			}
			attributes.add(atr);
		}

		for (Attribute atr : rightInput.getAttributes()){
			if (atr.getName().equals(rightAtr.getName())){
				rightAtrCount = atr.getValueCount();
			}
			attributes.add(atr);
		}

		double probability = 1.0 / Math.max(leftAtrCount, rightAtrCount);
		int resultingTuples = (int) Math.ceil(probability * leftInput.getTupleCount() * rightInput.getTupleCount());
		Relation output = new Relation((resultingTuples));

		for (Attribute atr : attributes){
			if (atr.getName().equals(leftAtr.getName())){
				output.addAttribute(new Attribute(atr.getName(), Math.min(leftAtrCount, rightAtrCount)));
			} else if (atr.getName().equals(rightAtr.getName())){
				output.addAttribute(new Attribute(atr.getName(), Math.min(leftAtrCount, rightAtrCount)));
			} else{
				output.addAttribute(atr);
			}
		}

		op.setOutput(output);
		cost += output.getTupleCount();
	}
}

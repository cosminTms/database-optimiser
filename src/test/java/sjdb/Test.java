package sjdb;

import java.util.ArrayList;

import com.queryOptimiser.sjdb.Estimator;
import com.queryOptimiser.sjdb.Inspector;
import com.queryOptimiser.sjdb.Optimiser;
import com.queryOptimiser.sjdb.catalogue.Catalogue;
import com.queryOptimiser.sjdb.model.Attribute;
import com.queryOptimiser.sjdb.operators.binary.Predicate;
import com.queryOptimiser.sjdb.operators.Operator;
import com.queryOptimiser.sjdb.operators.binary.Product;
import com.queryOptimiser.sjdb.operators.unary.Project;
import com.queryOptimiser.sjdb.operators.Scan;
import com.queryOptimiser.sjdb.operators.unary.Select;

public class Test {
	private Catalogue catalogue;
	
	public Test() {
	}

	public static void main(String[] args) throws Exception {
		Catalogue catalogue = createCatalogue();
		Inspector inspector = new Inspector();
		Estimator estimator = new Estimator();

		Operator plan = query(catalogue);
		plan.accept(estimator);
		plan.accept(inspector);

		Optimiser optimiser = new Optimiser(catalogue);
		Operator planopt = optimiser.optimise(plan);
		System.out.println("========OPTIMAL PLAN=========");
		planopt.accept(estimator);
		planopt.accept(inspector);
		System.out.println();
	}
	
	public static Catalogue createCatalogue() {
		Catalogue cat = new Catalogue();
		cat.createRelation("A", 100);
		cat.createAttribute("A", "a1", 100);
		cat.createAttribute("A", "a2", 15);
		cat.createRelation("B", 150);
		cat.createAttribute("B", "b1", 150);
		cat.createAttribute("B", "b2", 100);
		cat.createAttribute("B", "b3", 5);
		
		return cat;
	}

	public static Operator query(Catalogue cat) throws Exception {
		Scan a = new Scan(cat.getRelation("A"));
		Scan b = new Scan(cat.getRelation("B")); 
		
		Product p1 = new Product(a, b);
		
		Select s1 = new Select(p1, new Predicate(new Attribute("a2"), new Attribute("b3")));
		
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		atts.add(new Attribute("b1"));
		atts.add(new Attribute("a1"));


//		Join j1 = new Join(a,b,new Predicate(new Attribute("a2"), new String("val")));
		
		Project plan = new Project(s1, atts);
		
		return plan;
	}
	
}


package sjdb;
import java.io.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @author nmg
 *
 */
public class SJDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// read serialised catalogue from file and parse
		String catFile = "D:\\University of Southampton\\3rd Year\\Second Term\\COMP3211 - Advanced Databases\\sjdb\\data\\catLecture.txt";
//		String catFile = "D:\\University of Southampton\\3rd Year\\Second Term\\COMP3211 - Advanced Databases\\sjdb\\data\\cat.txt";
//		String catFile = args[0];
		Catalogue cat = new Catalogue();
		CatalogueParser catParser = new CatalogueParser(catFile, cat);
		catParser.parse();
		
		// read stdin, parse, and build canonical query plan
		QueryParser queryParser = new QueryParser(cat, new InputStreamReader(System.in));
		Operator plan = queryParser.parse();

		Inspector inspector = new Inspector();
				
		// create estimator visitor and apply it to canonical plan
		Estimator est = new Estimator();
		plan.accept(est);
		int canonicalCost = est.totalCost(plan);
		System.out.println("=======CANONICAL QUERY PLAN COST= " + canonicalCost + " tuples processed========");
		plan.accept(inspector);

		// create optimised plan
		Optimiser opt = new Optimiser(cat);
		Operator optPlan = opt.optimise(plan);
		int optimalCost = est.totalCost(optPlan);
		System.out.println("========OPTIMAL PLAN=========");
		System.out.println("=======OPTIMAL QUERY PLAN COST= " + optimalCost + " ("  + new DecimalFormat("0.00").format(optimalCost * 1.0/canonicalCost*100)  +"%) tuples processed========");
		optPlan.accept(est);
		optPlan.accept(inspector);
		System.out.println();
	}

}

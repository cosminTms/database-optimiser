/**
 * 
 */
package sjdb.operators.unary;

import sjdb.PlanVisitor;
import sjdb.operators.binary.Predicate;
import sjdb.operators.Operator;
import sjdb.operators.unary.UnaryOperator;

/**
 * Select * FROM input WHERE predicate
 *
 * @author nmg
 *
 */
public class Select extends UnaryOperator {
	private Predicate predicate;
	
	/**
	 * @param input
	 */
	public Select(Operator input, Predicate predicate) {
		super(input);
		this.predicate = predicate;
	}

	public Predicate getPredicate() {
		return this.predicate;
	}
	
	public void accept(PlanVisitor visitor) {
		super.accept(visitor);
		visitor.visit(this);
	}
	
	public String toString() {
		return "SELECT [" + this.predicate.toString() + "] (" + getInput().toString() + ")";
	}
}

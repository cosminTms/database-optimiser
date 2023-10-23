/**
 * 
 */
package com.queryOptimiser.sjdb.operators.unary;

import com.queryOptimiser.sjdb.PlanVisitor;
import com.queryOptimiser.sjdb.operators.binary.Predicate;
import com.queryOptimiser.sjdb.operators.Operator;

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

/**
 * 
 */
package com.queryOptimiser.sjdb.operators.unary;

import com.queryOptimiser.sjdb.PlanVisitor;
import com.queryOptimiser.sjdb.operators.Operator;

/**
 * @author nmg
 *
 */
public abstract class UnaryOperator extends Operator {
	/**
	 * 
	 */
	public UnaryOperator(Operator input) {
		super();	
		this.inputs.add(input);
		this.output = null;
	}

	/**
	 * Return the single child operator of this operator
	 * @return Child operator
	 */
	public Operator getInput() {
		return this.inputs.get(0);
	}

	/**
	 * @see Operator#accept(sjdb.OperatorVisitor)
	 */
	public void accept(PlanVisitor visitor) {
		super.accept(visitor);
		// depth-first traversal - accept the 
	}
}

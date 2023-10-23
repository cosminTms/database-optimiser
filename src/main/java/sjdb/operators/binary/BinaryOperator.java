package sjdb.operators.binary;

import sjdb.PlanVisitor;
import sjdb.model.Relation;
import sjdb.operators.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class represents a binary operator, and is
 * subclassed by Product and Join
 * @author nmg
 *
 */
public abstract class BinaryOperator extends Operator {
	/**
	 * Create a new binary operator
	 */
	public BinaryOperator(Operator left, Operator right) {
		super();
		this.inputs.add(left);
		this.inputs.add(right);
	}

	/**
	 * Return the left child below this operator in the query plan
	 * @return Left child
	 */
	public Operator getLeft() {
		return this.inputs.get(0);
	}

	/**
	 * Return the right child below this operator in the query plan
	 * @return Right child
	 */
	public Operator getRight() {
		return this.inputs.get(1);
	}

	/**
	 * @see sjdb.operators.Operator#getInputs()
	 */
	@Override
	public List<Operator> getInputs() {
		return this.inputs;
	}

	/**
	 * @see sjdb.operators.Operator#getOutput()
	 */
	@Override
	public Relation getOutput() {
		return this.output;
	}
	
	/**
	 * @see sjdb.operators.Operator#accept(sjdb.OperatorVisitor)
	 */
	public void accept(PlanVisitor visitor) {
		super.accept(visitor);
	}
}

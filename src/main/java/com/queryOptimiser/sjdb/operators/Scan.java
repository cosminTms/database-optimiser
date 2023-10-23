package com.queryOptimiser.sjdb.operators;

import com.queryOptimiser.sjdb.PlanVisitor;
import com.queryOptimiser.sjdb.model.NamedRelation;
import com.queryOptimiser.sjdb.model.Attribute;
import com.queryOptimiser.sjdb.model.Relation;

import java.util.List;
import java.util.Iterator;

/**
 * This class implements a Scan operator that feeds a NamedRelation into
 * a query plan.
 * @author nmg
 */
public class Scan extends Operator {
	/**
	 * The named relation to be scanned
	 */
	private NamedRelation relation;
	
	/**
	 * Create a new scan of a given named relation
	 * @param relation Named relation to be scanned
	 */
	public Scan(NamedRelation relation) {
		this.relation = relation;
		this.output = new Relation(relation.getTupleCount());
		Iterator<Attribute> iter = relation.getAttributes().iterator();
		
		while (iter.hasNext()) {
			this.output.addAttribute(new Attribute(iter.next()));
		}
	}

	/**
	 * @see Operator#getInputs()
	 */
	@Override
	public List<Operator> getInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return the named relation to be scanned
	 * @return Named relation to be scanned
	 */
	public Relation getRelation() {
		return this.relation;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.relation.toString();
	}
	
	/**
	 * @see Operator#accept(sjdb.OperatorVisitor)
	 */
	public void accept(PlanVisitor visitor) {
		visitor.visit(this);
	}
}

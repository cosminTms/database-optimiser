package com.queryOptimiser.sjdb;

import com.queryOptimiser.sjdb.operators.Scan;
import com.queryOptimiser.sjdb.operators.binary.Join;
import com.queryOptimiser.sjdb.operators.binary.Product;
import com.queryOptimiser.sjdb.operators.unary.Project;
import com.queryOptimiser.sjdb.operators.unary.Select;

public class Inspector implements PlanVisitor {

	public Inspector() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void visit(Scan op) {
		// TODO Auto-generated method stub
		System.out.println(op.toString());
		System.out.println("  in:  " + op.getRelation().render());
		System.out.println("  out: " + op.getOutput().render());
	}

	@Override
	public void visit(Project op) {
		// TODO Auto-generated method stub
		System.out.println(op.toString());
		System.out.println("  in:  " + op.getInput().getOutput().render());
		System.out.println("  out: " + op.getOutput().render());
	}

	@Override
	public void visit(Select op) {
		// TODO Auto-generated method stub
		System.out.println(op.toString());
		System.out.println("  in:  " + op.getInput().getOutput().render());
		System.out.println("  out: " + op.getOutput().render());
	}

	@Override
	public void visit(Product op) {
		// TODO Auto-generated method stub
		System.out.println(op.toString());
		System.out.println("  inl: " + op.getLeft().getOutput().render());
		System.out.println("  inr: " + op.getRight().getOutput().render());
		System.out.println("  out: " + op.getOutput().render());
	}

	@Override
	public void visit(Join op) {
		// TODO Auto-generated method stub
		System.out.println(op.toString());
		System.out.println("  inl: " + op.getLeft().getOutput().render());
		System.out.println("  inr: " + op.getRight().getOutput().render());
		System.out.println("  out: " + op.getOutput().render());
	}
}

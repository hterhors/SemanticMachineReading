package de.hterhors.semanticmr.crf.learner.regularizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

public class L2 implements Regularizer {

	private double l2 = 0.0001F;

	public L2() {
	}

	public L2(double l2) {
		this.l2 = l2;
	}

	@Override
	public void regularize(DoubleVector gradients, DoubleVector weights) {
		gradients.mulAndAdd(weights, l2);
	}

	@Override
	public double penalize(DoubleVector weights) {
		double penalty = weights.length() * l2;
		return penalty;
	}

}

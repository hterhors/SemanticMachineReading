package de.hterhors.semanticmr.crf.learner.regularizer;

import de.hterhors.semanticmr.crf.variables.Vector;

public class L2 implements Regularizer {

	private double l2 = 0.0001;

	public L2() {
	}

	public L2(double l2) {
		this.l2 = l2;
	}

	@Override
	public Vector regularize(Vector gradients, Vector weights) {
		Vector regularized = gradients.add(weights.mul(l2));
		return regularized;
	}

	@Override
	public double penalize(Vector weights) {
		double penalty = weights.length() * l2;
		return penalty;
	}

}

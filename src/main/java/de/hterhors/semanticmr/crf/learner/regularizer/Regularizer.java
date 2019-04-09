package de.hterhors.semanticmr.crf.learner.regularizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

public interface Regularizer {

	public double penalize(DoubleVector weights);

	public void regularize(DoubleVector gradients, DoubleVector weights);
}

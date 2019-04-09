package de.hterhors.semanticmr.crf.learner.optimizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

public interface Optimizer {

	public void applyUpdates(DoubleVector theta, DoubleVector gradient);

	public double getCurrentAlphaValue();
}

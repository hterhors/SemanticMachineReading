package de.hterhors.semanticmr.crf.learner.optimizer;

import de.hterhors.semanticmr.crf.variables.Vector;

public interface Optimizer {

	public Vector getUpdates(Vector theta, Vector gradient);

	public double getCurrentAlphaValue();
}

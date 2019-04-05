package de.hterhors.semanticmr.crf.learner.regularizer;

import de.hterhors.semanticmr.crf.variables.Vector;

public interface Regularizer {
	public double penalize(Vector weights);

	public Vector regularize(Vector gradients, Vector weights);
}

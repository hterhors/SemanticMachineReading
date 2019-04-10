package de.hterhors.semanticmr.crf.learner.regularizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

/**
 * Regularization interface.
 * 
 * @author hterhors
 *
 */
public interface Regularizer {

	/**
	 * Computes a panelization score based on the input weight vector.
	 * 
	 * @param weights
	 * @return penalization score
	 */
	public double penalize(DoubleVector weights);

	/**
	 * Applies a regularization to the gradients vector based on the weights vector.
	 * 
	 * @param gradients
	 * @param weights
	 */
	public void regularize(DoubleVector gradients, DoubleVector weights);
}

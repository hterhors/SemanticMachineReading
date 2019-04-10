package de.hterhors.semanticmr.crf.learner.optimizer;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

/**
 * Optimizer interface
 * 
 * @author hterhors
 *
 */
public interface Optimizer {

	/**
	 * Applys updates to the theta vector based on the gradient vector.
	 * 
	 * @param theta    the vector to which updates are applied
	 * @param gradient the vector from which updates are computed.
	 */
	public void applyUpdates(DoubleVector theta, DoubleVector gradient);

	/**
	 * Getter for the current alpha value.
	 * 
	 * @return current alpha
	 */
	public double getCurrentAlphaValue();
}

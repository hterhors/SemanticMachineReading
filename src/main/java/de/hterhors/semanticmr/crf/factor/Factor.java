package de.hterhors.semanticmr.crf.factor;

import de.hterhors.semanticmr.crf.variables.DoubleVector;

/**
 * A factor is an object that connects a feature vector to the variables that
 * are involved in computing this feature vector. Since the generation of a
 * factor and the actual computation of its features is separated into to steps,
 * you need to store/reference the variables you need for the computation of the
 * features inside the factorVariables object.
 * 
 * @author hterhors
 *
 */
public class Factor<S extends AbstractFactorScope<?>> {

	/**
	 * The factors scope
	 */
	private final S factorScope;

	/**
	 * The factors feature
	 */
	private final DoubleVector featureVector = new DoubleVector();

	/**
	 * Initialize the factor with its scope
	 * 
	 * @param factorScope
	 */
	public Factor(S factorScope) {
		this.factorScope = factorScope;
	}

	/**
	 * Getter for the feature vector
	 * 
	 * @return
	 */
	public DoubleVector getFeatureVector() {
		return featureVector;
	}

	/**
	 * Getter for the factor scope
	 * 
	 * @return
	 */
	public S getFactorScope() {
		return factorScope;
	}

	/**
	 * Computes the scalar score of that factor.
	 * 
	 * @return the factor scalar score
	 */
	public double computeScalarScore() {
		return Math.exp(featureVector.dotProduct(factorScope.getTemplate().getWeights()));
	}
}

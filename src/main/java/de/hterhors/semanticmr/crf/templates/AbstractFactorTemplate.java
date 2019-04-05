package de.hterhors.semanticmr.crf.templates;

import java.util.List;

import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.factor.FactorScope;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.crf.variables.Vector;

public abstract class AbstractFactorTemplate {

	/**
	 * Weights for the computation of factor scores. These weights are shared across
	 * all factors of this template.
	 */
	protected Vector weights = new Vector();

	/**
	 * Updates the weight of the given feature by adding the given alpha value.
	 * 
	 * @param feature
	 * @param gradient
	 * @param currentAlpha
	 */
	public void update(String feature, double update) {
		weights.addToValue(feature, update);
	}

	public Vector getWeights() {
		return weights;
	}

	public void setWeights(Vector weights) {
		this.weights = weights;
	}

	/**
	 * Returns all possible factor scopes that can be applied to the given state.
	 * Each FactorScope declares which variables are relevant for its computation
	 * but does NOT compute any features yet. Later, a selected set of factor
	 * variables that were created here are passed to the computeFactor() method,
	 * for the actual computation of factors and feature values.
	 * 
	 * @param state
	 * @return
	 */
	public abstract List<FactorScope> generateFactorScopes(State state);

	/**
	 * This method receives the previously created "empty" factor scopes and
	 * computes the features for this factor. For this, each previously created
	 * FactorScopes should include all the variables it needs to compute the
	 * respective factor.
	 * 
	 * @param state
	 * @param factor
	 */
	public abstract void computeFeatureVector(Factor factor);

	@Override
	public String toString() {
		return "AbstractFactorTemplate [weights=" + weights + "]";
	}

}

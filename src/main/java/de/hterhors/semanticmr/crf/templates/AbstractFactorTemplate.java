package de.hterhors.semanticmr.crf.templates;

import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

public abstract class AbstractFactorTemplate<S extends AbstractFactorScope> {

	/**
	 * Weights for the computation of factor scores. These weights are shared across
	 * all factors of this template.
	 */
	protected DoubleVector weights = new DoubleVector();

	public DoubleVector getWeights() {
		return weights;
	}

	public void setWeights(DoubleVector weights) {
		this.weights = weights;
	}

	/**
	 * Returns all possible factor scopes that can be applied to the given state.
	 * Each FactorScope declares which variables are relevant for its computation
	 * but does NOT compute any features yet. Later, a selected set of factor
	 * variables that were created here are passed to the generateFeatureVector()
	 * method, for the actual computation of factors and feature values.
	 * 
	 * @param state
	 * @return
	 */
	public abstract List<S> generateFactorScopes(State state);

	/**
	 * This method receives the previously created factor scopes and generates the
	 * features for this factor. For this, each previously created factor scope
	 * should include all the variables it needs to compute the respective factor.
	 * 
	 * @param state
	 * @param factor
	 */
	public abstract void generateFeatureVector(Factor<S> factor);

}

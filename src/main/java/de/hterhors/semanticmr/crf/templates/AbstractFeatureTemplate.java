package de.hterhors.semanticmr.crf.templates;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * Abstract class for feature templates. Consists of the shared weight vector.
 * 
 * @author hterhors
 *
 */
public abstract class AbstractFeatureTemplate<Scope extends AbstractFactorScope> {

	/**
	 * Weights for the computation of factor scores. These weights are shared across
	 * all factors of this template.
	 */
	protected DoubleVector weights = new DoubleVector();

	public DoubleVector getWeights() {
		return weights;
	}

	public final boolean enableFactorCaching;

	public AbstractFeatureTemplate() {
		this.enableFactorCaching = true;
	}

	public AbstractFeatureTemplate(final boolean chacheFactors) {
		this.enableFactorCaching = chacheFactors;
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
	public abstract List<Scope> generateFactorScopes(State state);

	/**
	 * 
	 * This method receives the previously created factor scopes and generates the
	 * features for this factor. For this, each previously created factor scope
	 * should include all the variables it needs to compute the respective factor.
	 * 
	 * @param state
	 * @param f
	 */
	public abstract void generateFeatureVector(Factor<Scope> f);

	public void setWeights(DoubleVector weights) {
		this.weights = weights;
	}

	protected <A extends AbstractAnnotation> List<A> getPredictedAnnotations(State state) {
		return state.getCurrentPredictions().getAnnotations();
	}

	public void initalize(Object[] parameter) {
		if (parameter != null) {
			throw new NotImplementedException(
					"The 'initialize'-method in template-class: " + this.getClass().getSimpleName()
							+ " is not proper implemented. Passed parameter won't have any effect!");
		}
	}

}

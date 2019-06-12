package de.hterhors.semanticmr.crf.factor;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * A factor graph is basically a collection of factor scopes and corresponding
 * factors. In this framework, each factor graph is specific to exactly one
 * state. Shared factors are handled by the factor pool.
 * 
 * @author hterhors
 *
 * @see {@link State}
 * @see {@link FactorPool}
 */
public class FactorGraph {

	/**
	 * The factor scopes of that factor graph.
	 */
	final private List<AbstractFactorScope<?>> factorScopes = new ArrayList<>();

	/**
	 * A cache of computed factors which is computed only once after the factor
	 * scopes are added to this factor graph.
	 */
	private List<Factor<?>> cache = null;

	/**
	 * Is dirty flag for computing the factor cache.
	 */
	private boolean isDirty = true;

	/**
	 * Add factor scopes to that factor graph. This method is called for each
	 * feature template that plays a role in this factor graph.
	 * 
	 * @param generatedFactorScopes a list of scopes for a particular feature
	 *                              template.
	 * 
	 * @see {@link AbstractFeatureTemplate}
	 */
	public void addFactorScopes(List<? extends AbstractFactorScope<?>> generatedFactorScopes) {
		this.factorScopes.addAll(generatedFactorScopes);
		/*
		 * Set dirty flag to true in case the factors need to be recomputed. This
		 * usually never happens as the factor scopes are added completely before
		 * computing any factors.
		 */
		this.isDirty = true;
	}

	/**
	 * Returns a list of pre-computed factors from the factor pool.
	 * 
	 * @return
	 */
	public List<Factor<?>> getFactors() {

		if (this.isDirty || this.cache == null)
			this.cache = FactorPool.getInstance().getFactors(factorScopes);

		/*
		 * Set dirty flag to false.
		 */
		this.isDirty = false;
		return cache;
	}

	/**
	 * Return the factor scopes of this factor graph.
	 * 
	 * @return list of factor scopes.
	 */
	public List<AbstractFactorScope<?>> getFactorScopes() {
		return factorScopes;
	}

}

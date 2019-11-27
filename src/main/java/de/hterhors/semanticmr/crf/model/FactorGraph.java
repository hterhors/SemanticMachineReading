package de.hterhors.semanticmr.crf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	final private List<AbstractFactorScope> factorScopes = new ArrayList<>();

	/**
	 * A cache of computed factors which is computed only once after the factor
	 * scopes are added to this factor graph.
	 */
	private List<Factor> cache = null;

	/**
	 * Is dirty flag for computing the factor cache.
	 */
	private boolean isDirty = true;
	private final AbstractFeatureTemplate<?> template;
	final private FactorPool factorPool;

	public FactorGraph(FactorPool factorPool, AbstractFeatureTemplate<?> template) {
		this.template = template;
		this.factorPool = factorPool;
	}

	/**
	 * Add factor scopes to that factor graph. This method is called for each
	 * feature template that plays a role in this factor graph.
	 * 
	 * @param generatedFactorScopes a list of scopes for a particular feature
	 *                              template.
	 * 
	 * @see {@link AbstractFeatureTemplate}
	 */
	public void addFactorScopes(List<AbstractFactorScope> generatedFactorScopes) {

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
	public List<Factor> getFactors() {
		if (this.template.enableFactorCaching)
			return getCachedFactors();
		else
			return getUnCachedFactors();
	}

	public List<Factor> getCachedFactors() {
		if (this.isDirty || this.cache == null)
			this.cache = factorPool.getFactors(factorScopes);

		/*
		 * Set dirty flag to false.
		 */
		this.isDirty = false;
		return cache;
	}

	public List<Factor> getUnCachedFactors() {

		List<Factor> factors = new ArrayList<>();

		for (AbstractFactorScope fs : factorScopes) {
			factors.add(new Factor(fs));
		}

		factors.parallelStream().forEach(f -> this.template.generateFeatureVector(f));
		return factors;

	}

	/**
	 * Return the factor scopes of this factor graph.
	 * 
	 * @return list of factor scopes.
	 */
	public List<? extends AbstractFactorScope> getFactorScopes() {
		return factorScopes;
	}

}

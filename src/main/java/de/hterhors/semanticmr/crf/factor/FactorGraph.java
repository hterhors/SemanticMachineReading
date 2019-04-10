package de.hterhors.semanticmr.crf.factor;

import java.util.ArrayList;
import java.util.List;

/**
 * A factor graph is basically a collection of factors.
 * 
 * @author hterhors
 *
 */
public class FactorGraph {

	final private List<FactorScope> factorScopes = new ArrayList<>();

	private List<Factor> cache = null;

	/**
	 * Is dirty flag for computing the factor cache.
	 */
	private boolean isDirty = true;

	public void addFactorScopes(List<? extends FactorScope> generatedFactorScopes) {
		this.factorScopes.addAll(generatedFactorScopes);
		this.isDirty = true;
	}

	public List<Factor> getFactors() {

		if (this.isDirty || this.cache == null)
			this.cache = FactorPool.getInstance().getFactors(factorScopes);

		this.isDirty = false;
		return cache;
	}

	public List<FactorScope> getFactorScopes() {
		return factorScopes;
	}

	@Override
	public String toString() {
		return "FactorGraph [factorScopes=" + factorScopes + "]";
	}

}

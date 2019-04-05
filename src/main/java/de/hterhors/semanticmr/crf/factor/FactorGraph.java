package de.hterhors.semanticmr.crf.factor;

import java.util.ArrayList;
import java.util.List;

public class FactorGraph {

	final private List<FactorScope> factorScopes;

	private List<Factor> cache = null;

	public FactorGraph() {
		factorScopes = new ArrayList<>();
	}

	public void addFactorScopes(List<? extends FactorScope> generatedFactorScopes) {
		this.factorScopes.addAll(generatedFactorScopes);
	}


	public List<Factor> getFactors() {
		if (cache == null)
			cache = FactorPool.getInstance().getFactors(factorScopes);
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

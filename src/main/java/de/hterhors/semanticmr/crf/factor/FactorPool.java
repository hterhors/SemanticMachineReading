package de.hterhors.semanticmr.crf.factor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class FactorPool {

	final private Map<FactorScope, Factor> factorCache = new HashMap<>();

	private static FactorPool sharedInstance = null;

	private FactorPool() {
	}

	protected static FactorPool getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new FactorPool();
		}
		return sharedInstance;
	}

	protected Set<FactorScope> getCachedFactorScopes() {
		return factorCache.keySet();
	}

	protected List<Factor> getFactors(List<FactorScope> factorVariablesList) {

		if (factorVariablesList.isEmpty())
			return Collections.emptyList();

		final List<Factor> factors = new ArrayList<>();

		for (FactorScope factorVariables : factorVariablesList) {

			final Factor factor;

			if ((factor = factorCache.get(factorVariables)) == null)
				throw new IllegalStateException(
						String.format("Could not retrieve factor for requested factor variables: %s", factorVariables));

			factors.add(factor);

		}
		return factors;
	}

	protected void addFactor(Factor factor) {
		System.out.println(factorCache.size());
		final Factor old = factorCache.put(factor.getFactorScope(), factor);
		if (old != null)
			throw new IllegalStateException("Factorpool already contains factor. " + factor);
	}

	protected void clear() {
		factorCache.clear();
	}

}

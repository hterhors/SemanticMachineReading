package de.hterhors.semanticmr.crf.factor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The FactorPool handles the caching of computed factors.
 * 
 * Computed factors are added to the pool using its factor scope as key.
 * 
 * @author hterhors
 *
 */
final class FactorPool {

	/**
	 * The factor cache
	 */
	final private Map<AbstractFactorScope<?>, Factor<?>> factorCache = new HashMap<>();

	/**
	 * The instance of this caching pool.
	 */
	private static FactorPool sharedInstance = null;

	private FactorPool() {
	}

	/**
	 * Singleton-method. Returns the FactorPool instance.
	 * 
	 * @return singleton instance.
	 */
	protected static FactorPool getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new FactorPool();
		}
		return sharedInstance;
	}

	/**
	 * 
	 * Checks if the factor pool already contains a factor scope as key.
	 * 
	 * @param factorScope
	 * @return true if the pool contains the given factor scope.
	 */
	protected boolean containsFactorScope(AbstractFactorScope<?> factorScope) {
		return factorCache.get(factorScope) != null;
	}

	/**
	 * Retrieves a list of factors for the given list of factor scopes. Note that,
	 * the retrieval process is not cached internally!
	 * 
	 * @throws IllegalStateException if the pool does not contain any of the
	 *                               searched factor scopes.
	 * 
	 * @param factorScopes
	 * @return an unmodifiable list of factors that corresponds to the list of
	 *         factor scopes.
	 */
	protected List<Factor<?>> getFactors(List<AbstractFactorScope<?>> factorScopes) {

		if (factorScopes.isEmpty())
			return Collections.emptyList();

		final List<Factor<?>> factors = new ArrayList<>();

		for (AbstractFactorScope<?> factorVariables : factorScopes) {

			final Factor<?> factor;

			if ((factor = factorCache.get(factorVariables)) == null)
				throw new IllegalStateException(
						String.format("Could not retrieve factor for requested factor variables: %s", factorVariables));

			factors.add(factor);

		}
		return Collections.unmodifiableList(factors);
	}

	/**
	 * Adds a pre-computed factor to this factor pool. The factor is stored under
	 * its corresponding factor scope.
	 * 
	 * @throws IllegalStateException if a factor was already stored for that
	 *                               specific scope.
	 * 
	 * @param factor
	 */
	protected void addFactor(Factor<?> factor) {
		final Factor<?> old = factorCache.put(factor.getFactorScope(), factor);
		if (old != null)
			throw new IllegalStateException("Factorpool already contains factor. " + factor);
	}

}

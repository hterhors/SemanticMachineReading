package de.hterhors.semanticmr.crf.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/**
 * The FactorPool handles the caching of computed factors.
 * 
 * Computed factors are added to the pool using its factor scope as key.
 * 
 * @author hterhors
 *
 */
final public class FactorPoolCache {

	/**
	 * The factor cache
	 */
	public LoadingCache<AbstractFactorScope, Factor> factorCache;

	/**
	 * The instance of this caching pool.
	 */
	// private static FactorPool sharedInstance = null;

	public FactorPoolCache(Model model, int maxSize, int initSize) {
		factorCache = CacheBuilder.newBuilder().maximumSize(maxSize).initialCapacity(initSize)
				.build(new FactorCacheLoader(model));
	}

//	/**
//	 * Singleton-method. Returns the FactorPool instance.
//	 * 
//	 * @return singleton instance.
//	 */
//	protected static FactorPool getInstance() {
//		if (sharedInstance == null) {
//			sharedInstance = new FactorPool();
//		}
//		return sharedInstance;
//	}

//	CRFStatistics [context=Train, getTotalDuration()=31683]
//	CRFStatistics [context=Test, getTotalDuration()=1348]

//	CRFStatistics [context=Train, getTotalDuration()=23368]
//	CRFStatistics [context=Test, getTotalDuration()=1044]

	/**
	 * 
	 * Checks if the factor pool already contains a factor scope as key.
	 * 
	 * @param factorScope
	 * @return true if the pool contains the given factor scope.
	 */
	protected boolean containsFactorScope(AbstractFactorScope factorScope) {
		/*
		 * If is not cacheable return false,
		 */
		if (!factorScope.template.enableFactorCaching)
			return false;

		return factorCache.getIfPresent(factorScope) != null;
	}

	/**
	 * 
	 * Checks if the factor pool already contains a factor scope as key.
	 * 
	 * @param factorScope
	 * @return true if the pool contains the given factor scope.
	 */
	protected Factor getIfPresent(AbstractFactorScope factorScope) {
		/*
		 * If is not cacheable return false,
		 */
		if (!factorScope.template.enableFactorCaching)
			return null;

		return factorCache.getIfPresent(factorScope);
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
	protected <Scope extends AbstractFactorScope> List<Factor> getFactors(List<Scope> factorScopes) {

		if (factorScopes.isEmpty())
			return Collections.emptyList();

		final List<Factor<?>> factors = new ArrayList<>();

		for (Scope factorVariables : factorScopes) {

			final Factor<?> factor;

			try {
				factor = factorCache.get(factorVariables);
			} catch (ExecutionException e) {
				throw new IllegalStateException(
						String.format("Could not retrieve factor for requested factor variables: %s", factorVariables));
			}

			factors.add(factor);

		}
		return Collections.unmodifiableList(factors);
	}

	/**
	 * Adds a pre-computed factor to this factor pool. The factor is stored under
	 * its corresponding factor scope.
	 * 
	 * @param factor
	 */
	protected void addFactor(Factor factor) {
		if (!factor.getFactorScope().template.enableFactorCaching)
			return;
		factorCache.put(factor.getFactorScope(), factor);
	}

	public String stats() {
		return factorCache.stats().toString();
	}

}

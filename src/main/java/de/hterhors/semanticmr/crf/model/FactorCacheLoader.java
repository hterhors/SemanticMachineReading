package de.hterhors.semanticmr.crf.model;

import com.google.common.cache.CacheLoader;

public class FactorCacheLoader extends CacheLoader<AbstractFactorScope, Factor> {

	private Model model = null;

	public FactorCacheLoader(Model model) {
		this.model = model;
	}

	@Override
	public Factor load(AbstractFactorScope key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}

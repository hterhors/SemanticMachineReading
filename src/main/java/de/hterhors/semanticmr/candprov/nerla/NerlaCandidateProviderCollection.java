package de.hterhors.semanticmr.candprov.nerla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of named entity recognition and linking candidate provider.
 * 
 * @author hterhors
 *
 */
public class NerlaCandidateProviderCollection {

	/**
	 * The list of candidate provider.
	 */
	final private List<INerlaCandidateProvider> candidateProvider = new ArrayList<>();

	public NerlaCandidateProviderCollection(INerlaCandidateProvider p) {
		candidateProvider.add(p);
	}

	/**
	 * Add a new candidate provider to the list. Each candidate provider can only be
	 * added once!
	 * 
	 * @param p the provider
	 * @return this collection
	 */
	public NerlaCandidateProviderCollection addCandidateProvider(INerlaCandidateProvider p) {
		if (candidateProvider.contains(p))
			System.out.println(
					"WARN: can not add candidate provider because it is already part of this collection: " + p);
		else
			candidateProvider.add(p);

		return this;
	}

	/**
	 * Returns an unmodifiable list of candidate provider.
	 * 
	 * @return the list of candidate provider.
	 */
	public List<INerlaCandidateProvider> getCandidateProvider() {
		return Collections.unmodifiableList(candidateProvider);
	}

}

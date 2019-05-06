package de.hterhors.semanticmr.candprov.nerla;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

public class ExhaustiveCandidateRetrieval implements INerlaCandidateProvider {

	private ExhaustiveCandidateRetrieval() {
	}

	@Override
	public Set<EntityType> getEntityTypeCandidates(String text) {
		return EntityType.getEntityTypes();
	}

	private static ExhaustiveCandidateRetrieval instance;

	public static INerlaCandidateProvider getInstance() {
		if (instance == null)
			instance = new ExhaustiveCandidateRetrieval();

		return instance;
	}

}

package de.hterhors.semanticmr.candprov.nerla;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

/**
 * An exhaustive candidate retrieval for named entity recognition and linking.
 * Given any surface form from a document, it always returns all existing entity
 * types of the current system scope.
 * 
 * @author hterhors
 *
 */
public class ExhaustiveCandidateRetrieval implements INerlaCandidateProvider {

	/**
	 * An exhaustive candidate retrieval for named entity recognition and linking.
	 * Given any surface form from a document, it always returns all existing entity
	 * types of the current system scope.
	 */
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

package de.hterhors.semanticmr.candprov.nerla;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

public class DefinedExhaustiveCandidateRetrieval implements INerlaCandidateProvider {

	private Set<EntityType> types;

	public DefinedExhaustiveCandidateRetrieval(Set<EntityType> types) {
		this.types = types;
	}

	@Override
	public Set<EntityType> getEntityTypeCandidates(String text) {
		return types;
	}

}

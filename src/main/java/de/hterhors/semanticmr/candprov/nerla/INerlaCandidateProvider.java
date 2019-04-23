package de.hterhors.semanticmr.candprov.nerla;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

public interface INerlaCandidateProvider {

	public Set<EntityType> getEntityTypeCandidates(final String text);

}

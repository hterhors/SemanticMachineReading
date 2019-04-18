package de.hterhors.semanticmr.candprov.nerla;

import java.util.List;

import de.hterhors.semanticmr.crf.structure.EntityType;

public interface INERLACandidateProvider {

	public List<EntityType> getEntityTypeCandidates(final String text);

}

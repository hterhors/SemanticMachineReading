package de.hterhors.semanticmr.candprov.sf;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;

public interface IEntityTypeAnnotationCandidateProvider extends ICandidateProvider {

	public Set<? extends EntityTypeAnnotation> getCandidates(EntityType templateType);

}

package de.hterhors.semanticmr.candprov.sf;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;

public interface IAnnotationCandidateProvider {

	public List<? extends AbstractAnnotation<?extends AbstractAnnotation<?>>> getSlotFillerCandidates(SlotType slot);

	public IAnnotationCandidateProvider addSlotFiller(AbstractAnnotation<? extends AbstractAnnotation<?>> slotFiller);

	public IAnnotationCandidateProvider addBatchSlotFiller(
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> slotFiller);

	/**
	 * TODO: UGLY PROGRAMMING HERE: Separate somehow.
	 * 
	 * @param templateType
	 * @return
	 */
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType);

}

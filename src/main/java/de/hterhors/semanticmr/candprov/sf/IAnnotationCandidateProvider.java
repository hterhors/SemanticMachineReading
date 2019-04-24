package de.hterhors.semanticmr.candprov.sf;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;

public interface IAnnotationCandidateProvider {

	public List<? extends AbstractSlotFiller<?extends AbstractSlotFiller<?>>> getSlotFillerCandidates(SlotType slot);

	public IAnnotationCandidateProvider addSlotFiller(AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller);

	public IAnnotationCandidateProvider addBatchSlotFiller(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFiller);

	/**
	 * TODO: UGLY PROGRAMMING HERE: Separate somehow.
	 * 
	 * @param templateType
	 * @return
	 */
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType);

}

package de.hterhors.semanticmr.candprov;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;

public interface ISlotFillerCandidateProvider<T extends AbstractSlotFiller<T>> {

	public List<? extends AbstractSlotFiller<T>> getSlotFillerCandidates(SlotType slot);

	public ISlotFillerCandidateProvider<T> addSlotFiller(T slotFiller);

	public ISlotFillerCandidateProvider<T> addBatchSlotFiller(Collection<T> slotFiller);

	/**
	 * TODO: UGLY PROGRAMMING HERE: Separate somehow.
	 * 
	 * @param templateType
	 * @return
	 */
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType);

}

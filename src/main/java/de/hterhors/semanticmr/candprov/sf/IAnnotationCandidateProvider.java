package de.hterhors.semanticmr.candprov.sf;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public interface IAnnotationCandidateProvider<T extends AbstractSlotFiller<T>> {

	public List<? extends AbstractSlotFiller<T>> getSlotFillerCandidates(SlotType slot);

	public IAnnotationCandidateProvider<T> addSlotFiller(T slotFiller);

	public IAnnotationCandidateProvider<T> addBatchSlotFiller(Collection<T> slotFiller);

	/**
	 * TODO: UGLY PROGRAMMING HERE: Separate somehow.
	 * 
	 * @param templateType
	 * @return
	 */
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType);

}

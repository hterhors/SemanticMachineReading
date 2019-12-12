package de.hterhors.semanticmr.candprov.sf;

import java.util.Collection;
import java.util.List;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;

public interface ISlotTypeAnnotationCandidateProvider extends ICandidateProvider {

	public List<? extends AbstractAnnotation> getCandidates(SlotType slot);

	public ISlotTypeAnnotationCandidateProvider addSlotFiller(AbstractAnnotation slotFiller);

	public ISlotTypeAnnotationCandidateProvider addBatchSlotFiller(Collection<AbstractAnnotation> slotFiller);

}

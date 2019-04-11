package de.hterhors.semanticmr.candprov;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public interface ISlotFillerCandidateProvider<T extends AbstractSlotFiller<T>> {

	public List<? extends AbstractSlotFiller<T>> getSlotFillerCandidates(SlotType slot);

	public void addSlotFiller(T slotFiller);

	public void addBatchSlotFiller(Collection<T> slotFiller);

	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType);

}

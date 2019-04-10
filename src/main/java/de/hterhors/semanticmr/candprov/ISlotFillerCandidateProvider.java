package de.hterhors.semanticmr.candprov;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public interface ISlotFillerCandidateProvider<T extends AbstractSlotFiller<T>> {

//	public class Unmodifyable<T extends AbstractSlotFiller<T>> implements ISlotFillerCandidateProvider<T> {
//
//		final private ISlotFillerCandidateProvider<T> cp;
//
//		public Unmodifyable(ISlotFillerCandidateProvider<T> cp) {
//			this.cp = cp;
//		}
//
//		@Override
//		public void addSlotFiller(T slotFiller) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public void addBatchSlotFiller(Collection<T> slotFiller) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public List<? extends AbstractSlotFiller<T>> getSlotFillerCandidates(SlotType slot) {
//			return cp.getSlotFillerCandidates(slot);
//		}
//
//		@Override
//		public Set<EntityType> getTemplateTypeCandidates(EntityType templateType) {
//			return cp.getTemplateTypeCandidates(templateType);
//		}
//	}

	public List<? extends AbstractSlotFiller<T>> getSlotFillerCandidates(SlotType slot);

	public void addSlotFiller(T slotFiller);

	public void addBatchSlotFiller(Collection<T> slotFiller);

	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType);

}

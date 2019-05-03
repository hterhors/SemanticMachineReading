package de.hterhors.semanticmr.crf.structure.slots;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IDeepCopyable;

/**
 * Abstract class of a slot. There are two instantiations.
 * {@link SingleFillerSlot} and {@link MultiFillerSlot}.
 * 
 * Each slot is initialized with an evaluationFunction as described in the
 * specifications.
 * 
 * @author hterhors
 *
 */
public abstract class AbstractSlot implements IDeepCopyable<AbstractSlot> {
	/**
	 * The slot type is specified by the specifications and can not be changed.
	 */
	public final SlotType slotType;

	public AbstractSlot(SlotType slotType) {
		this.slotType = slotType;
	}

	/**
	 * Whether the slot contains annotation(s) or not.
	 * 
	 * @return
	 */
	public abstract boolean containsSlotFiller();

	public abstract boolean containsSlotFillerOfEntityType(EntityType entityType);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slotType == null) ? 0 : slotType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractSlot other = (AbstractSlot) obj;
		if (slotType == null) {
			if (other.slotType != null)
				return false;
		} else if (!slotType.equals(other.slotType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractSlot [slotType=" + slotType + "]";
	}

}

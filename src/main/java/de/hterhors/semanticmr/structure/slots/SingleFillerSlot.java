package de.hterhors.semanticmr.structure.slots;

import de.hterhors.semanticmr.exce.IllegalSlotFillerException;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;

public class SingleFillerSlot extends AbstractSlot {

	/**
	 * The single annotation of this slot.
	 */
	private AbstractSlotFiller<?> slotFiller = null;

	public SingleFillerSlot(SlotType slotType) {
		super(slotType);
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param slotType
	 * @param deepClone
	 */
	private SingleFillerSlot(SlotType slotType, AbstractSlotFiller<?> slotFiller) {
		super(slotType);
		this.slotFiller = slotFiller;
	}

	public AbstractSlotFiller<?> getSlotFiller() {
		return slotFiller;
	}

	public boolean containsSlotFillerOfEntityType(EntityType entityType) {
		return !containsSlotFiller() ? false : slotFiller.getEntityType() == entityType;
	}

	/**
	 * Updates the annotation and returns the old annotation.
	 * 
	 * @param slotFiller
	 * @return the old annotation.
	 */
	public void set(final AbstractSlotFiller<?> slotFiller) {

		if (this.slotFiller == slotFiller)
			return;

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException("The provided slot filler annotation type "
					+ slotFiller.getEntityType() + "is not suitable for this slot of type " + slotType);

		this.slotFiller = slotFiller;
	}

	@Override
	public boolean containsSlotFiller() {
		return slotFiller != null;
	}

	@Override
	public String toString() {
		return "SingleAnnotationSlot [annotation=" + slotFiller + ", slotType=" + slotType + ", containsAnnotations()="
				+ containsSlotFiller() + "]";
	}

	public SingleFillerSlot deepCopy() {
		return new SingleFillerSlot(slotType, slotFiller == null ? null : slotFiller.deepCopy());
	}

	public String toPrettyString(final int depth) {
		return slotFiller == null ? ""
				: (new StringBuilder(slotType.toPrettyString(depth)).append("\t")
						.append(slotFiller.toPrettyString(depth))).toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((slotFiller == null) ? 0 : slotFiller.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleFillerSlot other = (SingleFillerSlot) obj;
		if (slotFiller == null) {
			if (other.slotFiller != null)
				return false;
		} else if (!slotFiller.equals(other.slotFiller))
			return false;
		return true;
	}

	public void removeFiller() {
		slotFiller = null;
	}

}

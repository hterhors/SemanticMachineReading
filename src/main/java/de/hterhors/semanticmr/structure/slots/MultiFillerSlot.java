package de.hterhors.semanticmr.structure.slots;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.exceptions.ExceedsMaximumNumberOfSlotFillerException;
import de.hterhors.semanticmr.exceptions.IllegalSlotFillerException;
import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;

public class MultiFillerSlot extends AbstractSlot {

	private final LinkedHashSet<AbstractSlotFiller<?>> slotFiller;

	public MultiFillerSlot(SlotType slotType) {
		super(slotType);
		this.slotFiller = new LinkedHashSet<>(slotType.multiFillerSlotMaxCapacity);
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param slotType
	 * @param slotFiller
	 */
	private MultiFillerSlot(final SlotType slotType, final LinkedHashSet<AbstractSlotFiller<?>> slotFiller) {
		super(slotType);
		this.slotFiller = slotFiller;
	}

	public LinkedHashSet<AbstractSlotFiller<?>> getSlotFiller() {
		return slotFiller;
	}

	public int getMaxExplorationCapacity() {
		return slotType.multiFillerSlotMaxCapacity;
	}

	public boolean containsMaximumFiller() {
		return slotFiller.size() == slotType.multiFillerSlotMaxCapacity;
	}

	public boolean containsSlotFiller(AbstractSlotFiller<?> slotFillerCandidate) {
		return this.slotFiller.contains(slotFillerCandidate);
	}

	@Override
	public boolean containsSlotFiller() {
		return !slotFiller.isEmpty();
	}

	public void addSlotFiller(AbstractSlotFiller<?> slotFiller) throws ExceedsMaximumNumberOfSlotFillerException {

		if (containsMaximumFiller())
			throw new ExceedsMaximumNumberOfSlotFillerException(
					"Can not add slot filler, maximum capcaity of " + getMaxExplorationCapacity() + " reached!");

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException(
					"The provided slot filler annotation type " + slotFiller.getEntityType()
							+ " is not suitable for this slot of type " + slotType.toPrettyString());

		this.slotFiller.add(slotFiller);
	}

	public void removeSlotFiller(AbstractSlotFiller<?> slotFiller) {
		this.slotFiller.remove(slotFiller);
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
		MultiFillerSlot other = (MultiFillerSlot) obj;
		if (slotFiller == null) {
			if (other.slotFiller != null)
				return false;
		} else if (!slotFiller.equals(other.slotFiller))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MultiFillerSlot [slotFiller=" + slotFiller + "]";
	}

	public MultiFillerSlot deepCopy() {
		return new MultiFillerSlot(slotType, slotFiller == null ? null
				: slotFiller.stream().map(a -> a.deepCopy()).collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	public String toPrettyString(final int depth) {

		if (slotFiller == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(slotType.toPrettyString(depth));
		sb.append("\t[\n");
		final int newDepth = depth + 1;
		for (AbstractSlotFiller<?> slotFiller : this.slotFiller) {
			for (int d = 0; d < newDepth; d++) {
				sb.append("\t");
			}
			sb.append(slotFiller.toPrettyString(newDepth));
			sb.append("\n");
		}
		for (int d = 0; d < newDepth; d++) {
			sb.append("\t");
		}
		sb.append("]");

		return sb.toString().trim();
	}

	public int size() {
		return this.slotFiller.size();
	}

	@Override
	public boolean containsSlotFillerOfEntityType(EntityType entityType) {

		if (!containsSlotFiller())
			return false;

		for (AbstractSlotFiller<?> slotFiller : slotFiller) {
			if (slotFiller.getEntityType() == entityType)
				return true;
		}

		return false;
	}

	/**
	 * Removes the current slot filler and adds the slot filler candidate to the set
	 * of slot fillers.
	 * 
	 * @param slotFiller          the old filler to remove
	 * @param slotFillerCandidate the new filler to add
	 */
	public void replace(AbstractSlotFiller<?> slotFiller, AbstractSlotFiller<?> slotFillerCandidate) {

		if (slotFiller == slotFillerCandidate)
			return;

		this.slotFiller.remove(slotFiller);
		this.slotFiller.add(slotFillerCandidate);
	}

}
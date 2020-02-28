package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.exce.ExceedsMaxSlotFillerException;
import de.hterhors.semanticmr.exce.IllegalSlotFillerException;

public class MultiFillerSlot extends AbstractSlot {

	private final LinkedHashSet<AbstractAnnotation> slotFiller;

	public MultiFillerSlot(SlotType slotType) {
		super(slotType);
		this.slotFiller = new LinkedHashSet<>();
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param slotType
	 * @param slotFiller
	 */
	private MultiFillerSlot(final SlotType slotType, final LinkedHashSet<AbstractAnnotation> slotFiller) {
		super(slotType);
		this.slotFiller = slotFiller;
	}

	public Set<AbstractAnnotation> getSlotFiller() {
		return Collections.unmodifiableSet(slotFiller);
	}

	public <E> Set<E> getAutoCastSlotFiller() {
		return (Set<E>) Collections.unmodifiableSet(slotFiller);
	}

	public int getMaxExplorationCapacity() {
		return slotType.slotMaxCapacity;
	}

	public boolean containsMaximumFiller() {
		return slotFiller.size() == slotType.slotMaxCapacity;
	}

	public boolean containsSlotFiller(IObjectiveFunction objectiveFunction, AbstractAnnotation slotFillerCandidate) {
		if (objectiveFunction != null) {

			for (AbstractAnnotation slotFillerValue : slotFiller) {
				if (slotFillerValue.evaluateEquals(objectiveFunction.getEvaluator(), slotFillerCandidate)) {
					return true;
				}
			}
//		} else {
//			return this.slotFiller.contains(slotFillerCandidate);
		}
		return false;
	}

	@Override
	public boolean containsSlotFiller() {
		return !slotFiller.isEmpty();
	}

	protected MultiFillerSlot add(AbstractAnnotation slotFiller) throws ExceedsMaxSlotFillerException {

		if (containsMaximumFiller())
			throw new ExceedsMaxSlotFillerException(
					"Can not add slot filler, maximum capcaity of " + getMaxExplorationCapacity() + " reached!");

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException(
					"The provided slot filler annotation type " + slotFiller.getEntityType()
							+ " is not suitable for this slot of type " + slotType.toPrettyString());

		this.slotFiller.add(slotFiller);
		return this;
	}

	protected void removeSlotFiller(AbstractAnnotation slotFiller) {
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

	@Override
	@SuppressWarnings("unchecked")
	public MultiFillerSlot deepCopy() {
		return new MultiFillerSlot(slotType,
				slotFiller == null ? null
						: slotFiller.stream().map(a -> (AbstractAnnotation) a.deepCopy())
								.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	public String toPrettyString(final int depth) {

		if (slotFiller == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(slotType.toPrettyString(depth));
		sb.append("\t[\n");
		final int newDepth = depth + 1;
		int c = 0;
		for (AbstractAnnotation slotFiller : this.slotFiller) {
			for (int d = 0; d < newDepth; d++) {
				sb.append("\t");
			}
			sb.append(slotFiller.toPrettyString(newDepth));
			sb.append("\n");
			if (c > 10) {
				for (int d = 0; d < newDepth; d++) {
					sb.append("\t");
				}
				sb.append("[omit rest...]\n");
				break;
			}
			c++;
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

		for (AbstractAnnotation slotFiller : slotFiller) {
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
	 * @return
	 */
	public MultiFillerSlot replace(AbstractAnnotation slotFiller, AbstractAnnotation slotFillerCandidate) {

		if (slotFiller == slotFillerCandidate)
			return this;

		this.slotFiller.remove(slotFiller);
		this.slotFiller.add(slotFillerCandidate);
		return this;
	}

}

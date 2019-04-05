package de.hterhors.semanticmr.structure.slotfiller;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

import de.hterhors.semanticmr.exceptions.IllegalSlotFillerException;
import de.hterhors.semanticmr.exceptions.UnkownMultiSlotException;
import de.hterhors.semanticmr.exceptions.UnkownSingleSlotException;
import de.hterhors.semanticmr.structure.slots.MultiFillerSlot;
import de.hterhors.semanticmr.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.structure.slots.SlotType;

/**
 * The information extraction template that needs to be filled.
 * 
 * @author hterhors
 *
 */
final public class EntityTemplate extends AbstractSlotFiller<EntityTemplate> {

	/**
	 * Defines the entity type of this annotation.
	 */
	private final EntityType entityType;

	/**
	 * An unmodifiable map of slots to fill.
	 */
	final private Map<SlotType, SingleFillerSlot> singleFillerSlots;

	/**
	 * An unmodifiable map of slots to fill.
	 */
	final private Map<SlotType, MultiFillerSlot> multiFillerSlots;

	/**
	 * Creates a new template entity of the given entity type.
	 * 
	 * @param entityType
	 */
	public EntityTemplate(EntityType entityType) {
		this.entityType = entityType;
		this.singleFillerSlots = Collections.unmodifiableMap(this.entityType.getSingleFillerSlotTypes().stream()
				.map(slotType -> new SingleFillerSlot(slotType)).collect(Collectors.toMap(s -> s.slotType, s -> s)));

		this.multiFillerSlots = Collections.unmodifiableMap(this.entityType.getMultiFillerSlotTypes().stream()
				.map(slotType -> new MultiFillerSlot(slotType)).collect(Collectors.toMap(s -> s.slotType, s -> s)));

	}

	/**
	 * Returns a set over all slot filler values for single- and multi-slot values.
	 * 
	 * @return a list of all direct slot filler values.
	 */
	public Set<AbstractSlotFiller<?>> getAllSlotFillerValues() {
		return Streams.concat(
				this.singleFillerSlots.values().stream().filter(s -> s.containsSlotFiller())
						.map(s -> s.getSlotFiller()),
				this.multiFillerSlots.values().stream().filter(l -> l.containsSlotFiller())
						.flatMap(l -> l.getSlotFiller().stream()))
				.collect(Collectors.toSet());
	}

	/**
	 * Deep clone constructor.
	 * 
	 * @param templateType
	 * @param singleFillerSlots
	 * @param multiFillerSlots
	 */
	private EntityTemplate(EntityType entityType, Map<SlotType, SingleFillerSlot> singleFillerSlots,
			Map<SlotType, MultiFillerSlot> multiFillerSlots) {
		this.entityType = entityType;
		this.singleFillerSlots = singleFillerSlots;
		this.multiFillerSlots = multiFillerSlots;
	}

	public boolean containsSingleFillerSlot(SlotType slotType) {
		return singleFillerSlots.containsKey(slotType);
	}

	public boolean containsMultiFillerSlot(SlotType slotType) {
		return multiFillerSlots.containsKey(slotType);
	}

	public SingleFillerSlot getSingleFillerSlot(SlotType slotType) {
		final SingleFillerSlot slot = singleFillerSlots.get(slotType);

		if (slot == null)
			throw new UnkownSingleSlotException("The requested single filler slot is unkown: " + slotType);

		return slot;
	}

	public EntityTemplate updateSingleFillerSlot(SlotType slotType, final AbstractSlotFiller<?> slotFiller) {

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");

		getSingleFillerSlot(slotType).updateFiller(slotFiller);
		return this;
	}

	public void updateMultiFillerSlot(SlotType slotType, AbstractSlotFiller<?> slotFiller,
			AbstractSlotFiller<?> slotFillerCandidate) {

		if (slotFillerCandidate == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (getMultiFillerSlot(slotType).containsSlotFiller(slotFillerCandidate)) {
			System.out.println("WARN: can not add same object twice: " + slotFiller.toPrettyString());
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");
		}

		getMultiFillerSlot(slotType).replace(slotFiller, slotFillerCandidate);
	}

	public void addToMultiFillerSlot(SlotType slotType, final AbstractSlotFiller<?> slotFiller) {

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (getMultiFillerSlot(slotType).containsSlotFiller(slotFiller)) {
			System.out.println("WARN: can not add same object twice: " + slotFiller.toPrettyString());
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");
		}

		getMultiFillerSlot(slotType).addSlotFiller(slotFiller);
	}

	public MultiFillerSlot getMultiFillerSlot(SlotType slotType) {

		final MultiFillerSlot slot = multiFillerSlots.get(slotType);

		if (slot == null)
			throw new UnkownMultiSlotException("The requested multi filler slot is unkown: " + slotType);

		return slot;
	}

	public Map<SlotType, SingleFillerSlot> getSingleFillerSlots() {
		return singleFillerSlots;
	}

	public Map<SlotType, MultiFillerSlot> getMultiFillerSlots() {
		return multiFillerSlots;
	}

	public String toPrettyString(final int depth) {
		final StringBuilder sb = new StringBuilder();
		final int newDepth = depth + 1;

		sb.append(entityType.toPrettyString());
		sb.append("\n");
		for (SingleFillerSlot slot : singleFillerSlots.values()) {
			if (slot.containsSlotFiller()) {
				for (int d = 0; d < newDepth; d++) {
					sb.append("\t");
				}
				sb.append(slot.toPrettyString(newDepth)).append("\n");
			}
		}
		for (MultiFillerSlot slot : multiFillerSlots.values()) {
			if (slot.containsSlotFiller()) {
				for (int d = 0; d < newDepth; d++) {
					sb.append("\t");
				}
				sb.append(slot.toPrettyString(newDepth)).append("\n");
			}
		}
		return sb.toString().trim();
	}

	@Override
	public String toString() {
		return "EntityTemplate [singleFillerSlots=" + singleFillerSlots + ", multiFillerSlots=" + multiFillerSlots
				+ ", entityType=" + entityType + "]";
	}

	@Override
	public EntityTemplate deepCopy() {
		return new EntityTemplate(entityType,
				this.singleFillerSlots.entrySet().stream()
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())),
				this.multiFillerSlots.entrySet().stream()
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())));
	}

	/**
	 * Creates a deep copy of this entity template but changes the template type.
	 * Overlapping slots are copied. New slots are empty. Vanishing slots are
	 * removed.
	 * 
	 * @param newTemplateType
	 * @return
	 */
	public EntityTemplate deepMergeCopy(EntityType newTemplateType) {
		return new EntityTemplate(newTemplateType,
				this.singleFillerSlots.entrySet().stream().filter(s -> newTemplateType.containsSlotType(s.getKey()))
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())),
				this.multiFillerSlots.entrySet().stream().filter(s -> newTemplateType.containsSlotType(s.getKey()))
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((multiFillerSlots == null) ? 0 : multiFillerSlots.hashCode());
		result = prime * result + ((singleFillerSlots == null) ? 0 : singleFillerSlots.hashCode());
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
		EntityTemplate other = (EntityTemplate) obj;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (entityType != other.entityType)
			return false;
		if (multiFillerSlots == null) {
			if (other.multiFillerSlots != null)
				return false;
		} else if (!multiFillerSlots.equals(other.multiFillerSlots))
			return false;
		if (singleFillerSlots == null) {
			if (other.singleFillerSlots != null)
				return false;
		} else if (!singleFillerSlots.equals(other.singleFillerSlots))
			return false;
		return true;
	}

	@Override
	public Score compare(EntityTemplate other) {

		final Score score = new Score();

		if (other == null) {
			score.fn++;
		} else {
			score.add(this.entityType.compare(other.getEntityType()));
		}

		addScoresForSingleFillerSlots(other, score);

		addScoresForMultiFillerSlots(other, score);

		return score;

	}

	private void addScoresForSingleFillerSlots(EntityTemplate other, final Score score) {
		for (SlotType singleSlotType : this.singleFillerSlots.keySet()) {

			final SingleFillerSlot singleSlotFiller = this.getSingleFillerSlot(singleSlotType);

			final SingleFillerSlot otherSingleSlotFiller;

			if (other != null && other.containsSingleFillerSlot(singleSlotType))
				otherSingleSlotFiller = other.getSingleFillerSlot(singleSlotType);
			else
				otherSingleSlotFiller = null;

			if (singleSlotFiller.containsSlotFiller()) {
				final AbstractSlotFiller<?> val = singleSlotFiller.getSlotFiller();
				final AbstractSlotFiller<?> otherVal = otherSingleSlotFiller == null ? null
						: otherSingleSlotFiller.getSlotFiller();

				score.add(score(val, otherVal));
			} else if (otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
				score.fp++;
			}

		}
	}

	private void addScoresForMultiFillerSlots(EntityTemplate other, final Score score) {
		for (SlotType multiSlotType : this.multiFillerSlots.keySet()) {

			final MultiFillerSlot slotFiller = this.getMultiFillerSlot(multiSlotType);
			final MultiFillerSlot otherSlotFiller;

			if (other != null && other.containsMultiFillerSlot(multiSlotType))
				otherSlotFiller = other.getMultiFillerSlot(multiSlotType);
			else
				otherSlotFiller = null;

			if (!slotFiller.containsSlotFiller() && (otherSlotFiller == null || !otherSlotFiller.containsSlotFiller()))
				continue;

			final Score bestScore = new Score();

			final int maxSize = otherSlotFiller == null ? slotFiller.size()
					: Math.max(slotFiller.size(), otherSlotFiller.size());

			final Score[][] scores = computeScores(slotFiller, otherSlotFiller, maxSize);

			Score.getPermutationStream(maxSize).filter(indexPermutation -> {
				final Score sum = new Score();

				for (int index = 0; index < indexPermutation.size(); index++) {
					final int permIndex = indexPermutation.get(index).intValue();
					sum.add(scores[index][permIndex]);
				}

				final double f1 = sum.getF1();

				if (bestScore.getF1() <= f1) {
					bestScore.set(sum);
				}

				return f1 == 1;

			}).findFirst();

			score.add(bestScore);
		}
	}

	private Score[][] computeScores(final MultiFillerSlot slotFiller, final MultiFillerSlot otherSlotFiller,
			final int maxSize) {

		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractSlotFiller<?>> slotFillerIterator = slotFiller.getSlotFiller().iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractSlotFiller<?> slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractSlotFiller<?>> otherSlotFillerIterator = otherSlotFiller.getSlotFiller().iterator();

			while (j != maxSize) {

				final AbstractSlotFiller<?> otherSlotFillerVal;
				if (otherSlotFillerIterator.hasNext()) {
					otherSlotFillerVal = otherSlotFillerIterator.next();
				} else {
					otherSlotFillerVal = null;
				}
				if (slotFillerVal == null) {
					scores[i][j] = score(otherSlotFillerVal, slotFillerVal).invert();
				} else {
					scores[i][j] = score(slotFillerVal, otherSlotFillerVal);
				}
				j++;
			}
			i++;
		}

		return scores;
	}

	private Score score(final AbstractSlotFiller<?> val, final AbstractSlotFiller<?> otherVal) {
		if (val instanceof DocumentLink && (otherVal instanceof DocumentLink || otherVal == null)) {
			return ((DocumentLink) val).compare((DocumentLink) otherVal);
		} else if (val instanceof Literal && (otherVal instanceof Literal || otherVal == null)) {
			return ((Literal) val).compare((Literal) otherVal);
		} else if (val instanceof EntityType && (otherVal instanceof EntityType || otherVal == null)) {
			return ((EntityType) val).compare((EntityType) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).compare((EntityTemplate) otherVal);
		} else {
			return Score.INCORRECT;
		}
	}

	@Override
	public EntityType getEntityType() {
		return entityType;
	}

}
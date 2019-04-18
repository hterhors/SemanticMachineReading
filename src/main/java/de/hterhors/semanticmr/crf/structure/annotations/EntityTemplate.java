package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.MultiFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.eval.EEvaluationMode;
import de.hterhors.semanticmr.eval.EvaluationHelper;
import de.hterhors.semanticmr.exce.IllegalSlotFillerException;
import de.hterhors.semanticmr.exce.UnkownMultiSlotException;
import de.hterhors.semanticmr.exce.UnkownSingleSlotException;

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
	private final EntityTypeAnnotation rootAnnotation;

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
	public EntityTemplate(EntityTypeAnnotation entityType) {
		this.rootAnnotation = entityType;
		this.singleFillerSlots = Collections.unmodifiableMap(this.rootAnnotation.entityType.getSingleFillerSlotTypes()
				.stream().map(slotType -> new SingleFillerSlot(slotType))
				.collect(Collectors.toMap(s -> s.slotType, s -> s)));

		this.multiFillerSlots = Collections.unmodifiableMap(this.rootAnnotation.entityType.getMultiFillerSlotTypes()
				.stream().map(slotType -> new MultiFillerSlot(slotType))
				.collect(Collectors.toMap(s -> s.slotType, s -> s)));

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
	private EntityTemplate(EntityTypeAnnotation entityType, Map<SlotType, SingleFillerSlot> singleFillerSlots,
			Map<SlotType, MultiFillerSlot> multiFillerSlots) {
		this.rootAnnotation = entityType;
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

	public EntityTemplate setSingleSlotFiller(SlotType slotType,
			final AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller) {

		if (slotFiller == null)
			return this;

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");

		getSingleFillerSlot(slotType).set(slotFiller);

		return this;
	}

	public void updateMultiFillerSlot(SlotType slotType, AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller,
			AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFillerCandidate) {

		if (slotFillerCandidate == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (getMultiFillerSlot(slotType).containsSlotFiller(slotFillerCandidate)) {
			System.out.println("WARN: can not add same object twice: " + slotFiller.toPrettyString());
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");
		}

		getMultiFillerSlot(slotType).replace(slotFiller, slotFillerCandidate);
	}

	public void addMultiSlotFiller(SlotType slotType,
			final AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller) {

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (getMultiFillerSlot(slotType).containsSlotFiller(slotFiller)) {
			System.out.println("WARN: can not add same object twice: " + slotFiller.toPrettyString());
			throw new IllegalSlotFillerException("Can not update slot .\"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");
		}

		getMultiFillerSlot(slotType).add(slotFiller);
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

		sb.append(rootAnnotation.toPrettyString());
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
				+ ", entityType=" + rootAnnotation + "]";
	}

	@Override
	public EntityTemplate deepCopy() {
		return new EntityTemplate(rootAnnotation,
				this.singleFillerSlots.entrySet().stream()
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())),
				this.multiFillerSlots.entrySet().stream()
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())));
	}

	/**
	 * Creates a deep copy of this entity template but changes the template root
	 * annotation. Overlapping slots are deep copied. New slots are empty. Vanishing
	 * slots are removed.
	 * 
	 * @param newTemplateType
	 * @return
	 */
	public EntityTemplate deepMergeCopy(EntityTypeAnnotation newTemplateType) {
		return new EntityTemplate(newTemplateType.deepCopy(),
				this.singleFillerSlots.entrySet().stream()
						.filter(s -> newTemplateType.entityType.containsSlotType(s.getKey()))
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())),
				this.multiFillerSlots.entrySet().stream()
						.filter(s -> newTemplateType.entityType.containsSlotType(s.getKey()))
						.collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue().deepCopy())));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootAnnotation == null) ? 0 : rootAnnotation.hashCode());
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
		if (rootAnnotation == null) {
			if (other.rootAnnotation != null)
				return false;
		} else if (rootAnnotation != other.rootAnnotation)
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
	public Score evaluate(EEvaluationMode evaluationMode, EntityTemplate other) {

		final Score score = new Score();

		if (other == null) {
			score.increaseFalseNegative();
		} else {
			score.add(this.rootAnnotation.evaluate(evaluationMode, other.rootAnnotation));
		}

		addScoresForSingleFillerSlots(evaluationMode, other, score);

		addScoresForMultiFillerSlots(evaluationMode, other, score);

		return score;

	}

	private void addScoresForSingleFillerSlots(EEvaluationMode evaluationMode, EntityTemplate other,
			final Score score) {
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

				score.add(EvaluationHelper.scoreSingle(evaluationMode, val, otherVal));
			} else if (otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
				score.increaseFalsePositive();
			}

		}
	}

	private void addScoresForMultiFillerSlots(EEvaluationMode evaluationMode, EntityTemplate other, final Score score) {

		for (SlotType multiSlotType : this.multiFillerSlots.keySet()) {

			final Set<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFiller = this
					.getMultiFillerSlot(multiSlotType).getSlotFiller();
			final Set<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> otherSlotFiller;

			if (other != null && other.containsMultiFillerSlot(multiSlotType))
				otherSlotFiller = other.getMultiFillerSlot(multiSlotType).getSlotFiller();
			else
				otherSlotFiller = Collections.emptySet();

			if (slotFiller.isEmpty() && (otherSlotFiller == null || otherSlotFiller.isEmpty()))
				continue;

			final Score bestScore = EvaluationHelper.scoreMax(evaluationMode, slotFiller, otherSlotFiller);

			score.add(bestScore);
		}
	}

	@Override
	public EntityType getEntityType() {
		return rootAnnotation.entityType;
	}

	public EntityTypeAnnotation getRootAnnotation() {
		return rootAnnotation;
	}

	@Override
	public EntityTemplateAnnotationFilter.Builder filter() {
		return new EntityTemplateAnnotationFilter.Builder(this);
	}

}
package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.exce.IllegalSlotFillerException;
import de.hterhors.semanticmr.exce.UnkownMultiSlotException;
import de.hterhors.semanticmr.exce.UnkownSingleSlotException;

/**
 * The information extraction template that needs to be filled.
 * 
 * @author hterhors
 *
 */
final public class EntityTemplate extends AbstractAnnotation {

	public static boolean includeRootForEvaluation = true;

	/**
	 * Defines the entity type of this annotation.
	 */
	private EntityTypeAnnotation rootAnnotation;

	/**
	 * A map of slots to fill.
	 */
	final private Map<SlotType, SingleFillerSlot> singleFillerSlots;

	/**
	 * A map of multi filler slots to fill.
	 */
	final private Map<SlotType, MultiFillerSlot> multiFillerSlots;

	/**
	 * Creates a new template entity of the given entity type.
	 * 
	 * @param entityType
	 */
	public EntityTemplate(EntityType entityType) {
		this(AnnotationBuilder.toAnnotation(entityType));
	}

	/**
	 * Creates a new template entity of the given entity type.
	 * 
	 * @param entityType
	 */
	public EntityTemplate(EntityTypeAnnotation entityType) {
		this.rootAnnotation = entityType;
		this.singleFillerSlots = new HashMap<>();
		this.multiFillerSlots = new HashMap<>();
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

	public Stream<AbstractAnnotation> streamSingleFillerSlotValues() {
		return this.singleFillerSlots.entrySet().stream().filter(e -> !e.getKey().isExcluded())
				.map(s -> s.getValue().getSlotFiller());
	}

	public Stream<AbstractAnnotation> flatStreamMultiFillerSlotValues() {
		return this.multiFillerSlots.entrySet().stream().filter(e -> !e.getKey().isExcluded())
				.flatMap(s -> s.getValue().getSlotFiller().stream());
	}

	public boolean hasSlotOfType(SlotType slotType) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		return getEntityType().containsSlotType(slotType);
	}

	public SingleFillerSlot getSingleFillerSlot(SlotType slotType) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		if (!getEntityType().containsSlotType(slotType))
			throw new UnkownSingleSlotException("The requested single filler slot is unkown: " + slotType
					+ " for entity type: " + getEntityType().name);

		return singleFillerSlots.getOrDefault(slotType, new SingleFillerSlot(slotType));
	}

	public SingleFillerSlot getSingleFillerSlotOfName(String slotType) {
		return getSingleFillerSlot(SlotType.get(slotType));
	}

	public EntityTemplate setSingleSlotFiller(SlotType slotType, final AbstractAnnotation slotFiller) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		if (!slotType.isSingleValueSlot())
			throw new UnkownMultiSlotException("The requested single filler slot is actually a multi filler slot: "
					+ slotType + " for entity: " + getEntityType().name);

		if (slotFiller == null)
			return this;

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (!slotType.matchesEntityType(slotFiller.getEntityType()))
			throw new IllegalSlotFillerException("Can not update slot \"" + slotType.toPrettyString()
					+ "\" with slot filler: \"" + slotFiller.toPrettyString() + "\"");

		this.singleFillerSlots.put(slotType, new SingleFillerSlot(slotType).set(slotFiller));

		return this;
	}

	public void updateMultiFillerSlot(SlotType slotType, AbstractAnnotation slotFiller,
			AbstractAnnotation slotFillerCandidate) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		if (slotType.isSingleValueSlot())
			throw new UnkownMultiSlotException("The requested multi filler slot is actually a single filler slot: "
					+ slotType + " for entity: " + getEntityType().name);

		if (slotFillerCandidate == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (!getEntityType().containsSlotType(slotType))
			throw new UnkownMultiSlotException(
					"The requested multi filler slot is unkown: " + slotType + " for entity: " + getEntityType().name);

		MultiFillerSlot mfs;
		if ((mfs = multiFillerSlots.get(slotType)) != null) {
			mfs.replace(slotFiller, slotFillerCandidate);
		} else {
			multiFillerSlots.put(slotType, new MultiFillerSlot(slotType).add(slotFillerCandidate));
		}

	}

	/**
	 * Adds a new value to the slot without checking, whether the set already
	 * contains this value or not.
	 * 
	 * @param slotType
	 * @param slotFiller
	 */
	public void addMultiSlotFiller(SlotType slotType, final AbstractAnnotation slotFiller) {

		if (slotType.isSingleValueSlot())
			throw new UnkownMultiSlotException("The requested multi filler slot is actually a single filler slot: "
					+ slotType + " for entity: " + getEntityType().name);

		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		if (slotFiller == this)
			throw new IllegalSlotFillerException("Can not put itself as slot filler of itself.");

		if (!getEntityType().containsSlotType(slotType))
			throw new UnkownMultiSlotException(
					"The requested multi filler slot is unkown: " + slotType + " for entity: " + getEntityType().name);

		MultiFillerSlot mfs;

		if ((mfs = multiFillerSlots.get(slotType)) != null) {
			mfs.add(slotFiller);
		} else {
			multiFillerSlots.put(slotType, new MultiFillerSlot(slotType).add(slotFiller));
		}

	}

	public MultiFillerSlot getMultiFillerSlot(SlotType slotType) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested multi filler slot was excluded: " + slotType);

		if (slotType.isSingleValueSlot())
			throw new UnkownMultiSlotException("The requested multi filler slot is actually a single filler slot: "
					+ slotType + " for entity: " + getEntityType().name);

		if (!getEntityType().containsSlotType(slotType))
			throw new UnkownMultiSlotException(
					"The requested multi filler slot is unkown: " + slotType + " for entity: " + getEntityType().name);

		return multiFillerSlots.getOrDefault(slotType, new MultiFillerSlot(slotType));
	}

	public MultiFillerSlot getMultiFillerSlotByName(String slotTypeName) {
		return getMultiFillerSlot(SlotType.get(slotTypeName));
	}

	public Map<SlotType, SingleFillerSlot> getSingleFillerSlots() {
		return Collections.unmodifiableMap(singleFillerSlots);
	}

	public Map<SlotType, MultiFillerSlot> getMultiFillerSlots() {
		return Collections.unmodifiableMap(multiFillerSlots);
	}

	@Override
	public String toPrettyString(final int depth) {
		final StringBuilder sb = new StringBuilder();
		final int newDepth = depth + 1;

		sb.append(rootAnnotation.toPrettyString());
		sb.append("\n");
		for (SingleFillerSlot slot : singleFillerSlots.values()) {
			if (slot.containsSlotFiller() && !slot.slotType.isExcluded()) {
				for (int d = 0; d < newDepth; d++) {
					sb.append("\t");
				}
				sb.append(slot.toPrettyString(newDepth)).append("\n");
			}
		}
		for (MultiFillerSlot slot : multiFillerSlots.values()) {
			if (slot.containsSlotFiller() && !slot.slotType.isExcluded()) {
				for (int d = 0; d < newDepth; d++) {
					sb.append("\t");
				}
				sb.append(" [").append(slot.getSlotFiller().size()).append("]");
				sb.append(slot.toPrettyString(newDepth)).append("\n");
			}
		}
		return sb.toString().trim();
	}

	@Override
	public String toString() {
		return "EntityTemplate [entityType=" + rootAnnotation + ", singleFillerSlots=" + singleFillerSlots
				+ ", multiFillerSlots=" + multiFillerSlots + "]";
	}

	@SuppressWarnings("unchecked")
	@Override
	public EntityTemplate deepCopy() {

		/**
		 * An map of slots to fill.
		 */
		final Map<SlotType, SingleFillerSlot> singleFillerSlots = new HashMap<>();
		for (Entry<SlotType, SingleFillerSlot> e : this.singleFillerSlots.entrySet()) {
			singleFillerSlots.put(e.getKey(), e.getValue().deepCopy());
		}
		final Map<SlotType, MultiFillerSlot> multiFillerSlots = new HashMap<>();
		for (Entry<SlotType, MultiFillerSlot> e : this.multiFillerSlots.entrySet()) {
			multiFillerSlots.put(e.getKey(), e.getValue().deepCopy());
		}

		/**
		 * An unmodifiable map of slots to fill.
		 */
		return new EntityTemplate(rootAnnotation.deepCopy(), singleFillerSlots, multiFillerSlots);
	}

	/**
	 * Creates a deep copy of this entity template but changes the template root
	 * annotation. Overlapping slots are deeply copied. New slots are empty.
	 * Vanishing slots are removed.
	 * 
	 * @param newTemplateType
	 * @return
	 */
	public EntityTemplate deepMergeCopy(EntityTypeAnnotation newTemplateType) {

		final Map<SlotType, SingleFillerSlot> singleFillerSlots = new HashMap<>();

		final Map<SlotType, MultiFillerSlot> multiFillerSlots = new HashMap<>();

		/*
		 * Deep copy of existing and matching slots.
		 */
		this.singleFillerSlots.entrySet().stream().filter(s -> newTemplateType.entityType.containsSlotType(s.getKey()))
				.forEach(s -> singleFillerSlots.put(s.getKey(), s.getValue().deepCopy()));

		this.multiFillerSlots.entrySet().stream().filter(s -> newTemplateType.entityType.containsSlotType(s.getKey()))
				.forEach(s -> multiFillerSlots.put(s.getKey(), s.getValue().deepCopy()));

		return new EntityTemplate(newTemplateType.deepCopy(), singleFillerSlots, multiFillerSlots);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((multiFillerSlots == null) ? 0 : multiFillerSlots.hashCode());
		result = prime * result + ((rootAnnotation == null) ? 0 : rootAnnotation.hashCode());
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
		if (multiFillerSlots == null) {
			if (other.multiFillerSlots != null)
				return false;
		} else if (!multiFillerSlots.equals(other.multiFillerSlots))
			return false;
		if (rootAnnotation == null) {
			if (other.rootAnnotation != null)
				return false;
		} else if (!rootAnnotation.equals(other.rootAnnotation))
			return false;
		if (singleFillerSlots == null) {
			if (other.singleFillerSlots != null)
				return false;
		} else if (!singleFillerSlots.equals(other.singleFillerSlots))
			return false;
		return true;
	}

//	@Override
//	public void evaluateDetailed(EEvaluationDetail evaluationDetail, IEvaluatable otherVal,
//			Map<EntityType, Score> detailedScore) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void evaluateDetailed(AbstractEvaluator evaluator, IEvaluatable other,
//			Map<EntityType, Score> detailedScore) {
//
//		EntityTemplate oet = null;
//
//		if (other == null) {
//			detailedScore.get(getEntityType()).increaseFalseNegative();
////			score.increaseFalseNegative();
//		} else {
//			if (!(other instanceof EntityTemplate)) {
//				detailedScore.get(getEntityType()).add(this.rootAnnotation.evaluate(evaluator, other));
////				score.add(this.rootAnnotation.evaluate(evaluator, other));
//			} else {
//				oet = (EntityTemplate) other;
//
//				/**
//				 * TODO: REMOVE QUICK FIX
//				 */
//				if (oet.rootAnnotation.isInstanceOfEntityTypeAnnotation()
//						&& (oet.getEntityType().name.equals("CompoundTreatment")
//								|| oet.getEntityType().getDirectSuperEntityTypes().isEmpty())) {
//
//				} else {
//					detailedScore.get(getEntityType()).add(this.rootAnnotation.evaluate(evaluator, oet.rootAnnotation));
//				}
//			}
//		}
//
//		scoreDetailedSingleFillerSlots(evaluator, oet, detailedScore);
////
//		scoreDetailedMultiFillerSlots(evaluator, oet, detailedScore);
//
//	}
//
//	private void scoreDetailedSingleFillerSlots(AbstractEvaluator evaluator, EntityTemplate other,
//			Map<EntityType, Score> detailedScore) {
//		for (SlotType singleSlotType : getSingleFillerSlotTypes()) {
//
//			if (singleSlotType.isExcluded())
//				continue;
//
//			final SingleFillerSlot otherSingleSlotFiller;
//
//			if (other != null && other.hasSlotOfType(singleSlotType))
//				otherSingleSlotFiller = other.getSingleFillerSlot(singleSlotType);
//			else
//				otherSingleSlotFiller = null;
//
//			final SingleFillerSlot singleSlotFiller;
//			AbstractAnnotation val = null;
//			if ((singleSlotFiller = this.singleFillerSlots.get(singleSlotType)) != null)
//				val = singleSlotFiller.getSlotFiller();
//
//			final AbstractAnnotation otherVal = otherSingleSlotFiller == null ? null
//					: otherSingleSlotFiller.getSlotFiller();
//
//			if (val == null && otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
////				score.increaseFalsePositive();
//				detailedScore.get(otherSingleSlotFiller.getSlotFiller().getEntityType()).increaseFalsePositive();
//			} else {
//				evaluator.scoreDetailedSingle(val, otherVal, detailedScore);
//			}
//
//		}
//	}
//
//	private void scoreDetailedMultiFillerSlots(AbstractEvaluator evaluator, EntityTemplate other,
//			Map<EntityType, Score> detailedScore) {
//
//		for (SlotType multiSlotType : getMultiFillerSlotTypes()) {
//
//			if (multiSlotType.isExcluded())
//				continue;
//
//			final Set<AbstractAnnotation> otherSlotFiller;
//
//			if (other != null && other.hasSlotOfType(multiSlotType))
//				otherSlotFiller = other.getMultiFillerSlot(multiSlotType).getSlotFiller();
//			else
//				otherSlotFiller = Collections.emptySet();
//
//			final Set<AbstractAnnotation> slotFiller;
//			MultiFillerSlot mfs;
//			if ((mfs = this.multiFillerSlots.get(multiSlotType)) != null) {
//				slotFiller = mfs.getSlotFiller();
//			} else {
//				slotFiller = Collections.emptySet();
//
//				if (otherSlotFiller == null || otherSlotFiller.isEmpty())
//					continue;
//
//			}
//
//			evaluator.scoreDetailedMultiValues(slotFiller, otherSlotFiller, EScoreType.MICRO, detailedScore);
////			final Score bestScore = evaluator.scoreDetailedMultiValues(slotFiller, otherSlotFiller, EScoreType.MICRO,
////					detailedScore);
//
////			score.add(bestScore);
//		}
//	}

	@Override
	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable other) {

		final Score score = new Score();

		EntityTemplate oet = null;

		if (other == null) {
			score.increaseFalseNegative();
		} else {
			if (!(other instanceof EntityTemplate)) {
				Score s = this.rootAnnotation.evaluate(evaluator, other);
				score.add(s);
			} else {
				oet = (EntityTemplate) other;

				/**
				 * TODO: REMOVE QUICK FIX
				 */
				if (oet.rootAnnotation.isInstanceOfEntityTypeAnnotation()
						&& (oet.getEntityType().name.equals("CompoundTreatment")
								|| oet.getEntityType().getDirectSuperEntityTypes().isEmpty())) {

				} else {
					Score s = this.rootAnnotation.evaluate(evaluator, oet.rootAnnotation);
					score.add(s);
				}
			}
		}

		score.add(scoreSingleFillerSlots(evaluator, oet));

		score.add(scoreMultiFillerSlots(evaluator, oet));

		return score;

	}

	private Score scoreSingleFillerSlots(AbstractEvaluator evaluator, EntityTemplate other) {
		final Score score = new Score();
		for (SlotType singleSlotType : getSingleFillerSlotTypes()) {

			if (singleSlotType.isExcluded())
				continue;

			final SingleFillerSlot otherSingleSlotFiller;

			if (other != null && other.hasSlotOfType(singleSlotType))
				otherSingleSlotFiller = other.getSingleFillerSlot(singleSlotType);
			else
				otherSingleSlotFiller = null;

			final SingleFillerSlot singleSlotFiller;
			AbstractAnnotation val = null;
			if ((singleSlotFiller = this.singleFillerSlots.get(singleSlotType)) != null)
				val = singleSlotFiller.getSlotFiller();

			final AbstractAnnotation otherVal = otherSingleSlotFiller == null ? null
					: otherSingleSlotFiller.getSlotFiller();

			if (val == null && otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
				score.increaseFalsePositive();

			} else {
				Score s = evaluator.scoreSingle(val, otherVal);
				score.add(s);
			}

//			if ((singleSlotFiller = this.singleFillerSlots.get(singleSlotType)) != null) {
//				final AbstractAnnotation val = singleSlotFiller.getSlotFiller();
//				final AbstractAnnotation otherVal = otherSingleSlotFiller == null ? null
//						: otherSingleSlotFiller.getSlotFiller();
//				
//				score.add(evaluator.scoreSingle(val, otherVal));
//			} else if (otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
//				score.increaseFalsePositive();
//			}

		}
		return score;
	}

	private Score scoreMultiFillerSlots(AbstractEvaluator evaluator, EntityTemplate other) {
		final Score score = new Score();

		for (SlotType multiSlotType : getMultiFillerSlotTypes()) {

			if (multiSlotType.isExcluded())
				continue;

			final Set<AbstractAnnotation> otherSlotFiller;

			if (other != null && other.hasSlotOfType(multiSlotType))
				otherSlotFiller = other.getMultiFillerSlot(multiSlotType).getSlotFiller();
			else
				otherSlotFiller = Collections.emptySet();

			final Set<AbstractAnnotation> slotFiller;
			MultiFillerSlot mfs;
			if ((mfs = this.multiFillerSlots.get(multiSlotType)) != null) {
				slotFiller = mfs.getSlotFiller();
			} else {
				slotFiller = Collections.emptySet();

				if (otherSlotFiller == null || otherSlotFiller.isEmpty())
					continue;

			}

			final Score bestScore = evaluator.scoreMultiValues(slotFiller, otherSlotFiller, EScoreType.MICRO);

			score.add(bestScore);
		}
		return score;
	}

	@Override
	public boolean evaluateEquals(AbstractEvaluator evaluator, IEvaluatable other) {

		if (this == other)
			return true;

		if (other == null)
			return false;

		final EntityTemplate oet;
		if (!(other instanceof EntityTemplate)) {
			if (!this.rootAnnotation.evaluateEquals(evaluator, other))
				return false;
			oet = null;
		} else {
			oet = (EntityTemplate) other;
			if (!this.rootAnnotation.evaluateEquals(evaluator, oet.rootAnnotation))
				return false;

		}

		if (!evalEqualsSingleFillerSlots(evaluator, oet))
			return false;

		if (!evalEqualsScoreMultiFillerSlots(evaluator, oet))
			return false;

		return true;
	}

	private boolean evalEqualsSingleFillerSlots(AbstractEvaluator evaluator, EntityTemplate other) {
		for (SlotType singleSlotType : getSingleFillerSlotTypes()) {

			if (singleSlotType.isExcluded())
				continue;

			final SingleFillerSlot otherSingleSlotFiller;

			if (other != null && other.hasSlotOfType(singleSlotType))
				otherSingleSlotFiller = other.getSingleFillerSlot(singleSlotType);
			else
				otherSingleSlotFiller = null;

			final SingleFillerSlot singleSlotFiller;
			if ((singleSlotFiller = this.singleFillerSlots.get(singleSlotType)) != null) {

				final AbstractAnnotation val = singleSlotFiller.getSlotFiller();
				final AbstractAnnotation otherVal = otherSingleSlotFiller == null ? null
						: otherSingleSlotFiller.getSlotFiller();

				if (!evaluator.evalEqualsSingle(val, otherVal))
					return false;

			} else if (otherSingleSlotFiller != null && otherSingleSlotFiller.containsSlotFiller()) {
				return false;
			}

		}
		return true;
	}

	private boolean evalEqualsScoreMultiFillerSlots(AbstractEvaluator evaluator, EntityTemplate other) {

		for (SlotType multiSlotType : getMultiFillerSlotTypes()) {

			if (multiSlotType.isExcluded())
				continue;

			final Set<AbstractAnnotation> otherSlotFiller;

			if (other != null && other.hasSlotOfType(multiSlotType))
				otherSlotFiller = other.getMultiFillerSlot(multiSlotType).getSlotFiller();
			else
				otherSlotFiller = Collections.emptySet();

			final Set<AbstractAnnotation> slotFiller;
			MultiFillerSlot mfs;
			if ((mfs = this.multiFillerSlots.get(multiSlotType)) != null) {
				slotFiller = mfs.getSlotFiller();
			} else {
				slotFiller = Collections.emptySet();

				if (otherSlotFiller == null || otherSlotFiller.isEmpty())
					continue;

			}

			if (!evaluator.evalEqualsMultiValues(slotFiller, otherSlotFiller))
				return false;
		}
		return true;
	}

	@Override
	public EntityType getEntityType() {
		return rootAnnotation.entityType;
	}

	public EntityTypeAnnotation getRootAnnotation() {
		return rootAnnotation;
	}

	/**
	 * Reduces the root annotation of e.g. DocLinkedAnnotation to EntityType. This
	 * is an irreversible process.
	 */
	public void reduceRootToEntityType() {
		rootAnnotation = new EntityTypeAnnotation(rootAnnotation.entityType);
	}

	public EntityTemplateAnnotationFilter.Builder filter() {
		return new EntityTemplateAnnotationFilter.Builder(this);
	}

	/**
	 * Clears all slots from this entity temple. Note that the root annotation
	 * remains.
	 */
	public EntityTemplate clearAllSlots() {
		this.singleFillerSlots.clear();
		this.multiFillerSlots.clear();
		return this;
	}

	@Override
	public Score evaluate(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		throw new IllegalStateException("Can not evaluate entity templates without evaluator.");
	}

	@Override
	public boolean evaluateEquals(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		throw new IllegalStateException("Can not evaluateEquals entity templates without evaluator.");
	}

	/**
	 * Returns true if this template has no slot filler for any slot.
	 * 
	 * @return true if there no slot is filled with any value.
	 */
	public boolean isEmpty() {
		return this.singleFillerSlots.isEmpty() && this.multiFillerSlots.isEmpty();
	}

	public List<SlotType> getSingleFillerSlotTypes() {
		return getEntityType().getSingleFillerSlotTypes();
	}

	public List<SlotType> getMultiFillerSlotTypes() {
		return getEntityType().getMultiFillerSlotTypes();
	}

	/**
	 * Removes the slot filler or all slot filler values from this template for the
	 * given slot type.
	 * 
	 * @param slotTypeName
	 */
	public void clearSlot(SlotType slotType) {
//		if (slotType.exclude)
//			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		if (!getEntityType().containsSlotType(slotType))
			throw new UnkownMultiSlotException(
					"The requested slot is unkown: " + slotType + " for entity: " + getEntityType().name);
		this.singleFillerSlots.remove(slotType);
		this.multiFillerSlots.remove(slotType);
	}

	/**
	 * Removes the slot filler or all slot filler values from this template for the
	 * given slot type.
	 * 
	 * @param slotTypeName
	 */
	public void clearSlotOfName(String slotTypeName) {
		clearSlot(SlotType.get(slotTypeName));
	}

	public void removeMultiFillerSlotFiller(SlotType slotType, AbstractAnnotation slotFiller) {
		if (slotType.isExcluded())
			throw new UnkownSingleSlotException("The requested single filler slot was excluded: " + slotType);

		this.multiFillerSlots.get(slotType).removeSlotFiller(slotFiller);
	}

}

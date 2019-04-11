package de.hterhors.semanticmr.structure.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hterhors.semanticmr.exce.UnkownEnityTypeException;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;
import de.hterhors.semanticmr.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.structure.annotations.normalization.INormalizationFunction;
import de.hterhors.semanticmr.structure.annotations.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.structure.annotations.normalization.IdentityNormalization;
import de.hterhors.semanticmr.structure.slots.SlotType;

final public class EntityType extends AbstractSlotFiller<EntityType>
		implements Comparable<EntityType>, IRequiresInitialization {
	/**
	 * The name of this entity type.
	 */
	final public String entityTypeName;

	/**
	 * Whether this entity type is of type literal.
	 */
	final public boolean isLiteral;

	/**
	 * A sorted unmodifiable list of slots of that entity type.
	 */
	private Set<SlotType> slotTypes;

	/**
	 * View of the unmodifiable list of all single annotation slot types of that
	 * template.
	 */
	private List<SlotType> singleAnnotationSlotTypes;

	/**
	 * View of the unmodifiable list of all multi annotation slot types of that
	 * entity type.
	 */
	private List<SlotType> multiAnnotationSlotTypes;

	/**
	 * A set of slot types where this entity type is a potential slot filler.
	 */
	private Set<SlotType> slotFillerOfSlotTypes;

	private Set<EntityType> superEntityTypes;

	private Set<EntityType> subEntityTypes;

	/**
	 * A set of entity types that are either sub entities or super entities;
	 */
	private Set<EntityType> relatedEntityTypes;

	/**
	 * A sorted unmodifiable list of slots of that entity type.
	 */
	final private List<String> slotTypeNames;

	final private Set<String> superEntityTypeNames;

	final private Set<String> subEntityTypeNames;

	private INormalizationFunction normalizationFunction = IdentityNormalization.getInstance();

	private Set<String> slotFillerOfSlotTypeNames;

	private EntityType() {
		this.entityTypeName = null;
		this.slotTypes = Collections.emptySet();
		this.singleAnnotationSlotTypes = Collections.emptyList();
		this.multiAnnotationSlotTypes = Collections.emptyList();
		this.superEntityTypeNames = Collections.emptySet();
		this.subEntityTypeNames = Collections.emptySet();
		this.slotTypeNames = Collections.emptyList();
		this.isLiteral = false;
	}

	private EntityType(final String internalizedEntityTypeName, StructureSpecification specifications) {
		this.entityTypeName = internalizedEntityTypeName;
		this.isLiteral = specifications.isLiteralEntityType(internalizedEntityTypeName);
		this.slotTypeNames = Collections.unmodifiableList(specifications.getSlotsForEntityType(this.entityTypeName)
				.stream().sorted().collect(Collectors.toList()));
		this.superEntityTypeNames = Collections.unmodifiableSet(specifications
				.getSuperEntityTypeNames(this.entityTypeName).stream().sorted().collect(Collectors.toSet()));
		this.subEntityTypeNames = Collections.unmodifiableSet(specifications.getSubEntityTypeNames(this.entityTypeName)
				.stream().sorted().collect(Collectors.toSet()));

		this.slotFillerOfSlotTypeNames = specifications
				.getSlotTypeNames().stream().filter(slotType -> specifications
						.getSlotFillerEntityTypeNames(slotType.intern()).contains(internalizedEntityTypeName))
				.collect(Collectors.toSet());
	}

	public Set<SlotType> getSlotTypes() {
		if (slotTypes == null) {
			slotTypes = Collections.unmodifiableSet(this.slotTypeNames.stream()
					.map(slotTypeName -> SlotType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}

		return slotTypes;
	}

	/**
	 * Returns a set of slot types where this entity can be slot filler.
	 * 
	 * @return
	 */
	public Set<SlotType> getSlotFillerOfSlotTypes() {
		if (slotFillerOfSlotTypes == null) {
			slotFillerOfSlotTypes = Collections.unmodifiableSet(this.slotFillerOfSlotTypeNames.stream()
					.map(slotTypeName -> SlotType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return slotFillerOfSlotTypes;
	}

	public List<SlotType> getSingleFillerSlotTypes() {
		if (singleAnnotationSlotTypes == null) {
			this.singleAnnotationSlotTypes = Collections.unmodifiableList(
					getSlotTypes().stream().filter(slot -> slot.isSingleValueSlot).collect(Collectors.toList()));
		}

		return singleAnnotationSlotTypes;
	}

	public List<SlotType> getMultiFillerSlotTypes() {
		if (multiAnnotationSlotTypes == null) {
			this.multiAnnotationSlotTypes = Collections.unmodifiableList(
					getSlotTypes().stream().filter(slot -> !slot.isSingleValueSlot).collect(Collectors.toList()));
		}
		return multiAnnotationSlotTypes;
	}

	private Set<EntityType> getSuperEntityTypes() {
		if (superEntityTypes == null) {
			superEntityTypes = Collections.unmodifiableSet(this.superEntityTypeNames.stream()
					.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return superEntityTypes;
	}

	public Set<EntityType> getSubEntityTypes() {
		if (subEntityTypes == null) {
			subEntityTypes = Collections.unmodifiableSet(this.subEntityTypeNames.stream()
					.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return subEntityTypes;
	}

	public Set<EntityType> getRelatedEntityTypes() {
		if (relatedEntityTypes == null) {
			relatedEntityTypes = Collections
					.unmodifiableSet(Stream.concat(this.subEntityTypeNames.stream(), this.superEntityTypeNames.stream())
							.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return relatedEntityTypes;
	}

	private final static Map<String, EntityType> entityTypeFactory = new HashMap<>();

	public static EntityType get(final String entityTypeName) {

		final String internalized = entityTypeName.intern();

		EntityType entityType;

		if ((entityType = entityTypeFactory.get(internalized)) == null) {
			throw new UnkownEnityTypeException(
					"The requested entity type is unkown. It is not part of the specifications: " + entityTypeName);
		}
		return entityType;

	}

	/**
	 * Checks recursively if the parameterized entity type is a sub entity of this
	 * entity type.
	 * 
	 * @param subEntityType
	 * @return true if
	 */
	public boolean isSuperEntityOf(EntityType subEntityType) {
		final Set<EntityType> directSubEntities = getSubEntityTypes();

		if (directSubEntities.contains(subEntityType)) {
			return true;
		}
		for (EntityType entityType : directSubEntities) {
			if (entityType.isSuperEntityOf(subEntityType))
				return true;
		}
		return false;
	}

	public boolean isSubEntityOf(EntityType superEntityType) {
		final Set<EntityType> directSuperEntities = getSuperEntityTypes();

		if (directSuperEntities.contains(superEntityType)) {
			return true;
		}
		for (EntityType entityType : directSuperEntities) {
			if (entityType.isSubEntityOf(superEntityType))
				return true;
		}
		return false;
	}

	public void setNormalizationFunction(INormalizationFunction normalizationFunction) {
		this.normalizationFunction = normalizationFunction;
	}

	public INormalizationFunction getNormalizationFunction() {
		return normalizationFunction;
	}

	private static EntityType initInstance = null;

	public static EntityType getInitializationInstance() {
		if (initInstance == null) {
			initInstance = new EntityType();
		}
		return initInstance;

	}

	@Override
	public void system_init(StructureSpecification specifications) {
		for (String entityTypeName : specifications.getEntityTypeNames()) {
			final String internalized = entityTypeName.intern();

			EntityType entityType;

			if ((entityType = entityTypeFactory.get(internalized)) == null) {
				entityType = new EntityType(internalized, specifications);
				entityTypeFactory.put(internalized, entityType);
			} else {
				throw new UnkownEnityTypeException(
						"Multiple occurrence for entity type " + entityTypeName + " in specifications.");
			}

		}

	}

	@Override
	public int compareTo(EntityType o) {
		return o.entityTypeName.compareTo(this.entityTypeName);
	}

	@Override
	public String toString() {
		return "EntityType [entityTypeName=" + entityTypeName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityTypeName == null) ? 0 : entityTypeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		return false;
	}

	public EntityType deepCopy() {
		return this;
	}

	public String toPrettyString(final int depth) {
		return entityTypeName;
	}

	public boolean containsSlotType(SlotType slotType) {
		return getSlotTypes().contains(slotType);
	}

	/**
	 * Returns true if this entity type has no slots.
	 * 
	 * @return
	 */
	public boolean isLeafEntityType() {
		return getSlotTypes().isEmpty();
	}

	@Override
	public Score evaluate(EntityType otherVal) {
		if (otherVal == null) {
			return Score.FN;
		} else if (equals(otherVal)) {
			return Score.TP;
		} else {
			return Score.FN_FP;
		}
	}

	@Override
	public EntityType getEntityType() {
		return this;
	}

}

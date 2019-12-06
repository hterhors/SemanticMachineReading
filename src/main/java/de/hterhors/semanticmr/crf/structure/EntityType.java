package de.hterhors.semanticmr.crf.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hterhors.semanticmr.crf.structure.annotations.normalization.INormalizationFunction;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IdentityNormalization;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.exce.UnkownEnityTypeException;
import de.hterhors.semanticmr.init.specifications.Specifications;

final public class EntityType implements Comparable<EntityType>, IRequiresInitialization {

	/**
	 * Set of entity types that were registered.
	 */
	private static Set<EntityType> entityTypes = null;

	/**
	 * The name of this entity type.
	 */
	final public String name;

	/**
	 * Whether this entity type is of type literal.
	 */
	final public boolean isLiteral;

	/**
	 * A sorted unmodifiable list of slots of that entity type.
	 */
	private Set<SlotType> slots;

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

	private Set<EntityType> transitiveClosureSuperEntityTypes;

	private Set<EntityType> transitiveClosureSubEntityTypes;

	private Set<EntityType> directSuperEntityTypes;

	private Set<EntityType> directSubEntityTypes;

	/**
	 * A set of entity types that are either sub entities or super entities;
	 */
	private Set<EntityType> relatedEntityTypes;

	/**
	 * A sorted unmodifiable list of slots of that entity type.
	 */
	final private List<String> slotNames;

	final private Set<String> transitiveClosureSuperEntityTypeNames;

	final private Set<String> transitiveClosureSubEntityTypeNames;

	final private Set<String> directSuperEntityTypeNames;

	final private Set<String> directSubEntityTypeNames;

	private INormalizationFunction normalizationFunction = IdentityNormalization.getInstance();

	private Set<String> slotFillerOfSlotTypeNames;

	private EntityType() {
		this.name = null;
		this.slots = Collections.emptySet();
		this.singleAnnotationSlotTypes = Collections.emptyList();
		this.multiAnnotationSlotTypes = Collections.emptyList();
		this.transitiveClosureSuperEntityTypeNames = Collections.emptySet();
		this.transitiveClosureSubEntityTypeNames = Collections.emptySet();
		this.directSuperEntityTypeNames = Collections.emptySet();
		this.directSubEntityTypeNames = Collections.emptySet();
		this.slotNames = Collections.emptyList();
		this.isLiteral = false;
	}

	private EntityType(final String internalizedEntityTypeName, Specifications specifications) {
		this.name = internalizedEntityTypeName;
		this.isLiteral = specifications.isLiteralEntityType(internalizedEntityTypeName);
		this.slotNames = Collections.unmodifiableList(
				specifications.getSlotsForEntityType(this.name).stream().sorted().collect(Collectors.toList()));
		this.transitiveClosureSuperEntityTypeNames = Collections
				.unmodifiableSet(specifications.getTransitiveClosureSuperEntityTypeNames(this.name).stream()
						.sorted().collect(Collectors.toSet()));
		this.transitiveClosureSubEntityTypeNames = Collections.unmodifiableSet(specifications
				.getTransitveClosureSubEntityTypeNames(this.name).stream().sorted().collect(Collectors.toSet()));

		this.directSuperEntityTypeNames = Collections.unmodifiableSet(specifications
				.getDirectSuperEntityTypeNames(this.name).stream().sorted().collect(Collectors.toSet()));
		this.directSubEntityTypeNames = Collections.unmodifiableSet(specifications
				.getDirectSubEntityTypeNames(this.name).stream().sorted().collect(Collectors.toSet()));

		this.slotFillerOfSlotTypeNames = specifications
				.getSlotTypeNames().stream().filter(slotType -> specifications
						.getSlotFillerEntityTypeNames(slotType.intern()).contains(internalizedEntityTypeName))
				.collect(Collectors.toSet());
	}

	public Set<SlotType> getSlots() {
		if (slots == null) {
			slots = Collections.unmodifiableSet(this.slotNames.stream().map(slotTypeName -> SlotType.get(slotTypeName))
					.sorted().collect(Collectors.toSet()));
		}

		return slots;
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
					getSlots().stream().filter(slot -> slot.isSingleValueSlot()).collect(Collectors.toList()));
		}

		return singleAnnotationSlotTypes;
	}

	public List<SlotType> getMultiFillerSlotTypes() {
		if (multiAnnotationSlotTypes == null) {
			this.multiAnnotationSlotTypes = Collections.unmodifiableList(
					getSlots().stream().filter(slot -> !slot.isSingleValueSlot()).collect(Collectors.toList()));
		}
		return multiAnnotationSlotTypes;
	}

	public Set<EntityType> getDirectSuperEntityTypes() {
		if (directSuperEntityTypes == null) {
			directSuperEntityTypes = Collections.unmodifiableSet(this.directSuperEntityTypeNames.stream()
					.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return directSuperEntityTypes;
	}

	public Set<EntityType> getDirectSubEntityTypes() {
		if (directSubEntityTypes == null) {
			directSubEntityTypes = Collections.unmodifiableSet(this.directSubEntityTypeNames.stream()
					.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return directSubEntityTypes;
	}

	public Set<EntityType> getTransitiveClosureSuperEntityTypes() {
		if (transitiveClosureSuperEntityTypes == null) {
			transitiveClosureSuperEntityTypes = Collections.unmodifiableSet(this.transitiveClosureSuperEntityTypeNames
					.stream().map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return transitiveClosureSuperEntityTypes;
	}

	public Set<EntityType> getTransitiveClosureSubEntityTypes() {
		if (transitiveClosureSubEntityTypes == null) {
			transitiveClosureSubEntityTypes = Collections.unmodifiableSet(this.transitiveClosureSubEntityTypeNames
					.stream().map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet()));
		}
		return transitiveClosureSubEntityTypes;
	}

	/**
	 * Returns a set of all entities that are either sub* or super* entities of this
	 * entity. includes this entity itself.
	 * 
	 * @return
	 */
	public Set<EntityType> getHierarchicalEntityTypes() {
		if (relatedEntityTypes == null) {
			relatedEntityTypes = Stream
					.concat(this.transitiveClosureSubEntityTypeNames.stream(),
							this.transitiveClosureSuperEntityTypeNames.stream())
					.map(slotTypeName -> EntityType.get(slotTypeName)).sorted().collect(Collectors.toSet());
			relatedEntityTypes.add(this);
		}
		return Collections.unmodifiableSet(relatedEntityTypes);
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
	 * Checks recursively if the given entity type is a sub entity of this entity
	 * type.
	 * 
	 * @param subEntityType
	 * @return true if
	 */
	public boolean isSuperEntityOf(EntityType subEntityType) {
		final Set<EntityType> directSubEntities = getTransitiveClosureSubEntityTypes();
		return directSubEntities.contains(subEntityType);
	}

	/**
	 * Checks recursively if the given entity type is a super entity of this entity
	 * type.
	 * 
	 * @param subEntityType
	 * @return true if
	 */
	public boolean isSubEntityOf(EntityType superEntityType) {
		final Set<EntityType> superEntities = getTransitiveClosureSuperEntityTypes();
		return superEntities.contains(superEntityType);
	}

	/**
	 * Sets the normalization function for this entity type.
	 * 
	 * @param normalizationFunction
	 */
	public void setNormalizationFunction(INormalizationFunction normalizationFunction) {
		this.normalizationFunction = normalizationFunction;
	}

	/**
	 * Returns the normalization function for that entity type.
	 * 
	 * @return
	 */
	public INormalizationFunction getNormalizationFunction() {
		return normalizationFunction;
	}

	private static EntityType initInstance = null;

	/**
	 * Returns the initialization instance.
	 * 
	 * @return the initialization instance.
	 */
	public static EntityType getInitializationInstance() {
		if (initInstance == null) {
			initInstance = new EntityType();
		}
		return initInstance;

	}

	@Override
	public void system_init(Specifications specifications) {
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
		entityTypes = Collections.unmodifiableSet(new HashSet<>(entityTypeFactory.values()));
	}

	/**
	 * Returns all entity types that were specified in the scope.
	 * 
	 * @return
	 */
	public static Set<EntityType> getEntityTypes() {
		return entityTypes;
	}

	@Override
	public int compareTo(EntityType o) {
		return o.name.compareTo(this.name);
	}

	@Override
	public String toString() {
		return "EntityType [entityTypeName=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		return false;
	}

	/**
	 * Checks if this entity type has the given slot.
	 * 
	 * @param slotType
	 * @return
	 */
	public boolean containsSlotType(SlotType slotType) {
		return getSlots().contains(slotType);
	}

	/**
	 * Returns true if this entity type has no slots.
	 * 
	 * @return
	 */
	public boolean hasNoSlots() {
		return getSlots().isEmpty();
	}

	/**
	 * Returns true if this entity type has no sub entity types.
	 * 
	 * @return
	 */
	public boolean isLeafEntityType() {
		return getTransitiveClosureSubEntityTypes().isEmpty();
	}

}

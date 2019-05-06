package de.hterhors.semanticmr.init.specifications;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class Specifications {

	private final Set<String> entityTypeNames;
	private final Set<String> slotTypeNames;

	private final Map<String, Boolean> isLiteralValueSlotTypes;

	/**
	 * A map of slot names and a corresponding set of entity types that can be
	 * filled in that slot.
	 */
	private final Map<String, Set<String>> slotFillerEntityTypes;

	/**
	 * Map of entities and a set of super entities.
	 */
	private final Map<String, Set<String>> superEntityTypes;
	private final Map<String, Set<String>> subEntityTypes;

	private final Map<String, Set<String>> slotsForEntity;
	private final Map<String, Integer> slotMaxSizes;

	public Specifications(Set<String> entityTypeNames, Set<String> slotTypeNames,
			Map<String, Boolean> isLiteralValueSlotTypes, Map<String, Set<String>> slotFillerEntityTypes,
			Map<String, Set<String>> superEntityTypes, Map<String, Set<String>> subEntityTypes,
			Map<String, Set<String>> slotsForEntities, Map<String, Integer> slotMaxSize) {

		this.entityTypeNames = Collections.unmodifiableSet(new HashSet<>(entityTypeNames));
		this.slotTypeNames = Collections.unmodifiableSet(new HashSet<>(slotTypeNames));

		this.isLiteralValueSlotTypes = Collections.unmodifiableMap(new HashMap<>(isLiteralValueSlotTypes));

		this.superEntityTypes = new HashMap<>();
		for (String entityTypeName : this.entityTypeNames) {
			resolveSuperClassTransitivity(superEntityTypes, entityTypeName, entityTypeName);
		}
		this.subEntityTypes = new HashMap<>();
		for (String entityTypeName : this.entityTypeNames) {
			resolveSubClassTransitivity(subEntityTypes, entityTypeName, entityTypeName);
		}

		this.slotFillerEntityTypes = new HashMap<>(slotFillerEntityTypes);
		resolveSlotFillerTransitivity(slotFillerEntityTypes);

		this.slotsForEntity = new HashMap<>(slotsForEntities);
		resolveSlotTransitivity(slotsForEntities);

		this.slotMaxSizes = Collections.unmodifiableMap(
				slotMaxSize.entrySet().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
	}

	private void resolveSuperClassTransitivity(Map<String, Set<String>> superEntityTypes, String baseEntityTypeName,
			final String superEntityName) {

		if (!superEntityTypes.containsKey(superEntityName))
			return;

		for (String superEntity : superEntityTypes.get(superEntityName)) {
			this.superEntityTypes.putIfAbsent(baseEntityTypeName, new HashSet<>());
			this.superEntityTypes.get(baseEntityTypeName).add(superEntity);

			resolveSuperClassTransitivity(superEntityTypes, baseEntityTypeName, superEntity);
		}

	}

	private void resolveSubClassTransitivity(Map<String, Set<String>> subEntityTypes, String baseEntityTypeName,
			final String subEntityName) {

		if (!subEntityTypes.containsKey(subEntityName))
			return;

		for (String subEntity : subEntityTypes.get(subEntityName)) {
			this.subEntityTypes.putIfAbsent(baseEntityTypeName, new HashSet<>());
			this.subEntityTypes.get(baseEntityTypeName).add(subEntity);
			resolveSubClassTransitivity(subEntityTypes, baseEntityTypeName, subEntity);
		}

	}

	private void resolveSlotFillerTransitivity(Map<String, Set<String>> slotFillerEntityTypes) {

		for (Entry<String, Set<String>> slotFillerEntityType : slotFillerEntityTypes.entrySet()) {

			for (String entitType : slotFillerEntityType.getValue()) {

				if (!this.subEntityTypes.containsKey(entitType))
					continue;

				this.slotFillerEntityTypes.get(slotFillerEntityType.getKey())
						.addAll(this.subEntityTypes.getOrDefault(entitType, Collections.emptySet()));
			}
		}
	}

	private void resolveSlotTransitivity(Map<String, Set<String>> slotsForEntities) {

		for (String entityWithSlots : slotsForEntities.keySet()) {

			if (!this.subEntityTypes.containsKey(entityWithSlots))
				continue;

			for (String subEntity : subEntityTypes.get(entityWithSlots)) {

				this.slotsForEntity.putIfAbsent(subEntity, new HashSet<>());
				this.slotsForEntity.get(subEntity)
						.addAll(slotsForEntities.getOrDefault(entityWithSlots, Collections.emptySet()));
			}
		}
	}

	public Set<String> getSlotsForEntityType(String entityTypeName) {
		return slotsForEntity.getOrDefault(entityTypeName, Collections.emptySet());
	}

	public Set<String> getEntityTypeNames() {
		return entityTypeNames;
	}

	public Set<String> getSlotTypeNames() {
		return slotTypeNames;
	}

	public boolean isLiteralEntityType(String internalizedEntityTypeName) {
		return isLiteralValueSlotTypes.get(internalizedEntityTypeName).booleanValue();
	}

	public Set<String> getSlotFillerEntityTypeNames(String internalizedSlotTypeName) {
		return slotFillerEntityTypes.getOrDefault(internalizedSlotTypeName, Collections.emptySet());
	}

	public Set<String> getSuperEntityTypeNames(String entityTypeName) {
		return superEntityTypes.getOrDefault(entityTypeName, Collections.emptySet());
	}

	public Set<String> getSubEntityTypeNames(String entityTypeName) {
		return subEntityTypes.getOrDefault(entityTypeName, Collections.emptySet());
	}

	public int getMultiAnnotationSlotMaxSize(String internalizedSlotTypeName) {
		return slotMaxSizes.getOrDefault(internalizedSlotTypeName, 0).intValue();
	}

}

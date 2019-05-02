package de.hterhors.semanticmr.init.specifications;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.hterhors.semanticmr.init.reader.json.structures.EntityTypeStructure;
import de.hterhors.semanticmr.init.reader.json.structures.SlotTypeSpecification;

import java.util.Set;

public class StructureSpecification {

	public static class ExcludeSlotTypePairNames {

		public final String onTemplateType;

		public ExcludeSlotTypePairNames(String onTemplateType, String withSlotTypeName, String withEntityTypeName,
				String excludeSlotTypeName, String excludeEntityTypeName) {
			this.onTemplateType = onTemplateType;
			this.withSlotTypeName = withSlotTypeName;
			this.withEntityTypeName = withEntityTypeName;
			this.excludeSlotTypeName = excludeSlotTypeName;
			this.excludeEntityTypeName = excludeEntityTypeName;
		}

		public final String withSlotTypeName;
		public final String withEntityTypeName;
		public final String excludeSlotTypeName;
		public final String excludeEntityTypeName;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((excludeEntityTypeName == null) ? 0 : excludeEntityTypeName.hashCode());
			result = prime * result + ((excludeSlotTypeName == null) ? 0 : excludeSlotTypeName.hashCode());
			result = prime * result + ((onTemplateType == null) ? 0 : onTemplateType.hashCode());
			result = prime * result + ((withEntityTypeName == null) ? 0 : withEntityTypeName.hashCode());
			result = prime * result + ((withSlotTypeName == null) ? 0 : withSlotTypeName.hashCode());
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
			ExcludeSlotTypePairNames other = (ExcludeSlotTypePairNames) obj;
			if (excludeEntityTypeName == null) {
				if (other.excludeEntityTypeName != null)
					return false;
			} else if (!excludeEntityTypeName.equals(other.excludeEntityTypeName))
				return false;
			if (excludeSlotTypeName == null) {
				if (other.excludeSlotTypeName != null)
					return false;
			} else if (!excludeSlotTypeName.equals(other.excludeSlotTypeName))
				return false;
			if (onTemplateType == null) {
				if (other.onTemplateType != null)
					return false;
			} else if (!onTemplateType.equals(other.onTemplateType))
				return false;
			if (withEntityTypeName == null) {
				if (other.withEntityTypeName != null)
					return false;
			} else if (!withEntityTypeName.equals(other.withEntityTypeName))
				return false;
			if (withSlotTypeName == null) {
				if (other.withSlotTypeName != null)
					return false;
			} else if (!withSlotTypeName.equals(other.withSlotTypeName))
				return false;
			return true;
		}

	}

	private final Set<String> entityTypeNames;
	private final Set<String> slotTypeNames;

	private final Map<String, Boolean> isSingleValueSlotTypes;
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
	private final Map<String, Integer> multiAnnotationSlotMaxSizes;
//	private final Set<ExcludeSlotTypePairNames> excludeSlotTypePairs;

	public StructureSpecification(Set<String> entityTypeNames, Set<String> slotTypeNames,
			Map<String, Boolean> isSingleValueSlotTypes, Map<String, Boolean> isLiteralValueSlotTypes,
			Map<String, Set<String>> slotFillerEntityTypes, Map<String, Set<String>> superEntityTypes,
			Map<String, Set<String>> subEntityTypes, Map<String, Set<String>> slotsForEntities,
			Map<String, Integer> multiAnnotationSlotMaxSizes
//			, Set<ExcludeSlotTypePairNames> excludeSlotTypePairs
	) {

		this.entityTypeNames = Collections.unmodifiableSet(new HashSet<>(entityTypeNames));
		this.slotTypeNames = Collections.unmodifiableSet(new HashSet<>(slotTypeNames));
		this.isSingleValueSlotTypes = Collections.unmodifiableMap(new HashMap<>(isSingleValueSlotTypes));

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

		this.multiAnnotationSlotMaxSizes = Collections.unmodifiableMap(multiAnnotationSlotMaxSizes);
//		this.excludeSlotTypePairs = Collections.unmodifiableSet(excludeSlotTypePairs);

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
//
//	public Set<ExcludeSlotTypePairNames> getExcludeSlotTypePairs() {
//		return excludeSlotTypePairs;
//	}

	public boolean isSingleValueSlot(String slotTypeName) {
		return isSingleValueSlotTypes.get(slotTypeName).booleanValue();
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

	@SuppressWarnings("boxing")
	public int getMultiAnnotationSlotMaxSize(String internalizedSlotTypeName) {
		return multiAnnotationSlotMaxSizes.getOrDefault(internalizedSlotTypeName, 0).intValue();
	}

}

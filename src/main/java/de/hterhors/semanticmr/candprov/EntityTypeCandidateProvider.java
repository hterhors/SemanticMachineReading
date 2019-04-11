package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.structure.annotations.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class EntityTypeCandidateProvider implements ISlotFillerCandidateProvider<EntityType> {

	private static EntityTypeCandidateProvider instance = null;

	public static EntityTypeCandidateProvider getInstance() {

		if (instance == null)
			instance = new EntityTypeCandidateProvider();

		return instance;

	}

	private final Map<SlotType, List<EntityType>> entityAnnotationCache = new HashMap<>();

	public List<EntityType> getSlotFillerCandidates(SlotType slot) {
		if (!entityAnnotationCache.containsKey(slot)) {

			for (EntityType slotEntityType : slot.getSlotFillerEntityTypes()) {

				if (slotEntityType.isLiteral) {
					entityAnnotationCache.putIfAbsent(slot, Collections.emptyList());
					continue;
				}

				entityAnnotationCache.putIfAbsent(slot, new ArrayList<>());
				entityAnnotationCache.get(slot).add(slotEntityType);
			}
		}
		return entityAnnotationCache.get(slot);

	}

	@Override
	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType) {
		return templateType.getRelatedEntityTypes();
	}

	@Override
	public void addSlotFiller(EntityType slotFiller) {
		throw new IllegalStateException(
				"Can not add slot filler to enitty type candiate provider. Candidates are based on the specification.");
	}

	@Override
	public void addBatchSlotFiller(Collection<EntityType> slotFiller) {
		throw new IllegalStateException(
				"Can not add slot filler to enitty type candiate provider. Candidates are based on the specification.");
	}

}

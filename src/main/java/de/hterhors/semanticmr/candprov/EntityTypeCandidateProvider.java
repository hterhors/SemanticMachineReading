package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class EntityTypeCandidateProvider implements ISlotFillerCandidateProvider<EntityType> {

	private final Map<SlotType, List<EntityType>> entityAnnotationCache = new HashMap<>();

	public EntityTypeCandidateProvider() {
	}

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
		// TODO Auto-generated method stub

	}

	@Override
	public void addBatchSlotFiller(Collection<EntityType> slotFiller) {
		// TODO Auto-generated method stub

	}

}

package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class EntityTemplateCandidateProvider implements ISlotFillerCandidateProvider<EntityTemplate> {

	private final Map<SlotType, List<EntityTemplate>> entityAnnotationCache;

	public EntityTemplateCandidateProvider() {
		this.entityAnnotationCache = new HashMap<>();
	}

	public void addSlotFiller(EntityTemplate slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add(slotFiller);
			}
		}
	}

	public void addBatchSlotFiller(Collection<EntityTemplate> slotFiller) {
		for (EntityTemplate literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
	}

	@Override
	public List<EntityTemplate> getSlotFillerCandidates(SlotType slot) {
		return entityAnnotationCache.getOrDefault(slot, Collections.emptyList());
	}

	@Override
	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType) {
		return Collections.emptySet();
	}

}

package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.Literal;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class LiteralCandidateProvider implements ISlotFillerCandidateProvider<Literal> {

	private final Map<SlotType, List<Literal>> entityAnnotationCache = new HashMap<>();

	public void addSlotFiller(Literal slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add(slotFiller);
			}
		}
	}

	public void addBatchSlotFiller(Collection<Literal> slotFiller) {
		for (Literal literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
	}

	@Override
	public List<Literal> getSlotFillerCandidates(SlotType slotType) {
		return entityAnnotationCache.getOrDefault(slotType, Collections.emptyList());
	}

	@Override
	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType) {
		return Collections.emptySet();
	}

}

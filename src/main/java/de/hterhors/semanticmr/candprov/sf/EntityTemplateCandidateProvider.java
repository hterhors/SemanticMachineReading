package de.hterhors.semanticmr.candprov.sf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public class EntityTemplateCandidateProvider implements ISlotTypeAnnotationCandidateProvider {

	private final Map<SlotType, List<EntityTemplate>> entityAnnotationCache = new HashMap<>();

	private final Instance relatedInstance;

	public EntityTemplateCandidateProvider(Instance relatedInstance) {
		this.relatedInstance = relatedInstance;
	}

	@Override
	public EntityTemplateCandidateProvider addSlotFiller(
			AbstractAnnotation<? extends AbstractAnnotation<?>> slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add((EntityTemplate) slotFiller);
			}
		}
		return this;
	}

	@Override
	public EntityTemplateCandidateProvider addBatchSlotFiller(
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> slotFiller) {
		for (AbstractAnnotation<? extends AbstractAnnotation<?>> literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
		return this;
	}

	@Override
	public List<EntityTemplate> getCandidates(SlotType slot) {
		return entityAnnotationCache.getOrDefault(slot, Collections.emptyList());
	}

	@Override
	public Instance getRelatedInstance() {
		return relatedInstance;
	}
}

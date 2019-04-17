package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public class GeneralCandidateProvider implements ISlotFillerCandidateProvider<EntityTypeAnnotation> {

	private final Map<SlotType, List<EntityTypeAnnotation>> entityAnnotationCache = new HashMap<>();
	private final Map<EntityType, Set<EntityTypeAnnotation>> rootAnnotationsCache = new HashMap<>();

	private final Instance relatedInstance;

	public GeneralCandidateProvider(Instance relatedInstance) {
		this.relatedInstance = relatedInstance;
	}

	public GeneralCandidateProvider addSlotFiller(EntityTypeAnnotation slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add(slotFiller);
			}
		}

		rootAnnotationsCache.putIfAbsent(slotFiller.entityType, new HashSet<>());
		rootAnnotationsCache.get(slotFiller.entityType).add(slotFiller);
		for (EntityType relatedEntitytype : slotFiller.entityType.getRelatedEntityTypes()) {
			rootAnnotationsCache.putIfAbsent(relatedEntitytype, new HashSet<>());
			rootAnnotationsCache.get(relatedEntitytype).add(slotFiller);
		}
		return this;
	}

	public GeneralCandidateProvider addBatchSlotFiller(Collection<EntityTypeAnnotation> slotFiller) {
		for (EntityTypeAnnotation literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
		return this;
	}

	@Override
	public List<EntityTypeAnnotation> getSlotFillerCandidates(SlotType slotType) {
		return entityAnnotationCache.getOrDefault(slotType, Collections.emptyList());
	}

	@Override
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType) {
		return rootAnnotationsCache.getOrDefault(templateType, Collections.emptySet());
	}

	public Instance getRelatedInstance() {
		return relatedInstance;
	}

}

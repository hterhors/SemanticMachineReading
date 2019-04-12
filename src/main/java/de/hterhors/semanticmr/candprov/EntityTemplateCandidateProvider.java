package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class EntityTemplateCandidateProvider implements ISlotFillerCandidateProvider<EntityTemplate> {

	private final Map<SlotType, List<EntityTemplate>> entityAnnotationCache = new HashMap<>();
	private final Document relatedDocument;

	public EntityTemplateCandidateProvider(Document relatedDocument) {
		this.relatedDocument = relatedDocument;
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
	public Set<EntityTypeAnnotation> getTemplateRootAnnotationCandidates(EntityType templateType) {
		return Collections.emptySet();
	}

	public Document getRelatedDocument() {
		return relatedDocument;
	}
}

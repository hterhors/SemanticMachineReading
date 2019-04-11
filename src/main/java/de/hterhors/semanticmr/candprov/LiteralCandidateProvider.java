package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.LiteralAnnotation;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class LiteralCandidateProvider implements ISlotFillerCandidateProvider<LiteralAnnotation> {

	private final Map<SlotType, List<LiteralAnnotation>> entityAnnotationCache = new HashMap<>();

	private final Document relatedDocument;

	public LiteralCandidateProvider(Document relatedDocument) {
		this.relatedDocument = relatedDocument;
	}

	public void addSlotFiller(LiteralAnnotation slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add(slotFiller);
			}
		}
	}

	public void addBatchSlotFiller(Collection<LiteralAnnotation> slotFiller) {
		for (LiteralAnnotation literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
	}

	@Override
	public List<LiteralAnnotation> getSlotFillerCandidates(SlotType slotType) {
		return entityAnnotationCache.getOrDefault(slotType, Collections.emptyList());
	}

	@Override
	public Set<EntityType> getTemplateTypeCandidates(EntityType templateType) {
		return Collections.emptySet();
	}

	public Document getRelatedDocument() {
		return relatedDocument;
	}

}

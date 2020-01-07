package de.hterhors.semanticmr.candidateretrieval.sf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;

public class SlotFillingCandidateRetrieval {

	private final Map<EntityType, Set<EntityTypeAnnotation>> entityTypeCandidates = new HashMap<>();

	private final Map<SlotType, Set<AbstractAnnotation>> slotTypeCandidates = new HashMap<>();

	public Set<EntityTypeAnnotation> getEntityTypeCandidates(EntityType entityType) {
		return entityTypeCandidates.getOrDefault(entityType, Collections.emptySet());
	}

	public Set<AbstractAnnotation> getSlotTypeCandidates(SlotType slotType) {
		return slotTypeCandidates.getOrDefault(slotType, Collections.emptySet());
	}

	public void addCandidateAnnotations(Collection<? extends AbstractAnnotation> candidates) {
		for (AbstractAnnotation candidate : candidates) {
			addCandidateAnnotation(candidate);
		}
	}

	public void addCandidateAnnotation(AbstractAnnotation candidate) {

		if (!candidate.isInstanceOfEntityTemplate()) {
			candidate = candidate.getEntityType().hasNoSlots() ? candidate
					: new EntityTemplate(candidate.asInstanceOfEntityTypeAnnotation());
		}

		for (SlotType slotType : candidate.getEntityType().getSlotFillerOfSlotTypes()) {
			slotTypeCandidates.putIfAbsent(slotType, new HashSet<>());
			if (slotType.matchesEntityType(candidate.getEntityType())) {
				slotTypeCandidates.get(slotType).add(candidate);
			}
		}

		for (EntityType relatedEntitytype : candidate.getEntityType().getHierarchicalEntityTypes()) {
			if (candidate.isInstanceOfEntityTypeAnnotation()) {
				entityTypeCandidates.putIfAbsent(relatedEntitytype, new HashSet<>());
				entityTypeCandidates.get(relatedEntitytype).add((EntityTypeAnnotation) candidate);
			} else {
				entityTypeCandidates.putIfAbsent(relatedEntitytype, new HashSet<>());
				entityTypeCandidates.get(relatedEntitytype).add(((EntityTemplate) candidate).getRootAnnotation());
			}
		}

	}

}

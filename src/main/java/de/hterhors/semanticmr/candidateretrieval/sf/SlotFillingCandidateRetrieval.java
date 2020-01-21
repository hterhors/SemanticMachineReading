package de.hterhors.semanticmr.candidateretrieval.sf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;

public class SlotFillingCandidateRetrieval {
	public static interface IFilter {
		public boolean remove(AbstractAnnotation candidate);
	}

	public void removeCandidateAnnotations(IFilter filter) {
		for (EntityType key : entityTypeCandidates.keySet()) {
			for (Iterator<EntityTypeAnnotation> iterator = entityTypeCandidates.get(key).iterator(); iterator
					.hasNext();) {
				if (filter.remove(iterator.next()))
					iterator.remove();
			}
		}
		for (SlotType key : slotTypeCandidates.keySet()) {
			for (Iterator<AbstractAnnotation> iterator = slotTypeCandidates.get(key).iterator(); iterator.hasNext();) {
				if (filter.remove(iterator.next()))
					iterator.remove();
			}
		}
	}

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

		if (!candidate.getEntityType().getTransitiveClosureSubEntityTypes().isEmpty())
			return;

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

		for (EntityType relatedEntityType : candidate.getEntityType().getHierarchicalEntityTypes()) {

			entityTypeCandidates.putIfAbsent(relatedEntityType, new HashSet<>());
			if (candidate.isInstanceOfEntityTypeAnnotation()) {
				entityTypeCandidates.get(relatedEntityType).add((EntityTypeAnnotation) candidate);
			} else {
				entityTypeCandidates.get(relatedEntityType).add(((EntityTemplate) candidate).getRootAnnotation());
			}
		}

	}

}

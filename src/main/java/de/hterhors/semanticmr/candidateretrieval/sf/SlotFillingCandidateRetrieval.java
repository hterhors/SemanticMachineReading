package de.hterhors.semanticmr.candidateretrieval.sf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer.ESamplingMode;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;

public class SlotFillingCandidateRetrieval {

	public static interface IFilter {
		public boolean remove(AbstractAnnotation candidate);
	}

	public void filterOutAnnotationCandidates(IFilter filter) {
		for (EntityType key : entityTypeAnnotationCandidates.keySet()) {
			for (Iterator<EntityTypeAnnotation> iterator = entityTypeAnnotationCandidates.get(key).iterator(); iterator
					.hasNext();) {
				if (filter.remove(iterator.next()))
					iterator.remove();
			}
		}
		for (SlotType key : slotTypeAnnotationCandidates.keySet()) {
			for (Iterator<AbstractAnnotation> iterator = slotTypeAnnotationCandidates.get(key).iterator(); iterator
					.hasNext();) {
				if (filter.remove(iterator.next()))
					iterator.remove();
			}
		}
	}

	private final Map<EntityType, Set<EntityTypeAnnotation>> entityTypeAnnotationCandidates = new HashMap<>();

	private final Map<SlotType, Set<AbstractAnnotation>> slotTypeAnnotationCandidates = new HashMap<>();

	private final Map<EntityType, Set<EntityTypeAnnotation>> entityTypeCandidates = new HashMap<>();

	private final Map<SlotType, Set<AbstractAnnotation>> slotTypeCandidates = new HashMap<>();

	public Set<EntityTypeAnnotation> getEntityTypeCandidates(ESamplingMode samplingMode, EntityType entityType) {
		if (samplingMode == ESamplingMode.ANNOTATION_BASED)
			return entityTypeAnnotationCandidates.getOrDefault(entityType, Collections.emptySet());
		if (samplingMode == ESamplingMode.TYPE_BASED)
			return entityTypeCandidates.getOrDefault(entityType, Collections.emptySet());
		else
			throw new IllegalArgumentException("Unkown sampling mode: " + samplingMode);
	}

	public Set<AbstractAnnotation> getSlotTypeCandidates(ESamplingMode samplingMode, SlotType slotType) {
		if (samplingMode == ESamplingMode.ANNOTATION_BASED)
			return slotTypeAnnotationCandidates.getOrDefault(slotType, Collections.emptySet());
		if (samplingMode == ESamplingMode.TYPE_BASED)
			return slotTypeCandidates.getOrDefault(slotType, Collections.emptySet());
		else
			throw new IllegalArgumentException("Unkown sampling mode: " + samplingMode);
	}

	public void addCandidateAnnotations(Collection<? extends AbstractAnnotation> candidates) {
		for (AbstractAnnotation candidate : candidates) {
			addCandidateAnnotation(candidate);
		}
	}

	public void addCandidateAnnotation(AbstractAnnotation candidate) {

		if (!candidate.isInstanceOfEntityTemplate()
				&& !candidate.getEntityType().getTransitiveClosureSubEntityTypes().isEmpty())
			return;

		if (!candidate.isInstanceOfEntityTemplate()) {
			candidate = candidate.getEntityType().hasNoSlots() ? candidate
					: new EntityTemplate(candidate.asInstanceOfEntityTypeAnnotation());
		}

		for (SlotType slotType : candidate.getEntityType().getSlotFillerOfSlotTypes()) {

			if (slotType.isExcluded() || slotType.isFrozen())
				continue;

			slotTypeAnnotationCandidates.putIfAbsent(slotType, new HashSet<>());
			if (slotType.matchesEntityType(candidate.getEntityType())) {
				slotTypeAnnotationCandidates.get(slotType).add(candidate);
			}

			slotTypeCandidates.putIfAbsent(slotType, new HashSet<>());
			if (slotType.matchesEntityType(candidate.getEntityType())) {
				slotTypeCandidates.get(slotType).add(candidate.reduceToEntityType());
			}

		}

		for (EntityType relatedEntityType : candidate.getEntityType().getHierarchicalEntityTypes()) {

			entityTypeAnnotationCandidates.putIfAbsent(relatedEntityType, new HashSet<>());
			if (candidate.isInstanceOfEntityTypeAnnotation()) {
				entityTypeAnnotationCandidates.get(relatedEntityType).add((EntityTypeAnnotation) candidate);
			} else {
				entityTypeAnnotationCandidates.get(relatedEntityType)
						.add(((EntityTemplate) candidate).getRootAnnotation());
			}

			entityTypeCandidates.putIfAbsent(relatedEntityType, new HashSet<>());
			Set<EntityTypeAnnotation> annotations = entityTypeCandidates.get(relatedEntityType);
			final EntityTypeAnnotation annotation;
			if (candidate.isInstanceOfEntityTypeAnnotation()) {
				annotation = (EntityTypeAnnotation) candidate;
			} else {
				annotation = ((EntityTemplate) candidate).getRootAnnotation();
			}

			if (candidate.getEntityType().isLiteral) {
				annotations.add(annotation);

			} else {
				/**
				 * Reduce to entity type if annotation would be subclass of entity type
				 * annotation.
				 */
				annotations.add(AnnotationBuilder.toAnnotation(annotation.getEntityType()));

			}

		}

	}

}

package de.hterhors.semanticmr.candprov.sf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public class SlotEntityTypeCandidateProvider
		implements ISlotTypeAnnotationCandidateProvider, IEntityTypeAnnotationCandidateProvider {

	private static SlotEntityTypeCandidateProvider instance = null;

	public static SlotEntityTypeCandidateProvider getInstance() {

		if (instance == null)
			instance = new SlotEntityTypeCandidateProvider();

		return instance;

	}

	private final Map<EntityType, Set<EntityTypeAnnotation>> rootAnnotationsCache = new HashMap<>();

	private final Map<SlotType, List<AbstractAnnotation>> entityAnnotationCache = new HashMap<>();

	@Override
	public List<AbstractAnnotation> getCandidates(SlotType slot) {
		if (slot.isExcluded())
			return Collections.emptyList();

		if (!entityAnnotationCache.containsKey(slot)) {

			for (EntityType slotEntityType : slot.getSlotFillerEntityTypes()) {

				if (slotEntityType.isLiteral) {
					entityAnnotationCache.putIfAbsent(slot, Collections.emptyList());
					continue;
				}

				entityAnnotationCache.putIfAbsent(slot, new ArrayList<>());
				entityAnnotationCache.get(slot).add(EntityTypeAnnotation.get(slotEntityType));
			}
		}
		return entityAnnotationCache.get(slot);

	}

	@Override
	public Set<EntityTypeAnnotation> getCandidates(EntityType templateType) {
		if (!rootAnnotationsCache.containsKey(templateType)) {

			for (EntityType slotEntityType : templateType.getHierarchicalEntityTypes()) {

				if (slotEntityType.isLiteral) {
					rootAnnotationsCache.putIfAbsent(templateType, Collections.emptySet());
					continue;
				}

				rootAnnotationsCache.putIfAbsent(templateType, new HashSet<>());
				rootAnnotationsCache.get(templateType).add(EntityTypeAnnotation.get(slotEntityType));
			}
		}
		return rootAnnotationsCache.getOrDefault(templateType, Collections.emptySet());
	}

	@Override
	public SlotEntityTypeCandidateProvider addSlotFiller(AbstractAnnotation slotFiller) {
		throw new IllegalStateException(
				"Can not add slot filler to enitty type candiate provider. Candidates are based on the specification.");
	}

	@Override
	public SlotEntityTypeCandidateProvider addBatchSlotFiller(Collection<AbstractAnnotation> slotFiller) {
		throw new IllegalStateException(
				"Can not add slot filler to enitty type candiate provider. Candidates are based on the specification.");
	}

	@Override
	public Instance getRelatedInstance() {
		throw new IllegalStateException(
				"This candidate provider does not correspond to an individual instance but to all.");
	}

}

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
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public class GeneralCandidateProvider implements IAnnotationCandidateProvider {

	private final Map<SlotType, List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>>> entityAnnotationCache = new HashMap<>();
	private final Map<EntityType, Set<EntityTypeAnnotation>> rootAnnotationsCache = new HashMap<>();

	private final Instance relatedInstance;

	public GeneralCandidateProvider(Instance relatedInstance) {
		this.relatedInstance = relatedInstance;
	}

	@Override
	public GeneralCandidateProvider addSlotFiller(AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller) {
		for (SlotType slotType : slotFiller.getEntityType().getSlotFillerOfSlotTypes()) {
			entityAnnotationCache.putIfAbsent(slotType, new ArrayList<>());
			if (slotType.matchesEntityType(slotFiller.getEntityType())) {
				entityAnnotationCache.get(slotType).add(slotFiller);
			}
		}

		rootAnnotationsCache.putIfAbsent(slotFiller.getEntityType(), new HashSet<>());
		rootAnnotationsCache.get(slotFiller.getEntityType()).add((EntityTypeAnnotation) slotFiller);
		for (EntityType relatedEntitytype : slotFiller.getEntityType().getRelatedEntityTypes()) {
			rootAnnotationsCache.putIfAbsent(relatedEntitytype, new HashSet<>());
			rootAnnotationsCache.get(relatedEntitytype).add((EntityTypeAnnotation) slotFiller);
		}
		return this;
	}

	@Override
	public GeneralCandidateProvider addBatchSlotFiller(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFiller) {
		for (AbstractSlotFiller<? extends AbstractSlotFiller<?>> literalSlotFiller : slotFiller) {
			addSlotFiller(literalSlotFiller);
		}
		return this;
	}

	@Override
	public List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> getSlotFillerCandidates(SlotType slotType) {
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

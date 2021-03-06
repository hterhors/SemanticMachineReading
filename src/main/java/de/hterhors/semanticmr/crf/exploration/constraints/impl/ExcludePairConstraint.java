package de.hterhors.semanticmr.crf.exploration.constraints.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.exploration.constraints.AbstractHardConstraint;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlot;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * Implements a constraint on a pair of slot-entity pair.
 * 
 * Validation fails if the checked EntityTemplate contains both pairs of
 * {@link SlotType}-{@link EntityType}-pairs
 * 
 * @author hterhors
 *
 */
public class ExcludePairConstraint extends AbstractHardConstraint {

	public static class SlotEntityPair {
		final SlotType slotType;
		final EntityType entityType;

		public SlotEntityPair(SlotType withSlotType, EntityType withSlotFillerEntityType) {
			this.slotType = withSlotType;
			this.entityType = withSlotFillerEntityType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
			result = prime * result + ((slotType == null) ? 0 : slotType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SlotEntityPair other = (SlotEntityPair) obj;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (entityType != other.entityType)
				return false;
			if (slotType == null) {
				if (other.slotType != null)
					return false;
			} else if (slotType != other.slotType)
				return false;
			return true;
		}

	}

	final private SlotEntityPair with;

	final private SlotEntityPair exclude;

	final private EntityType onTemplateType;

	public ExcludePairConstraint(EntityType onTemplateType, SlotEntityPair withSlotEntityPair,
			SlotEntityPair excludeSlotEntityPair) {
		this.onTemplateType = onTemplateType;
		this.with = withSlotEntityPair;
		this.exclude = excludeSlotEntityPair;
	}

	public ExcludePairConstraint(SlotEntityPair withSlotEntityPair, SlotEntityPair excludeSlotEntityPair) {
		this.with = withSlotEntityPair;
		this.exclude = excludeSlotEntityPair;
		this.onTemplateType = null;
	}

	@Override
	public boolean violatesConstraint(State state, EntityTemplate entityTemplate, int annotationindex) {

		if (onTemplateType != null && onTemplateType != entityTemplate.getEntityType())
			return false;

		final AbstractSlot withSlotFiller = with.slotType.isSingleValueSlot()
				? entityTemplate.getSingleFillerSlot(with.slotType)
				: entityTemplate.getMultiFillerSlot(with.slotType);

		if (!withSlotFiller.containsSlotFiller())
			return false;

		if (!withSlotFiller.containsSlotFillerOfEntityType(with.entityType))
			return false;

		final AbstractSlot excludeSlotFiller = exclude.slotType.isSingleValueSlot()
				? entityTemplate.getSingleFillerSlot(exclude.slotType)
				: entityTemplate.getMultiFillerSlot(exclude.slotType);

		if (!excludeSlotFiller.containsSlotFiller())
			return false;

		return excludeSlotFiller.containsSlotFillerOfEntityType(exclude.entityType);
	}

	@Override
	public List<EntityTemplate> violatesConstraint(State currentState, List<EntityTemplate> candidateListToFilter,
			int annotationindex) {

		List<EntityTemplate> filteredList = new ArrayList<>(candidateListToFilter.size());

		filteredList = candidateListToFilter.parallelStream()
				.filter(candidate -> !violatesConstraint(currentState, candidate, annotationindex))
				.collect(Collectors.toList());

		return filteredList;
	}

}

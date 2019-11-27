package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.SlotIsFilledTemplate.SlotIsFilledScope;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class SlotIsFilledTemplate extends AbstractFeatureTemplate<SlotIsFilledScope> {

	public SlotIsFilledTemplate() {
		super(false);
	}

	static class SlotIsFilledScope extends AbstractFactorScope {

		final EntityType entityTemplateType;
		final SlotType slot;
		final int numberOfSlotFiller;

		public SlotIsFilledScope(AbstractFeatureTemplate<SlotIsFilledScope> template, EntityType entityTemplateType,
				SlotType slot, int numberOfSlotFiller) {
			super(template);
			this.entityTemplateType = entityTemplateType;
			this.numberOfSlotFiller = numberOfSlotFiller;
			this.slot = slot;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((entityTemplateType == null) ? 0 : entityTemplateType.hashCode());
			result = prime * result + numberOfSlotFiller;
			result = prime * result + ((slot == null) ? 0 : slot.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SlotIsFilledScope other = (SlotIsFilledScope) obj;
			if (entityTemplateType == null) {
				if (other.entityTemplateType != null)
					return false;
			} else if (!entityTemplateType.equals(other.entityTemplateType))
				return false;
			if (numberOfSlotFiller != other.numberOfSlotFiller)
				return false;
			if (slot == null) {
				if (other.slot != null)
					return false;
			} else if (!slot.equals(other.slot))
				return false;
			return true;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String toString() {
			return "SlotIsFilledScope [entityTemplateType=" + entityTemplateType.entityName + ", slot=" + slot.slotName
					+ ", numberOfSlotFiller=" + numberOfSlotFiller + "]";
		}

	}

	private static final String PREFIX = "SIFT\t";

	@Override
	public List<SlotIsFilledScope> generateFactorScopes(State state) {
		List<SlotIsFilledScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			EntityTemplateAnnotationFilter filter = annotation.filter().entityTypeAnnoation().entityTemplateAnnoation()
					.multiSlots().singleSlots().merge().build();

			for (Entry<SlotType, Set<AbstractAnnotation>> slotIsFilledScope : filter.getMergedAnnotations()
					.entrySet()) {
				factors.add(new SlotIsFilledScope(this, annotation.getEntityType(), slotIsFilledScope.getKey(),
						slotIsFilledScope.getValue().size()));
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<SlotIsFilledScope> factor) {

		DoubleVector featureVector = factor.getFeatureVector();
		
		EntityType entity = factor.getFactorScope().entityTemplateType;
		add(factor, featureVector, entity);

		for (EntityType e : entity.getDirectSuperEntityTypes()) {
			add(factor, featureVector, e);
		}

	}

	public void add(Factor<SlotIsFilledScope> factor, DoubleVector featureVector, EntityType entity) {
		final String parentClassName = entity.entityName;
		final boolean slotIsFilled = factor.getFactorScope().numberOfSlotFiller > 0;

		final String slotName = factor.getFactorScope().slot.slotName;

		featureVector.set(PREFIX + "[" + parentClassName + "]" + slotName + " contains >2 element",
				factor.getFactorScope().numberOfSlotFiller >= 2);
		featureVector.set(PREFIX + "[" + parentClassName + "]" + slotName + " contains >3 element",
				factor.getFactorScope().numberOfSlotFiller >= 3);
		featureVector.set(PREFIX + "[" + parentClassName + "]" + slotName + " contains >4 element",
				factor.getFactorScope().numberOfSlotFiller >= 4);

//		featureVector.set("[" + parentClassName + "]" + propertyNameChain + " contains >2 distinct element",
//				factor.getFactorScope().numberOfDistinctSlotFiller >= 2
//						&& factor.getFactorScope().numberOfSlotFiller >= 2);
//		featureVector.set("[" + parentClassName + "]" + propertyNameChain + " contains >3 distinct element",
//				factor.getFactorScope().numberOfDistinctSlotFiller >= 3
//						&& factor.getFactorScope().numberOfSlotFiller <= 3);
//		featureVector.set("[" + parentClassName + "]" + propertyNameChain + " contains >4 distinct element",
//				factor.getFactorScope().numberOfDistinctSlotFiller >= 4
//						&& factor.getFactorScope().numberOfSlotFiller >= 4);
//
//		if (factor.getFactorScope().numberOfSlotFiller > 1) {
//			final boolean allDistinct = factor.getFactorScope().numberOfSlotFiller == factor
//					.getFactorScope().numberOfDistinctSlotFiller;
//			featureVector.set("[" + parentClassName + "]" + propertyNameChain + " all distinct", allDistinct);
//		}

		featureVector.set(PREFIX + "[" + parentClassName + "]" + slotName + " contains elements", slotIsFilled);
		featureVector.set(PREFIX + "[" + parentClassName + "]" + slotName + " is empty", !slotIsFilled);
	}

}

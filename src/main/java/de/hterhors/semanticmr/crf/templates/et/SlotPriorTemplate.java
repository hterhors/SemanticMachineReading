package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.SlotPriorTemplate.Scope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class SlotPriorTemplate extends AbstractFeatureTemplate<Scope> {

	static class Scope extends AbstractFactorScope {

		final SlotType slotType;
		final Set<String> values;

		public Scope(AbstractFeatureTemplate<Scope> template, SlotType slotType,
				Set<String> assignedEntityTypesOrValues) {
			super(template);
			this.values = assignedEntityTypesOrValues;
			this.slotType = slotType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((values == null) ? 0 : values.hashCode());
			result = prime * result + ((slotType == null) ? 0 : slotType.hashCode());
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
			Scope other = (Scope) obj;
			if (values == null) {
				if (other.values != null)
					return false;
			} else if (!values.equals(other.values))
				return false;
			if (slotType == null) {
				if (other.slotType != null)
					return false;
			} else if (!slotType.equals(other.slotType))
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

	}

	@Override
	public List<Scope> generateFactorScopes(State state) {
		List<Scope> factors = new ArrayList<>();

		for (EntityTemplate soccerPlayer : super.<EntityTemplate>getPredictedAnnotations(state)) {

			EntityTemplateAnnotationFilter singleFilter = soccerPlayer.filter().singleSlots().docLinkedAnnoation()
					.build();

			for (Entry<SlotType, AbstractAnnotation> annotation : singleFilter.getSingleAnnotations().entrySet()) {
				Set<String> values = new HashSet<>();
				addValue(values, annotation.getValue());
				factors.add(new Scope(this, annotation.getKey(), values));
			}

			EntityTemplateAnnotationFilter multiFilter = soccerPlayer.filter().multiSlots().docLinkedAnnoation()
					.build();

			for (Entry<SlotType, Set<AbstractAnnotation>> annotation : multiFilter.getMultiAnnotations().entrySet()) {

				Set<String> values = new HashSet<>();
				for (AbstractAnnotation a : annotation.getValue()) {
					addValue(values, a);
				}
				factors.add(new Scope(this, annotation.getKey(), values));
			}

		}

		return factors;
	}

	private void addValue(Set<String> values, AbstractAnnotation a) {
		DocumentLinkedAnnotation dla = a.asInstanceOfDocumentLinkedAnnotation();

		if (dla.entityType.isLiteral) {
			values.add(dla.textualContent.surfaceForm);
		} else {
			values.add(dla.entityType.name);
		}
	}

	@Override
	public void generateFeatureVector(Factor<Scope> factor) {

		/*
		 * If the property had multiple values, as in one-to-many relations (such as
		 * hasTeams) we create a feature that takes both values into account. This might
		 * be very sparse.
		 */
		if (factor.getFactorScope().values.size() > 1)
			factor.getFeatureVector().set("Prior towards: " + factor.getFactorScope().slotType.name + "->"
					+ factor.getFactorScope().values, true);

		for (String assignedClassesNamesOrValue : factor.getFactorScope().values) {

			/*
			 * For each property and for each value for that property create a single
			 * feature.
			 */
			factor.getFeatureVector().set(
					"Prior: " + factor.getFactorScope().slotType.name + "->" + assignedClassesNamesOrValue, true);
		}
	}

}

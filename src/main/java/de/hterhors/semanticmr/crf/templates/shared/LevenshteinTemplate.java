package de.hterhors.semanticmr.crf.templates.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.helper.LevenShteinSimilarities;
import de.hterhors.semanticmr.crf.templates.shared.LevenshteinTemplate.LevenshteinScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class LevenshteinTemplate extends AbstractFeatureTemplate<LevenshteinScope> {

	static class LevenshteinScope extends AbstractFactorScope<LevenshteinScope> {

		public final EntityType entityType;
		public final String surfaceForm;

		public LevenshteinScope(AbstractFeatureTemplate<LevenshteinScope> template, EntityType entityType,
				String surfaceForm) {
			super(template);
			this.entityType = entityType;
			this.surfaceForm = surfaceForm;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
			result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
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
			LevenshteinScope other = (LevenshteinScope) obj;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (!entityType.equals(other.entityType))
				return false;
			if (surfaceForm == null) {
				if (other.surfaceForm != null)
					return false;
			} else if (!surfaceForm.equals(other.surfaceForm))
				return false;
			return true;
		}

		@Override
		public int implementHashCode() {
			return hashCode();
		}

		@Override
		public boolean implementEquals(Object obj) {
			return equals(obj);
		}

	}

	@Override
	public List<LevenshteinScope> generateFactorScopes(State state) {
		List<LevenshteinScope> factors = new ArrayList<>();

		for (AbstractAnnotation annotation : getPredictedAnnotations(state)) {

			if (annotation.isInstanceOfDocumentLinkedAnnotation()) {
				factors.add(
						new LevenshteinScope(this, annotation.asInstanceOfDocumentLinkedAnnotation().getEntityType(),
								annotation.asInstanceOfDocumentLinkedAnnotation().getSurfaceForm()));
			} else if (annotation.isInstanceOfEntityTemplate()) {

				final EntityTemplateAnnotationFilter filter = annotation.asInstanceOfEntityTemplate().filter()
						.singleSlots().multiSlots().merge().nonEmpty().literalAnnoation().build();

				for (Entry<SlotType, Set<AbstractAnnotation>> slot : filter.getMergedAnnotations().entrySet()) {

					for (AbstractAnnotation slotFiller : slot.getValue()) {

						final LiteralAnnotation docLinkedAnnotation = slotFiller.asInstanceOfLiteralAnnotation();

						factors.add(new LevenshteinScope(this, docLinkedAnnotation.getEntityType(),
								docLinkedAnnotation.getSurfaceForm()));
					}
				}
			}
		}

		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<LevenshteinScope> factor) {
		LevenshteinScope scope = factor.getFactorScope();

		final double levenDist = LevenShteinSimilarities.levenshteinSimilarity(
				factor.getFactorScope().entityType.entityName, factor.getFactorScope().surfaceForm, 100);
		x: {
			if (levenDist > 0.1) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.1 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.1 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.2) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.2 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.2 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.3) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.3 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.3 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.4) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.4 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.4 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.5) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.5 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.5 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.6) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.6 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.6 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.7) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.7 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.7 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.8) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.8 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.8 " + levenDist, true);
				break x;
			}
			if (levenDist > 0.9) {
				factor.getFeatureVector().set("<" + scope.entityType.entityName + "> leven sim > 0.9 " + levenDist,
						true);
				factor.getFeatureVector().set("leven sim > 0.9 " + levenDist, true);
				break x;
			}
		}

	}

}

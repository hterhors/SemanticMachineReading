package de.hterhors.semanticmr.crf.templates.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.shared.SurfaceFormTemplate.SurfaceFormScope;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class SurfaceFormTemplate extends AbstractFeatureTemplate<SurfaceFormScope> {

	/**
	 * 
	 */

	public SurfaceFormTemplate() {
//		super(false);
	}

	private static final String PREFIX = "SFT\t";

	static class SurfaceFormScope extends AbstractFactorScope {

		public EntityType entityType;
		public final String surfaceForm;

		public SurfaceFormScope(AbstractFeatureTemplate<SurfaceFormScope> template, EntityType entityType,
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
			SurfaceFormScope other = (SurfaceFormScope) obj;
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

		@Override
		public String toString() {
			return entityType + ":" + surfaceForm;
		}

	}

	@Override
	public List<SurfaceFormScope> generateFactorScopes(State state) {
		List<SurfaceFormScope> factors = new ArrayList<>();

		for (AbstractAnnotation annotation : getPredictedAnnotations(state)) {

			if (annotation.isInstanceOfDocumentLinkedAnnotation()) {
				factors.add(new SurfaceFormScope(this, annotation.asInstanceOfLiteralAnnotation().getEntityType(),
						annotation.asInstanceOfLiteralAnnotation().getSurfaceForm()));
			} else if (annotation.isInstanceOfEntityTemplate()) {

				if (annotation.asInstanceOfEntityTemplate().getRootAnnotation().isInstanceOfLiteralAnnotation())
					factors.add(new SurfaceFormScope(this,
							annotation.asInstanceOfEntityTemplate().getRootAnnotation().getEntityType(),
							annotation.asInstanceOfEntityTemplate().getRootAnnotation().asInstanceOfLiteralAnnotation()
									.getSurfaceForm()));

				final EntityTemplateAnnotationFilter filter = annotation.asInstanceOfEntityTemplate().filter()
						.singleSlots().multiSlots().merge().nonEmpty().literalAnnoation().build();

				for (Entry<SlotType, Set<AbstractAnnotation>> slot : filter.getMergedAnnotations().entrySet()) {

					for (AbstractAnnotation slotFiller : slot.getValue()) {

						final LiteralAnnotation docLinkedAnnotation = slotFiller.asInstanceOfLiteralAnnotation();

						factors.add(new SurfaceFormScope(this, docLinkedAnnotation.getEntityType(),
								docLinkedAnnotation.getSurfaceForm()));
					}
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<SurfaceFormScope> factor) {

		Set<EntityType> entityTypes = new HashSet<>();

		entityTypes.add(factor.getFactorScope().entityType);
//		entityTypes.addAll(factor.getFactorScope().entityType.getTransitiveClosureSuperEntityTypes());

		factor.getFeatureVector().set(PREFIX + factor.getFactorScope().toString(), true);

	}

}

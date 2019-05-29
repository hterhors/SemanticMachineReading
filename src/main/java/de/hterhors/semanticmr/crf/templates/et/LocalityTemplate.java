package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.LocalityTemplate.LocalityScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class LocalityTemplate extends AbstractFeatureTemplate<LocalityScope> {

	static class LocalityScope extends AbstractFactorScope<LocalityScope> {

		public final EntityType parentEntity;
		public final EntityType childEntity;
		public final int sentenceDistance;

		public LocalityScope(AbstractFeatureTemplate<LocalityScope> template, EntityType parentEntity,
				EntityType childClass, int sentenceDistance) {
			super(template);
			this.parentEntity = parentEntity;
			this.childEntity = childClass;
			this.sentenceDistance = sentenceDistance;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((childEntity == null) ? 0 : childEntity.hashCode());
			result = prime * result + ((parentEntity == null) ? 0 : parentEntity.hashCode());
			result = prime * result + sentenceDistance;
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
			LocalityScope other = (LocalityScope) obj;
			if (childEntity == null) {
				if (other.childEntity != null)
					return false;
			} else if (!childEntity.equals(other.childEntity))
				return false;
			if (parentEntity == null) {
				if (other.parentEntity != null)
					return false;
			} else if (!parentEntity.equals(other.parentEntity))
				return false;
			if (sentenceDistance != other.sentenceDistance)
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
	public List<LocalityScope> generateFactorScopes(State state) {
		List<LocalityScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			if (annotation.getRootAnnotation().isInstanceOfDocumentLinkedAnnotation()) {

				final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
						.nonEmpty().docLinkedAnnoation().build();

				final EntityType rootEntityType = annotation.getEntityType();
				final int rootSenIndex = annotation.getRootAnnotation()
						.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSenTokenIndex();

				for (Set<AbstractAnnotation> mergedAnnotations : filter.getMergedAnnotations().values()) {

					for (AbstractAnnotation a : mergedAnnotations) {

						final EntityType slotFillerEntityType = a.getEntityType();
						final int slotFillerSenIndex = a.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0)
								.getSenTokenIndex();

						factors.add(new LocalityScope(this, rootEntityType, slotFillerEntityType,
								Math.abs(rootSenIndex - slotFillerSenIndex)));
					}
				}
			}

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<LocalityScope> factor) {

		for (EntityType pe : factor.getFactorScope().parentEntity.getTransitiveClosureSuperEntityTypes()) {

			for (EntityType ce : factor.getFactorScope().childEntity.getTransitiveClosureSuperEntityTypes()) {

				factor.getFeatureVector().set(pe.entityName + "->" + ce.entityName + " sentence dist = "
						+ factor.getFactorScope().sentenceDistance, true);
				factor.getFeatureVector().set(pe.entityName + "->" + ce.entityName + " sentence dist >= 4",
						factor.getFactorScope().sentenceDistance >= 4);
			}

		}

		factor.getFeatureVector()
				.set(factor.getFactorScope().parentEntity.entityName + "->"
						+ factor.getFactorScope().childEntity.entityName + " sentence dist = "
						+ factor.getFactorScope().sentenceDistance, true);
		factor.getFeatureVector()
				.set(factor.getFactorScope().parentEntity.entityName + "->"
						+ factor.getFactorScope().childEntity.entityName + " sentence dist >= 4",
						factor.getFactorScope().sentenceDistance >= 4);
	}

}

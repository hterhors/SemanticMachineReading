package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
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

		public final EntityType firstEntity;
		public final EntityType secondEntity;
		public final int sentenceDistance;

		public LocalityScope(AbstractFeatureTemplate<LocalityScope> template, EntityType parentEntity,
				EntityType childClass, int sentenceDistance) {
			super(template);
			this.firstEntity = parentEntity;
			this.secondEntity = childClass;
			this.sentenceDistance = sentenceDistance;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((secondEntity == null) ? 0 : secondEntity.hashCode());
			result = prime * result + ((firstEntity == null) ? 0 : firstEntity.hashCode());
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
			if (secondEntity == null) {
				if (other.secondEntity != null)
					return false;
			} else if (!secondEntity.equals(other.secondEntity))
				return false;
			if (firstEntity == null) {
				if (other.firstEntity != null)
					return false;
			} else if (!firstEntity.equals(other.firstEntity))
				return false;
			if (sentenceDistance != other.sentenceDistance)
				return false;
			return true;
		}

		@Override
		public int implementHashCode() {
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}

	}

	@Override
	public List<LocalityScope> generateFactorScopes(State state) {
		List<LocalityScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().docLinkedAnnoation().build();

			if (annotation.getRootAnnotation().isInstanceOfDocumentLinkedAnnotation()) {

				final EntityType rootEntityType = annotation.getEntityType();
				final int rootSenIndex = annotation.getRootAnnotation()
						.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex();

				for (Set<AbstractAnnotation> mergedAnnotations : filter.getMergedAnnotations().values()) {

					for (AbstractAnnotation a : mergedAnnotations) {

						final EntityType slotFillerEntityType = a.getEntityType();
						final int slotFillerSenIndex = a.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0)
								.getSentenceIndex();

						factors.add(new LocalityScope(this, rootEntityType, slotFillerEntityType,
								Math.abs(rootSenIndex - slotFillerSenIndex)));
					}
				}
			}

			List<AbstractAnnotation> mergedAnnotations = new ArrayList<>();

			for (SlotType key : filter.getMergedAnnotations().keySet()) {
				mergedAnnotations.addAll(filter.getMergedAnnotations().get(key));
			}

			Collections.sort(mergedAnnotations, new Comparator<AbstractAnnotation>() {

				@Override
				public int compare(AbstractAnnotation o1, AbstractAnnotation o2) {
					return o1.getEntityType().compareTo(o2.getEntityType());
				}
			});

			for (int i = 0; i < mergedAnnotations.size(); i++) {

				final EntityType rootEntityType = mergedAnnotations.get(i).getEntityType();
				final int rootSenIndex = mergedAnnotations.get(i).asInstanceOfDocumentLinkedAnnotation().relatedTokens
						.get(0).getSentenceIndex();
				for (int j = i + 1; j < mergedAnnotations.size(); j++) {

					final EntityType slotFillerEntityType = mergedAnnotations.get(j).getEntityType();
					final int slotFillerSenIndex = mergedAnnotations.get(j)
							.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex();

					factors.add(new LocalityScope(this, rootEntityType, slotFillerEntityType,
							Math.abs(rootSenIndex - slotFillerSenIndex)));
				}
			}

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<LocalityScope> factor) {

		for (EntityType pe : factor.getFactorScope().firstEntity.getDirectSuperEntityTypes()) {

			for (EntityType ce : factor.getFactorScope().secondEntity.getDirectSuperEntityTypes()) {

				factor.getFeatureVector().set(pe.entityName + "->" + ce.entityName + " sentence dist = "
						+ factor.getFactorScope().sentenceDistance, true);
				factor.getFeatureVector().set(pe.entityName + "->" + ce.entityName + " sentence dist >= 4",
						factor.getFactorScope().sentenceDistance >= 4);
			}

		}

		factor.getFeatureVector()
				.set(factor.getFactorScope().firstEntity.entityName + "->"
						+ factor.getFactorScope().secondEntity.entityName + " sentence dist = "
						+ factor.getFactorScope().sentenceDistance, true);
		factor.getFeatureVector()
				.set(factor.getFactorScope().firstEntity.entityName + "->"
						+ factor.getFactorScope().secondEntity.entityName + " sentence dist >= 4",
						factor.getFactorScope().sentenceDistance >= 4);
	}

}

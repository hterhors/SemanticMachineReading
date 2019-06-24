package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.function.library.max;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.ClusterTemplate.ClusterScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class ClusterTemplate extends AbstractFeatureTemplate<ClusterScope> {

	static class ClusterScope extends AbstractFactorScope<ClusterScope> {

		public final int sentenceSpread;

		public ClusterScope(AbstractFeatureTemplate<ClusterScope> template, int sentenceSpread) {
			super(template);
			this.sentenceSpread = sentenceSpread;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + sentenceSpread;
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
			ClusterScope other = (ClusterScope) obj;
			if (sentenceSpread != other.sentenceSpread)
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
	public List<ClusterScope> generateFactorScopes(State state) {
		List<ClusterScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			int maxSenIndex = 0;
			int minSenIndex = Integer.MAX_VALUE;

			if (annotation.getRootAnnotation().isInstanceOfDocumentLinkedAnnotation()) {
				final int rootSenIndex = annotation.getRootAnnotation()
						.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex();
				maxSenIndex = Math.max(maxSenIndex, rootSenIndex);
				minSenIndex = Math.min(minSenIndex, rootSenIndex);
			}
			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().docLinkedAnnoation().build();

			for (Set<AbstractAnnotation> mergedAnnotations : filter.getMergedAnnotations().values()) {
				for (AbstractAnnotation ma : mergedAnnotations) {

					int senIndex = ma.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex();
					maxSenIndex = Math.max(maxSenIndex, senIndex);
					minSenIndex = Math.min(minSenIndex, senIndex);
				}
			}

			
			factors.add(new ClusterScope(this, maxSenIndex - minSenIndex));

		}
		return factors;

	}

	@Override
	public void generateFeatureVector(Factor<ClusterScope> factor) {
		
		factor.getFeatureVector().set("Annotation Spread = 0", factor.getFactorScope().sentenceSpread == 0);
		factor.getFeatureVector().set("Annotation Spread != 0", factor.getFactorScope().sentenceSpread != 0);
//		factor.getFeatureVector().set("Annotation Spread = 1", factor.getFactorScope().sentenceSpread == 1);
//		factor.getFeatureVector().set("Annotation Spread = 2", factor.getFactorScope().sentenceSpread == 2);
	}

}

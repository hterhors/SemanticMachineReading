package de.hterhors.semanticmr.crf.templates.nerla;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.EmptyNerlaTemplate.MorphologicalNerlaScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class EmptyNerlaTemplate extends AbstractFeatureTemplate<MorphologicalNerlaScope, DocumentLinkedAnnotation> {

	static class MorphologicalNerlaScope
			extends AbstractFactorScope<MorphologicalNerlaScope, DocumentLinkedAnnotation> {

		public MorphologicalNerlaScope(
				AbstractFeatureTemplate<MorphologicalNerlaScope, DocumentLinkedAnnotation> template) {
			super(template);
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
	public List<MorphologicalNerlaScope> generateFactorScopes(State state) {
		List<MorphologicalNerlaScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : getPredictedAnnotations(state)) {

			factors.add(new MorphologicalNerlaScope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MorphologicalNerlaScope> factor) {

		factor.getFeatureVector().set("EMPTY", true);

	}

}

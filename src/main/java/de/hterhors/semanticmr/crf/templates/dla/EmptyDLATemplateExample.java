package de.hterhors.semanticmr.crf.templates.dla;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.dla.EmptyDLATemplateExample.Scope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class EmptyDLATemplateExample extends AbstractFeatureTemplate<Scope> {

	static class Scope extends AbstractFactorScope {

		public Scope(AbstractFeatureTemplate<Scope> template) {
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
	public List<Scope> generateFactorScopes(State state) {
		List<Scope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new Scope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<Scope> factor) {

		factor.getFeatureVector().set("EMPTY", true);

	}

}

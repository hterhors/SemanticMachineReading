package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.EmptyETTemplateExample.Scope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class EmptyETTemplateExample extends AbstractFeatureTemplate<Scope> {

	static class Scope extends AbstractFactorScope<Scope> {

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

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			factors.add(new Scope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<Scope> factor) {

		factor.getFeatureVector().set("EMPTY", true);

	}

}

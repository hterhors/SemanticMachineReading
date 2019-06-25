package de.hterhors.semanticmr.crf.templates.cardinality;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.cardinality.RootClassCardinalityTemplate.RootClassCardinalityScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class RootClassCardinalityTemplate extends AbstractFeatureTemplate<RootClassCardinalityScope> {

	static class RootClassCardinalityScope extends AbstractFactorScope {

		
		
		
		public RootClassCardinalityScope(AbstractFeatureTemplate<RootClassCardinalityScope> template) {
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
	public List<RootClassCardinalityScope> generateFactorScopes(State state) {
		List<RootClassCardinalityScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			factors.add(new RootClassCardinalityScope(this));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<RootClassCardinalityScope> factor) {

		factor.getFeatureVector().set("EMPTY", true);

	}

}

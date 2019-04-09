package de.hterhors.semanticmr.crf.variables;

import java.util.List;

import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;

public class VectorUtil {

	/**
	 * Computes the differences of all features of both states. For this, the
	 * template uses features from all factors that are not associated to both
	 * states (since these differences would be always 0).
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public static DoubleVector getFeatureDifferences(AbstractFactorTemplate template, State state1, State state2) {
		DoubleVector diff = new DoubleVector();

		List<Factor> factors1 = state1.getFactorGraph(template).getFactors();
		List<Factor> factors2 = state2.getFactorGraph(template).getFactors();

		for (Factor factor : factors1) {
			DoubleVector featureVector = factor.getFeatureVector();
			diff.add(featureVector);
		}
		for (Factor factor : factors2) {
			DoubleVector featureVector = factor.getFeatureVector();
			diff.sub(featureVector);
		}
		return diff;
	}
	
	

}

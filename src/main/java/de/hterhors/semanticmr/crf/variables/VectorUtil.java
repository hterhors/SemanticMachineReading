package de.hterhors.semanticmr.crf.variables;

import java.util.Collections;
import java.util.List;

import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;

/**
 * Vector utility class.
 * 
 * @author hterhors
 *
 */
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
	public static DoubleVector getFeatureDifferences(AbstractFeatureTemplate<?> template, State state1, State state2) {
		DoubleVector diff = new DoubleVector();

//		if (!(state1.getFactorGraph(template) != null && state2.getFactorGraph(template) != null))
//			return diff;

		List<Factor> factors1 = state1.getFactorGraph(template) != null ? state1.getFactorGraph(template).getCachedFactors()
				: Collections.emptyList();
		List<Factor> factors2 = state2.getFactorGraph(template) != null ? state2.getFactorGraph(template).getCachedFactors()
				: Collections.emptyList();

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

package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;

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
	public static Vector getFeatureDifferences(AbstractFactorTemplate template, State state1, State state2) {
		Vector diff = new Vector();
		Collection<Factor> factors1 = null;
		Collection<Factor> factors2 = null;
		try {
			factors1 = state1.getFactorGraph().getFactors();
			factors2 = state2.getFactorGraph().getFactors();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		for (Factor factor : factors1) {
			if (factor.getTemplate() == template) {
				Vector featureVector = factor.getFeatureVector();
				diff.addFAST(featureVector);
			}
		}
		for (Factor factor : factors2) {
			if (factor.getTemplate() == template) {
				Vector featureVector = factor.getFeatureVector();
				diff.subFAST(featureVector);
			}
		}
		return diff;
	}

}

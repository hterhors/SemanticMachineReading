package de.hterhors.semanticmr.crf.factor;

import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.crf.variables.Vector;

/**
 * A factor is an object that connects a feature vector to the variables that
 * are involved in computing this feature vector. Since the generation of a
 * factor and the actual computation of its features is separated into to steps,
 * you need to store/reference the variables you need for the computation of the
 * features inside the factorVariables object.
 *
 */
public class Factor {

	private final FactorScope factorScope;
	
	private final Vector features = new Vector();

	public Factor(FactorScope factorScope) {
		this.factorScope = factorScope;
	}

	public Vector getFeatureVector() {
		return features;
	}

	public FactorScope getFactorScope() {
		return factorScope;
	}

	public AbstractFactorTemplate getTemplate() {
		return factorScope.getTemplate();
	}

}

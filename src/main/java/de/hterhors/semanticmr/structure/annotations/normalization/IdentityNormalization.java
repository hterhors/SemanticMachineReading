package de.hterhors.semanticmr.structure.annotations.normalization;

/**
 * Evaluation function that returns the identity of the annotation of a slot.
 * 
 * @author hterhors
 *
 */
public class IdentityNormalization implements INormalizationFunction {

	private static IdentityNormalization instance = null;

	private IdentityNormalization() {
	}

	public static IdentityNormalization getInstance() {
		if (instance == null) {
			instance = new IdentityNormalization();

		}
		return instance;
	}

	public String normalize(String annotation) {
		return annotation;
	}

}

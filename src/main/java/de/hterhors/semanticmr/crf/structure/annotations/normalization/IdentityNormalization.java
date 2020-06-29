package de.hterhors.semanticmr.crf.structure.annotations.normalization;

/**
 * Evaluation function that returns the identity of the annotation of a slot.
 * 
 * @author hterhors
 *
 */
public class IdentityNormalization implements INormalizationFunction {

	private static IdentityNormalization instance = null;

	public static IdentityNormalization getInstance() {
		if (instance == null) {
			instance = new IdentityNormalization();
		}
		return instance;
	}

	@Override
	public String normalize(String annotation) {
		return annotation;
	}

}

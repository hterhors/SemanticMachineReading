package de.hterhors.semanticmr.examples.psink.normalization;

import de.hterhors.semanticmr.structure.annotations.normalization.INormalizationFunction;

public class WeightNormalization implements INormalizationFunction {

	@Override
	public String normalize(String annotation) {
		final String[] parts = annotation.toLowerCase().split("\\W");
		StringBuilder normalizedString = new StringBuilder();
		for (String part : parts) {
			normalizedString.append(part);
			normalizedString.append(" ");
		}
		return normalizedString.toString().trim();
	}

}

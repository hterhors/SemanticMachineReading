package de.hterhors.semanticmr.examples.psink.normalization;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;

public class WeightNormalization extends AbstractNormalizationFunction {

	public WeightNormalization(EntityType entityType) {
		super(entityType);
	}

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

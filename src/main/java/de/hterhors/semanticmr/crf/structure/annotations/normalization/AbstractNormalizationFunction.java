package de.hterhors.semanticmr.crf.structure.annotations.normalization;

import de.hterhors.semanticmr.crf.structure.EntityType;

public abstract class AbstractNormalizationFunction implements INormalizationFunction {

	final public EntityType entityType;

	public AbstractNormalizationFunction(EntityType entityType) {
		this.entityType = entityType;
	}

}

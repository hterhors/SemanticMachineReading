package de.hterhors.semanticmr.crf.exploration.constraints;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public abstract class AbstractHardConstraint {

	/**
	 * Validates the given EntityTemplate.
	 * 
	 * @param entityTemplate
	 * @return true if the implemented constraint is violated, else false.
	 */
	public abstract boolean violatesConstraint(EntityTemplate entityTemplate);

}

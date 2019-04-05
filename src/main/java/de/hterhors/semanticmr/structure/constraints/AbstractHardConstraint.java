package de.hterhors.semanticmr.structure.constraints;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public abstract class AbstractHardConstraint {

	/**
	 * Validates the given EntityTemplate.
	 * 
	 * @param entityTemplate
	 * @return true if the implemented constraint is violated, else false.
	 */
	abstract boolean violatesConstraint(EntityTemplate entityTemplate);

}

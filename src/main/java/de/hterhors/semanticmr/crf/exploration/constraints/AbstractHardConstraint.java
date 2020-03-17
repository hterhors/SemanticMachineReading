package de.hterhors.semanticmr.crf.exploration.constraints;

import java.util.List;

import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.variables.State;

public abstract class AbstractHardConstraint {

	class CandidateViolatesPair {
		public final EntityTemplate entityTemplate;
		public boolean violates;

		public CandidateViolatesPair(EntityTemplate entityTemplate, boolean violates) {
			this.entityTemplate = entityTemplate;
			this.violates = violates;
		}

	}

	/**
	 * Validates the given EntityTemplate.
	 * 
	 * @param entityTemplate
	 * @return true if the implemented constraint is violated, else false.
	 */
	public abstract boolean violatesConstraint(State currentState, EntityTemplate entityTemplate);

	/**
	 * 
	 * @param currentState         the current state
	 * @param candidateListToFilter
	 * 
	 * @return the filtered List
	 */
	public abstract List<EntityTemplate> violatesConstraint(State currentState, List<EntityTemplate> candidateListToFilter);

}

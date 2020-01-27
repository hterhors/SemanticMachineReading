package de.hterhors.semanticmr.crf.variables;

import java.util.Arrays;
import java.util.List;

/**
 * Interface or state initialization.
 * 
 * @author hterhors
 *
 */
public interface IStateInitializer {

	/**
	 * Returns an initial (empty) state for a given instance.
	 * 
	 * @param instance
	 * @return initial (empty) state
	 */
	public State getInitState(final Instance instance);

	default public List<State> getInitMultiStates(final Instance instance) {
		/**
		 * Add
		 */
		return Arrays.asList(getInitState(instance));
	}

}
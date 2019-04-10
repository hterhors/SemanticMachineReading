package de.hterhors.semanticmr.crf.variables;

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

}
package de.hterhors.semanticmr.crf.stopcrit;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

/**
 * Interface for training / sampling / exploration stopping criterion.
 * 
 * @author hterhors
 *
 */
public interface IStoppingCriterion {

	/**
	 * Returns true if the condition of stopping criterion is fulfilled
	 * 
	 * @param producedStateChain the produced state chain.
	 * 
	 * @return true if the condition of stopping criterion is fulfilled else false.
	 */
	public boolean checkCondition(final List<State> producedStateChain);

}

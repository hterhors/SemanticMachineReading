package de.hterhors.semanticmr.crf.stopcrit.impl;

import java.util.List;

import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * Checks for a given maximum chain length. This corresponds with the number of
 * sampling steps.
 * 
 * @author hterhors
 *
 */
public class MaxChainLength implements IStoppingCriterion {

	/**
	 * Maximum number of sampling steps / chain length.
	 */
	final public int maxStateChain;

	public MaxChainLength(final int maxSamplingSteps) {
		this.maxStateChain = maxSamplingSteps;
	}

	@Override
	public boolean checkCondition(List<State> producedStateChain) {
		return producedStateChain.size() == maxStateChain;
	}

}

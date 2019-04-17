package de.hterhors.semanticmr.crf.sampling.stopcrit.impl;

import java.util.List;

import de.hterhors.semanticmr.crf.sampling.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * Checks for a given maximum chain length. This corresponds with the number of
 * sampling steps.
 * 
 * @author hterhors
 *
 */
public class MaxChainLengthCrit implements IStoppingCriterion {

	/**
	 * Maximum number of sampling steps / chain length.
	 */
	final public int maxStateChain;

	public MaxChainLengthCrit(final int maxSamplingSteps) {
		this.maxStateChain = maxSamplingSteps;
	}

	@Override
	public boolean meetsCondition(List<State> producedStateChain) {
		return producedStateChain.size() == maxStateChain;
	}

}

package de.hterhors.semanticmr.crf.stopcrit.impl;

import java.util.List;

import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.State;

public class MaxSamplingSteps implements IStoppingCriterion {

	final public int maxSamplingSteps;

	public MaxSamplingSteps(final int maxSamplingSteps) {
		this.maxSamplingSteps = maxSamplingSteps;
	}

	@Override
	public boolean checkCondition(List<State> producedStateChain) {
		return producedStateChain.size() == maxSamplingSteps;
	}

}

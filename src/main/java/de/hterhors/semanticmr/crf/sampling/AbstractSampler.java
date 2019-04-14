package de.hterhors.semanticmr.crf.sampling;

import java.util.List;

import de.hterhors.semanticmr.crf.sampling.impl.DefaultSamplingStrategy;
import de.hterhors.semanticmr.crf.variables.State;

public abstract class AbstractSampler {

	final protected ISamplingStrategy samplingStrategy;

	protected int currentEpoch;

	public AbstractSampler(ISamplingStrategy samplingStrategy) {
		this.samplingStrategy = samplingStrategy;
	}

	public AbstractSampler(final boolean sampleBasedOnObjectiveFunction) {
		this.samplingStrategy = new DefaultSamplingStrategy(sampleBasedOnObjectiveFunction);
	}

	public abstract State sampleCandidate(List<State> proposalStates);

	public boolean sampleBasedOnObjectiveScore(int epoch) {
		this.currentEpoch = epoch;
		return samplingStrategy.sampleBasedOnObjectiveScore(epoch);
	}

	public abstract AcceptStrategy getAcceptanceStrategy(int epoch);

}
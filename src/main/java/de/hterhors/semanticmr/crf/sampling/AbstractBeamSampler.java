package de.hterhors.semanticmr.crf.sampling;

import java.util.List;

import de.hterhors.semanticmr.crf.StatePair;
import de.hterhors.semanticmr.crf.sampling.impl.beam.DefaultBeamSamplingStrategy;

public abstract class AbstractBeamSampler {

	final protected IBeamSamplingStrategy samplingStrategy;

	protected int currentEpoch;

	public AbstractBeamSampler(IBeamSamplingStrategy samplingStrategy) {
		this.samplingStrategy = samplingStrategy;
	}

	public AbstractBeamSampler(final boolean sampleBasedOnObjectiveFunction) {
		this.samplingStrategy = new DefaultBeamSamplingStrategy(sampleBasedOnObjectiveFunction);
	}

	public abstract List<StatePair> sampleCandidate(List<StatePair> proposalStates, int beamSize);

	public boolean sampleBasedOnObjectiveScore(int epoch) {
		this.currentEpoch = epoch;
		return samplingStrategy.sampleBasedOnObjectiveScore(epoch);
	}

	public abstract AcceptStrategy getAcceptanceStrategy(int epoch);

}
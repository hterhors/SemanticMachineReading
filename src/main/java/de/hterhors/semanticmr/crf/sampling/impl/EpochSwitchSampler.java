package de.hterhors.semanticmr.crf.sampling.impl;

import java.util.List;

import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;
import de.hterhors.semanticmr.crf.sampling.ISamplingStrategy;
import de.hterhors.semanticmr.crf.variables.State;

public class EpochSwitchSampler extends AbstractSampler {

	public EpochSwitchSampler(ISamplingStrategy samplingStrategy) {
		super(samplingStrategy);
	}

	public EpochSwitchSampler() {
		super(new SwitchSamplingStrategy());
	}

	@Override
	public State sampleCandidate(List<State> candidates) {
		if (sampleBasedOnObjectiveScore(currentEpoch)) {
			return candidates.stream().max((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore()))
					.get();
		} else {
			return candidates.stream().max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore())).get();
		}
	}

	@Override
	public AcceptStrategy getAcceptanceStrategy(int epoch) {
		if (sampleBasedOnObjectiveScore(currentEpoch)) {
			return AcceptStrategies.strictObjectiveAccept();
		} else {
			return AcceptStrategies.strictModelAccept();
		}
	}

}

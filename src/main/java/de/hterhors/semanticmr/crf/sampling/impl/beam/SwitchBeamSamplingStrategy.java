package de.hterhors.semanticmr.crf.sampling.impl.beam;

import de.hterhors.semanticmr.crf.sampling.IBeamSamplingStrategy;

public class SwitchBeamSamplingStrategy implements IBeamSamplingStrategy {
	@Override
	public boolean sampleBasedOnObjectiveScore(int epoch) {
		return epoch % 2 == 0;
	}
}

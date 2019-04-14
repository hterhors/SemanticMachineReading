package de.hterhors.semanticmr.crf.sampling.impl;

import de.hterhors.semanticmr.crf.sampling.ISamplingStrategy;

public class SwitchSamplingStrategy implements ISamplingStrategy {
	@Override
	public boolean sampleBasedOnObjectiveScore(int epoch) {
		return epoch % 2 == 0;
	}
}

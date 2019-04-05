package de.hterhors.semanticmr.crf.sampling.impl;

import java.util.Random;

import de.hterhors.semanticmr.crf.sampling.ISamplingStrategy;

/**
 * Switches between model score and objective score randomly.
 * 
 * @author hterhors
 *
 */
public class RandomSwitchSamplingStrategy implements ISamplingStrategy {

	final Random random;

	public RandomSwitchSamplingStrategy() {
		random = new Random();
	}

	public RandomSwitchSamplingStrategy(final long seed) {
		this.random = new Random(seed);
	}

	@Override
	public boolean sampleBasedOnObjectiveScore(int epoch) {
		return random.nextBoolean();
	}

}

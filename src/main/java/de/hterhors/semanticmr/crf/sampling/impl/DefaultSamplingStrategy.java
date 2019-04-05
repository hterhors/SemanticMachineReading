package de.hterhors.semanticmr.crf.sampling.impl;

import de.hterhors.semanticmr.crf.sampling.ISamplingStrategy;

/**
 * Switches between model score and objective score randomly.
 * 
 * @author hterhors
 *
 */
public class DefaultSamplingStrategy implements ISamplingStrategy {

	final boolean sampleWithObjectiveFunction;

	public DefaultSamplingStrategy(final boolean sampleWithObjectiveFunction) {
		this.sampleWithObjectiveFunction = sampleWithObjectiveFunction;
	}

	@Override
	public boolean sampleBasedOnObjectiveScore(int epoch) {
		return sampleWithObjectiveFunction;
	}

}

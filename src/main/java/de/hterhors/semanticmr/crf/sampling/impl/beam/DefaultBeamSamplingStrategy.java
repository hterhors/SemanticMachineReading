package de.hterhors.semanticmr.crf.sampling.impl.beam;

import de.hterhors.semanticmr.crf.sampling.IBeamSamplingStrategy;

/**
 * Switches between model score and objective score randomly.
 * 
 * @author hterhors
 *
 */
public class DefaultBeamSamplingStrategy implements IBeamSamplingStrategy {

	final boolean sampleWithObjectiveFunction;

	public DefaultBeamSamplingStrategy(final boolean sampleWithObjectiveFunction) {
		this.sampleWithObjectiveFunction = sampleWithObjectiveFunction;
	}

	@Override
	public boolean sampleBasedOnObjectiveScore(int epoch) {
		return sampleWithObjectiveFunction;
	}

}

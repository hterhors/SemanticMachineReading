package de.hterhors.semanticmr.crf.sampling;

public interface IBeamSamplingStrategy {

	public boolean sampleBasedOnObjectiveScore(final int epoch);

}

package de.hterhors.semanticmr.crf.sampling;

public interface ISamplingStrategy {

	public boolean sampleBasedOnObjectiveScore(final int epoch);

}

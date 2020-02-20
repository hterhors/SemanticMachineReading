package de.hterhors.semanticmr.crf.sampling.stopcrit.impl;

import java.util.List;
import java.util.function.Function;

import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.State;

public class ConverganceCrit implements ISamplingStoppingCriterion {

	final private double threshold;

	final private int maxTimesNoChange;
	final private Function<State, Double> f;

	public ConverganceCrit(final int maxTimesNoChange, Function<State, Double> f) {
		this(maxTimesNoChange, f, 0.00001);
	}

	public ConverganceCrit(final int maxTimesNoChange, Function<State, Double> f, final double threshold) {
		if (maxTimesNoChange < 1)
			throw new IllegalArgumentException(
					"The parameter maxTimesNoChange must be larger than 1, given value: " + maxTimesNoChange);
		this.maxTimesNoChange = maxTimesNoChange;
		this.threshold = threshold;
		this.f = f;
	}

	@Override
	public boolean meetsCondition(List<State> producedStateChain) {

		if (producedStateChain.size() < maxTimesNoChange)
			return false;

		final double latestValue = f.apply(producedStateChain.get(producedStateChain.size() - 1));

		int countNoChange = 0;

		
		
		for (int i = producedStateChain.size() - 2; i >= 0; i--) {
			final double v = f.apply(producedStateChain.get(i)).doubleValue();

			if (Math.abs(latestValue - v) <= threshold) {
				countNoChange++;

				if (countNoChange == maxTimesNoChange)
					return true;
			}
		}

		return false;
	}

}

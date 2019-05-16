package de.hterhors.semanticmr.crf.sampling.stopcrit.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.State;

public class NoEvalChangeCrit implements ITrainingStoppingCriterion {

	public static Logger log = LogManager.getFormatterLogger(NoEvalChangeCrit.class.getSimpleName());

	private static final int MIN_EPOCH_TRAINING = 1;
	public List<Double> prevMeans = new ArrayList<>();
	double threshold = 0.001;
	protected static final int TIMES_SAME_EVALUATION_SCORE = 3;

	/**
	 * The best epoch is determined by minimum epoch and minimum error.
	 */
	public int bestEpoch = 0;

	private double bestPerformance = 0;

	private boolean meetsCondition(final double mean) {
		log.info("Previous run performance, mean values = ");
		prevMeans.forEach(log::info);
		log.info("Current epoch = " + (prevMeans.size() + 1));
		log.info("Current run performance, mean value = " + mean);

		if (prevMeans.size() >= MIN_EPOCH_TRAINING) {

			for (int i = prevMeans.size() - 1; i >= prevMeans.size() - TIMES_SAME_EVALUATION_SCORE; i--) {

				final double prevMean = prevMeans.get(i);
				if (Math.abs(mean - prevMean) <= threshold) {
				} else {
					return false;
				}
			}
			log.info("Stop training due to no change in evaluation results for " + TIMES_SAME_EVALUATION_SCORE
					+ " times.");
			log.info("Best epoch = " + bestEpoch);
			return true;
		}

		if (mean > bestPerformance) {
			bestEpoch = prevMeans.size();
			bestPerformance = mean;
		}

		prevMeans.add(mean);

		log.info("Best epoch: " + bestEpoch + " with performance: " + bestPerformance);

		return false;
	}

	@Override
	public boolean meetsCondition(Collection<State> producedStateChain) {
		double mean = 0;
		for (State state : producedStateChain) {
			mean += state.getObjectiveScore();

		}
		mean /= producedStateChain.size();

		return meetsCondition(mean);
	}

}

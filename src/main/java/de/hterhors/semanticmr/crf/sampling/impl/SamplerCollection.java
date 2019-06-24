package de.hterhors.semanticmr.crf.sampling.impl;

import java.util.List;

import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;
import de.hterhors.semanticmr.crf.sampling.SamplingUtils;
import de.hterhors.semanticmr.crf.variables.State;

public class SamplerCollection {
	public static AbstractSampler greedyModelStrategy() {
		return new AbstractSampler(false) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				return candidates.stream().max((s1, s2) -> Double.compare(s1.getModelScore(), s2.getModelScore()))
						.get();
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictModelAccept();
			}

		};
	};

	public static AbstractSampler greedyObjectiveStrategy() {
		return new AbstractSampler(true) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				return candidates.stream()
						.max((s1, s2) -> Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore())).get();
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictObjectiveAccept();
			}
		};
	}

	public static AbstractSampler topKModelDistributionSamplingStrategy(int k) {
		return new AbstractSampler(false) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				candidates.sort(Model.modelScoreComparator);
				return SamplingUtils.drawFromDistribution(candidates.subList(0, k), true);
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictModelAccept();
			}

		};
	}

	public static AbstractSampler topKObjectiveDistributionSamplingStrategy(int k) {
		return new AbstractSampler(true) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				candidates.sort(Model.objectiveScoreComparator);
				return SamplingUtils.drawFromDistribution(candidates.subList(0, k), false);
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictObjectiveAccept();
			}
		};
	}

	public static AbstractSampler linearModelSamplingStrategy() {
		return new AbstractSampler(false) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				return SamplingUtils.drawFromDistribution(candidates, true);
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictModelAccept();
			}
		};
	}

	public static AbstractSampler linearObjectiveSamplingStrategy() {
		return new AbstractSampler(true) {

			@Override
			public State sampleCandidate(List<State> candidates) {
				return SamplingUtils.drawFromDistribution(candidates, false);
			}

			@Override
			public AcceptStrategy getAcceptanceStrategy(int epoch) {
				return AcceptStrategies.strictObjectiveAccept();
			}
		};
	}

}

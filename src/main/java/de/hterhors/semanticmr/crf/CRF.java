package de.hterhors.semanticmr.crf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class CRF {

	private static class CRFStatistics {
		private final String context;

		private long startTrainingTime;
		private long endTrainingTime;

		public CRFStatistics(String context) {
			this.context = context;
		}

		private long getTotalDuration() {
			return endTrainingTime - startTrainingTime;
		}

		@Override
		public String toString() {
			return "CRFStatistics [context=" + context + ", getTotalDuration()=" + getTotalDuration() + "]";
		}

	}

	/**
	 * The maximum number of sampling steps per instance. This prevents infinite
	 * loops if no stopping criterion ever matches.
	 */
	final static public int MAX_SAMPLING = 100;

	final EntityTemplateExploration explorer;

	final Model model;

	final ObjectiveFunction objectiveFunction;

	final AbstractSampler sampler;

	private final IStateInitializer initializer;

	private CRFStatistics trainingStatistics;

	private CRFStatistics testStatistics;

	public CRF(Model model, EntityTemplateExploration explorer, AbstractSampler sampler, IStateInitializer initializer,
			ObjectiveFunction objectiveFunction) {
		this.model = model;
		this.explorer = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
		this.initializer = initializer;
		this.trainingStatistics = new CRFStatistics("Train");
		this.testStatistics = new CRFStatistics("Test");
	}

	public Map<Instance, State> train(List<Instance> trainingInstances, final int numberOfEpochs,
			IStoppingCriterion... stoppingCriterion) {
		this.trainingStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {

			final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

			for (Instance instance : trainingInstances) {
//				System.out.println(instance.getName());
//				System.out.println(instance.getGoldAnnotations().getAnnotations().get(0).toPrettyString());
//				System.out.println();

				final List<State> producedStateChain = new ArrayList<>();

				State currentState = initializer.getInitState(instance);
				objectiveFunction.score(currentState);
				finalStates.put(instance, currentState);
				producedStateChain.add(currentState);

				for (int samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {
//					System.out.println();
//					System.out.println(currentState.getCurrentPredictions().getAnnotations().get(0).toPrettyString());
//					System.out.println();

					final List<State> proposalStates = explorer.explore(currentState);

					scoreProposalStates(sampleBasedOnObjectiveFunction, proposalStates);

					final State candidateState = selectCandidateState(proposalStates);

//					System.out.println(candidateState.getCurrentPredictions().getAnnotations().get(0).toPrettyString());
//					System.out.println();
					scoreSelectedStates(sampleBasedOnObjectiveFunction, currentState, candidateState);

					currentState = selectNextState(epoch, currentState, candidateState);

					producedStateChain.add(currentState);

					finalStates.put(instance, currentState);

					if (meetsStoppingCriterion(stoppingCriterion, producedStateChain))
						break;

				}
			}
		}
		this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;
	}

	private boolean meetsStoppingCriterion(IStoppingCriterion[] stoppingCriterion,
			final List<State> producedStateChain) {
		for (IStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain))
				return true;
		}
		return false;
	}

	private State selectCandidateState(final List<State> proposalStates) {
		return sampler.sampleCandidate(proposalStates);
	}

	private State selectNextState(int epoch, State currentState, State candidateState) {
		boolean isAccepted = sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState, currentState);

		if (isAccepted) {
			model.updateWeights(currentState, candidateState);
			currentState = candidateState;
		}

		return currentState;
	}

	private void scoreSelectedStates(final boolean sampleBasedOnObjectiveFunction, State currentState,
			State candidateState) {
		if (sampleBasedOnObjectiveFunction) {
			model.score(candidateState);
		} else {
			objectiveFunction.score(candidateState);
			objectiveFunction.score(currentState);
		}
	}

	private void scoreProposalStates(final boolean sampleBasedOnObjectiveFunction, final List<State> proposalStates) {
		if (sampleBasedOnObjectiveFunction) {
			objectiveFunction.score(proposalStates);
//			System.out.println("#############################################");
//			for (State state : proposalStates) {
//				System.out.println(state.getObjectiveScore() + ":"
//						+ state.getCurrentPredictions().getAnnotations().get(0).toPrettyString());
//			}
//			System.out.println("#############################################");
		} else {
			model.score(proposalStates);
		}
	}

	public void printTrainingStatistics(final PrintStream ps) {
		ps.println(this.trainingStatistics);
	}

	public void printTestStatistics(final PrintStream ps) {
		ps.println(this.testStatistics);
	}

	public Map<Instance, State> test(List<Instance> testInstances, IStoppingCriterion... stoppingCriterion) {
		this.testStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		for (Instance instance : testInstances) {
//				System.out.println(instance.getName());
//				System.out.println(instance.getGoldAnnotations().getAnnotations().get(0).toPrettyString());
//				System.out.println();

			final List<State> producedStateChain = new ArrayList<>();

			State currentState = initializer.getInitState(instance);
			objectiveFunction.score(currentState);
			finalStates.put(instance, currentState);
			producedStateChain.add(currentState);

			for (int samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {
//					System.out.println();
//					System.out.println(currentState.getCurrentPredictions().getAnnotations().get(0).toPrettyString());
//					System.out.println();

				final List<State> proposalStates = explorer.explore(currentState);

				model.score(proposalStates);

				final State candidateState = SamplerCollection.greedyModelStrategy().sampleCandidate(proposalStates);

				boolean accepted = AcceptStrategies.strictModelAccept().isAccepted(candidateState, currentState);

				if (accepted) {
					currentState = candidateState;
					objectiveFunction.score(currentState);
				}

				producedStateChain.add(currentState);

				finalStates.put(instance, currentState);

				if (meetsStoppingCriterion(stoppingCriterion, producedStateChain))
					break;

			}
		}
		this.testStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;
	}
}

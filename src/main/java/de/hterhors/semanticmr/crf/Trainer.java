package de.hterhors.semanticmr.crf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.hterhors.semanticmr.crf.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class Trainer {

	private static class TrainingStatistics {
		private long startTrainingTime;
		private long endTrainingTime;

		private long getTotalTrainingDuration() {
			return endTrainingTime - startTrainingTime;
		}

		@Override
		public String toString() {
			return "TrainingStatistics [getTotalTrainingDuration()=" + getTotalTrainingDuration() + "]";
		}

	}

	final int numberOfEpochs;

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

	private final IStoppingCriterion stoppingCriterion;

	private TrainingStatistics trainingStatistics;

	public Trainer(Model model, EntityTemplateExploration explorer, AbstractSampler sampler,
			IStateInitializer initializer, IStoppingCriterion stoppingCriterion, ObjectiveFunction objectiveFunction,
			final int numberOfEpochs) {
		this.numberOfEpochs = numberOfEpochs;
		this.stoppingCriterion = stoppingCriterion;
		this.model = model;
		this.explorer = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
		this.initializer = initializer;
		this.trainingStatistics = new TrainingStatistics();
	}

	public Map<Instance, State> trainModel(List<Instance> trainingInstances) {
		this.trainingStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {

			for (Instance instance : trainingInstances) {

				final List<State> producedStateChain = new ArrayList<>();

				State currentState = initializer.getInitState(instance);
				objectiveFunction.score(currentState);
				producedStateChain.add(currentState);

				for (int samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

					final List<State> proposalStates = explorer.explore(currentState);

					final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

					if (sampleBasedOnObjectiveFunction) {
						objectiveFunction.score(proposalStates);
					} else {
						model.score(proposalStates);
					}

					State candidateState = sampler.sampleCandidate(proposalStates);

					if (sampleBasedOnObjectiveFunction) {
						model.score(candidateState);
					} else {
						objectiveFunction.score(candidateState);
						objectiveFunction.score(currentState);
					}

					boolean isAccepted = sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState, currentState);

					if (isAccepted) {
						model.updateWeights(currentState, candidateState);
						currentState = candidateState;
					}

					producedStateChain.add(currentState);

					if (stoppingCriterion.checkCondition(producedStateChain))
						break;

				}
				finalStates.put(instance, producedStateChain.get(producedStateChain.size() - 1));
			}
		}
		this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;
	}

	public void printTrainingStatistics(final PrintStream ps) {
		ps.println(this.trainingStatistics);
	}
}

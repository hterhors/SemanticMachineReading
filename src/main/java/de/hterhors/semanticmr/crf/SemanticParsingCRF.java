package de.hterhors.semanticmr.crf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class SemanticParsingCRF {

	private static Logger log = LogManager.getFormatterLogger(SemanticParsingCRF.class);

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

	private static final String TRAIN_CONTEXT = "===========TRAIN============\n";
	private static final String TEST_CONTEXT = "===========TEST============\n";

	final IExplorationStrategy explorer;

	final Model model;

	final IObjectiveFunction objectiveFunction;

	final AbstractSampler sampler;

	private final IStateInitializer initializer;

	private CRFStatistics trainingStatistics;

	private CRFStatistics testStatistics;

	public SemanticParsingCRF(Model model, IExplorationStrategy explorer, AbstractSampler sampler,
			IStateInitializer initializer, IObjectiveFunction objectiveFunction) {
		this.model = model;
		this.explorer = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
		this.initializer = initializer;
		this.trainingStatistics = new CRFStatistics("Train");
		this.testStatistics = new CRFStatistics("Test");
	}

	public Map<Instance, State> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final ISamplingStoppingCriterion[] samplingStoppingCrits) {
		return train(learner, trainingInstances, numberOfEpochs, new ITrainingStoppingCriterion[] {},
				samplingStoppingCrits);

	}

	public Map<Instance, State> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final ITrainingStoppingCriterion[] trainingStoppingCrits) {
		return train(learner, trainingInstances, numberOfEpochs, trainingStoppingCrits,
				new ISamplingStoppingCriterion[] {});
	}

	public Map<Instance, State> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final ITrainingStoppingCriterion[] trainingStoppingCrits,
			final ISamplingStoppingCriterion[] samplingStoppingCrits) {

		log.info("Start training procedure...");

		this.trainingStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {

			log.info("############");
			log.info("# Epoch: " + epoch + " #");
			log.info("############");

			final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

			int instanceIndex = 0;

			for (Instance instance : trainingInstances) {
				final List<State> producedStateChain = new ArrayList<>();

				State currentState = initializer.getInitState(instance);
				objectiveFunction.score(currentState);
				finalStates.put(instance, currentState);
				producedStateChain.add(currentState);
				int samplingStep;
				for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

					final List<State> proposalStates = explorer.explore(currentState);

					if (proposalStates.isEmpty())
						proposalStates.add(currentState);

					if (sampleBasedOnObjectiveFunction) {
						objectiveFunction.score(proposalStates);
					} else {
						model.score(proposalStates);
					}

					final State candidateState = sampler.sampleCandidate(proposalStates);

					scoreSelectedStates(sampleBasedOnObjectiveFunction, currentState, candidateState);

					boolean isAccepted = sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState, currentState);

					if (isAccepted) {
						model.updateWeights(learner, currentState, candidateState);
						currentState = candidateState;
					}

					producedStateChain.add(currentState);

					finalStates.put(instance, currentState);

					if (meetsSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain))
						break;

				}
				this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
				LogUtils.logState(log,
						TRAIN_CONTEXT + " [" + (epoch + 1) + "/" + numberOfEpochs + "]" + "[" + ++instanceIndex + "/"
								+ trainingInstances.size() + "]" + "[" + (samplingStep + 1) + "]",
						instance, currentState);
				log.info("Time: " + this.trainingStatistics.getTotalDuration());
			}

			if (meetsTrainingStoppingCriterion(trainingStoppingCrits, finalStates))
				break;

		}
		this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;
	}

	private boolean meetsSamplingStoppingCriterion(ISamplingStoppingCriterion[] stoppingCriterion,
			final List<State> producedStateChain) {
		for (ISamplingStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain))
				return true;
		}
		return false;
	}

	private boolean meetsTrainingStoppingCriterion(ITrainingStoppingCriterion[] stoppingCriterion,
			final Map<Instance, State> producedStateChain) {
		for (ITrainingStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain.values()))
				return true;
		}
		return false;
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

	public CRFStatistics getTrainingStatistics() {
		return this.trainingStatistics;
	}

	public CRFStatistics getTestStatistics() {
		return this.testStatistics;
	}

	public Map<Instance, State> test(List<Instance> testInstances, ISamplingStoppingCriterion... stoppingCriterion) {
		this.testStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		int instanceIndex = 0;
		for (Instance instance : testInstances) {

			final List<State> producedStateChain = new ArrayList<>();

			State currentState = initializer.getInitState(instance);
			objectiveFunction.score(currentState);
			finalStates.put(instance, currentState);
			producedStateChain.add(currentState);
			int samplingStep;
			for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

				final List<State> proposalStates = explorer.explore(currentState);

				if (proposalStates.isEmpty())
					proposalStates.add(currentState);

				model.score(proposalStates);

				final State candidateState = SamplerCollection.greedyModelStrategy().sampleCandidate(proposalStates);

				boolean accepted = AcceptStrategies.strictModelAccept().isAccepted(candidateState, currentState);

				if (accepted) {
					currentState = candidateState;
					objectiveFunction.score(currentState);
				}

				producedStateChain.add(currentState);

				finalStates.put(instance, currentState);

				if (meetsSamplingStoppingCriterion(stoppingCriterion, producedStateChain))
					break;

			}
			this.testStatistics.endTrainingTime = System.currentTimeMillis();
			LogUtils.logState(log,
					TEST_CONTEXT + "[" + ++instanceIndex + "/" + testInstances.size() + "] [" + samplingStep + "]",
					instance, currentState);
			log.info("Time: " + this.testStatistics.getTotalDuration());

		}
		this.testStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;
	}

}

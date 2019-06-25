package de.hterhors.semanticmr.crf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.model.FactorGraph;
import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class SemanticParsingCRF {
	public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");

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

	private static final String COVERAGE_CONTEXT = "===========COVERAGE============\n";
	private static final String TRAIN_CONTEXT = "===========TRAIN============\n";
	private static final String TEST_CONTEXT = "===========TEST============\n";

	final List<IExplorationStrategy> explorerList;

	final Model model;

	final IObjectiveFunction objectiveFunction;

	final AbstractSampler sampler;

	private final IStateInitializer initializer;

	private CRFStatistics trainingStatistics;

	private CRFStatistics testStatistics;

	public SemanticParsingCRF(Model model, IExplorationStrategy explorer, AbstractSampler sampler,
			IStateInitializer initializer, IObjectiveFunction objectiveFunction) {
		this(model, Arrays.asList(explorer), sampler, initializer, objectiveFunction);
	}

	public SemanticParsingCRF(Model model, List<IExplorationStrategy> explorer, AbstractSampler sampler,
			IStateInitializer initializer, IObjectiveFunction objectiveFunction) {
		this.model = model;
		this.explorerList = explorer;
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
			log.info("# Epoch: " + (epoch + 1) + " #");
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

					for (IExplorationStrategy explorer : explorerList) {

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

						boolean isAccepted = sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState,
								currentState);

						if (isAccepted) {
							model.updateWeights(learner, currentState, candidateState);
							currentState = candidateState;
						}

						producedStateChain.add(currentState);

						finalStates.put(instance, currentState);

					}
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

	public Map<Instance, State> predict(Model model, List<Instance> testInstances,
			ISamplingStoppingCriterion... stoppingCriterion) {
		this.testStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		int instanceIndex = 0;
		for (Instance instance : testInstances) {

			final List<State> producedStateChain = new ArrayList<>();

			State currentState = initializer.getInitState(instance);
			finalStates.put(instance, currentState);
			objectiveFunction.score(currentState);
			producedStateChain.add(currentState);
			int samplingStep;
			for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

				for (IExplorationStrategy explorer : explorerList) {

					final List<State> proposalStates = explorer.explore(currentState);

					if (proposalStates.isEmpty())
						proposalStates.add(currentState);

					model.score(proposalStates);

					final State candidateState = SamplerCollection.greedyModelStrategy()
							.sampleCandidate(proposalStates);

					boolean accepted = AcceptStrategies.strictModelAccept().isAccepted(candidateState, currentState);

//					{
//						Collections.sort(proposalStates, Model.modelScoreComparator);
//
//						for (int i = 0; i < proposalStates.size(); i++) {
//							log.info("Index: " + i);
//							compare(currentState, proposalStates.get(i));
//						}
//
//						log.info("SampledState: ");
//						compare(currentState, candidateState);
//					}

					if (accepted) {
						currentState = candidateState;
						objectiveFunction.score(currentState);
					}

					producedStateChain.add(currentState);

					finalStates.put(instance, currentState);
				}
				if (meetsSamplingStoppingCriterion(stoppingCriterion, producedStateChain)) {
					break;
				}
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

	public Map<Instance, State> predict(List<Instance> testInstances, ISamplingStoppingCriterion... stoppingCriterion) {
		return predict(this.model, testInstances, stoppingCriterion);
	}

	private void compare(State currentState, State candidateState) {

		Map<String, Double> differences = getDifferences(collectFeatures(currentState),
				collectFeatures(candidateState));
		if (differences.isEmpty())
			return;

		List<Entry<String, Double>> sortedWeightsPrevState = new ArrayList<>(collectFeatures(currentState).entrySet());
		List<Entry<String, Double>> sortedWeightsCandState = new ArrayList<>(
				collectFeatures(candidateState).entrySet());

		Collections.sort(sortedWeightsPrevState, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
		Collections.sort(sortedWeightsCandState, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));

		log.info(currentState.getInstance().getName());
		log.info("_____________GoldAnnotations:_____________");
		log.info(currentState.getGoldAnnotations());
		log.info("_____________PrevState:_____________");
		sortedWeightsPrevState.stream().filter(k -> differences.containsKey(k.getKey())).forEach(log::info);
		log.info("ModelScore: " + currentState.getModelScore() + ": " + currentState.getCurrentPredictions());
		log.info("_____________CandState:_____________");
		sortedWeightsCandState.stream().filter(k -> differences.containsKey(k.getKey())).forEach(log::info);
		log.info("ModelScore: " + candidateState.getModelScore() + ": " + candidateState.getCurrentPredictions());
		log.info("------------------");
	}

	@SuppressWarnings("boxing")
	public Map<String, Double> getDifferences(Map<String, Double> currentFeatures,
			Map<String, Double> candidateFeatures) {
		Map<String, Double> differences = new HashMap<>();

		Set<String> keys = new HashSet<>();

		keys.addAll(candidateFeatures.keySet());
		keys.addAll(currentFeatures.keySet());

		for (String key : keys) {

			if (candidateFeatures.containsKey(key) && currentFeatures.containsKey(key)) {
				double diff = 0;
				if ((diff = Math.abs(currentFeatures.get(key) - candidateFeatures.get(key))) != 0.0D) {
					// This should or can not happen as feature weights are shared throughout states
					differences.put(key, diff);
				}
			} else if (currentFeatures.containsKey(key)) {
				differences.put(key, currentFeatures.get(key));
			} else {
				differences.put(key, candidateFeatures.get(key));
			}

		}
		return differences;
	}

	@SuppressWarnings("boxing")
	public Map<String, Double> collectFeatures(State currentState) {
		Map<String, Double> features = new HashMap<>();

		for (FactorGraph fg : currentState.getFactorGraphs()) {
			for (Factor f : fg.getFactors()) {

				for (Entry<Integer, Double> feature : f.getFeatureVector().getFeatures().entrySet()) {
					if (f.getFactorScope().getTemplate().getWeights().getFeatures().containsKey(feature.getKey()))
						features.put(
								f.getFactorScope().getTemplate().getClass().getSimpleName() + ":"
										+ Model.getFeatureForIndex(feature.getKey()),
								feature.getValue() * f.getFactorScope().getTemplate().getWeights().getFeatures()
										.get(feature.getKey()));

				}
			}
		}
		return features;
	}

	/**
	 * 
	 * Computes the coverage of the given instances. The coverage is defined by the
	 * objective mean score that can be reached relying on greedy objective function
	 * sampling strategy. The coverage can be seen as the upper bound of the system.
	 * The upper bound depends only on the exploration strategy, e.g. the provided
	 * NER-annotations during slot-filling.
	 * 
	 * @param printDetailedLog whether detailed log should be printed or not.
	 * @param predictionOF
	 * @param instances        the instances to compute the coverage on.
	 * @return a score that contains information of the coverage.
	 */
	public Score computeCoverage(final boolean printDetailedLog, IObjectiveFunction predictionOF,
			final List<Instance> instances) {

		log.info("Compute coverage...");

		ISamplingStoppingCriterion[] noObjectiveChangeCrit = new ISamplingStoppingCriterion[] {
				new ConverganceCrit(explorerList.size(), s -> s.getObjectiveScore()) };

		final Map<Instance, State> finalStates = new LinkedHashMap<>();

		int instanceIndex = 0;

		for (Instance instance : instances) {
			final List<State> producedStateChain = new ArrayList<>();

			State currentState = initializer.getInitState(instance);
			predictionOF.score(currentState);
			finalStates.put(instance, currentState);
			producedStateChain.add(currentState);
			int samplingStep;
			for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

				for (IExplorationStrategy explorer : explorerList) {

					final List<State> proposalStates = explorer.explore(currentState);

					if (proposalStates.isEmpty())
						proposalStates.add(currentState);

					predictionOF.score(proposalStates);

					final State candidateState = SamplerCollection.greedyObjectiveStrategy()
							.sampleCandidate(proposalStates);

					boolean isAccepted = SamplerCollection.greedyObjectiveStrategy().getAcceptanceStrategy(0)
							.isAccepted(candidateState, currentState);

					if (isAccepted) {
						currentState = candidateState;
					}

					producedStateChain.add(currentState);

					finalStates.put(instance, currentState);

				}
				if (meetsSamplingStoppingCriterion(noObjectiveChangeCrit, producedStateChain))
					break;

			}
			if (printDetailedLog)
				LogUtils.logState(log, COVERAGE_CONTEXT + " [1/1]" + "[" + ++instanceIndex + "/" + instances.size()
						+ "]" + "[" + (samplingStep + 1) + "]", instance, currentState);
		}

		Score meanTrainOFScore = new Score();
		for (Entry<Instance, State> finalState : finalStates.entrySet()) {
			predictionOF.score(finalState.getValue());
			if (printDetailedLog)
				log.info(
						finalState.getKey().getName().substring(0, Math.min(finalState.getKey().getName().length(), 10))
								+ "... \t" + SCORE_FORMAT.format(finalState.getValue().getObjectiveScore()));
			meanTrainOFScore.add(finalState.getValue().getScore());
		}

		return meanTrainOFScore;
	}

}

package de.hterhors.semanticmr.crf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.SlotFillingObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractBeamSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public class SemanticParsingCRFMultiState implements ISemanticParsingCRF {
	public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");

	private static Logger log = LogManager.getFormatterLogger("SlotFilling");

	/**
	 * The maximum number of sampling steps per instance. This prevents infinite
	 * loops if no stopping criterion ever matches.
	 */
	final static public int MAX_SAMPLING = 100;

	public final List<IExplorationStrategy> explorerList;

	final Model model;

	final IObjectiveFunction objectiveFunction;

	final AbstractBeamSampler sampler;

	private IStateInitializer initializer;

	public void setInitializer(IStateInitializer initializer) {
		this.initializer = initializer;
	}

	public IStateInitializer getInitializer() {
		return initializer;
	}

	private CRFStatistics trainingStatistics;

	private CRFStatistics testStatistics;

	private IObjectiveFunction coverageObjectiveFunction = new SlotFillingObjectiveFunction(
			new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE));

	public SemanticParsingCRFMultiState(Model model, IExplorationStrategy explorer, AbstractBeamSampler sampler,
			IObjectiveFunction objectiveFunction) {
		this(model, Arrays.asList(explorer), sampler, objectiveFunction);
	}

	public SemanticParsingCRFMultiState(Model model, List<IExplorationStrategy> explorer, AbstractBeamSampler sampler,
			IObjectiveFunction objectiveFunction) {
		this.model = model;
		this.explorerList = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
	}

	public SemanticParsingCRFMultiState(Model model, List<IExplorationStrategy> explorerList,
			AbstractBeamSampler sampler, IStateInitializer stateInitializer,
			IObjectiveFunction trainingObjectiveFunction) {
		this.model = model;
		this.explorerList = explorerList;
		this.objectiveFunction = trainingObjectiveFunction;
		this.sampler = sampler;
		this.initializer = stateInitializer;
	}

	public Map<Instance, State> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final ISamplingStoppingCriterion[] samplingStoppingCrits) {
		return train(learner, trainingInstances, numberOfEpochs, new ITrainingStoppingCriterion[] {},
				samplingStoppingCrits);

	}

//	public Map<Instance, State> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
//			final int numberOfEpochs, final ITrainingStoppingCriterion[] trainingStoppingCrits) {
//		return train(learner, trainingInstances, numberOfEpochs, trainingStoppingCrits,
//				new ISamplingStoppingCriterion[] {});
//	}

	public Map<Instance, State> train(final AdvancedLearner learner, List<Instance> trainingInstances,
			final int numberOfEpochs, final ITrainingStoppingCriterion[] trainingStoppingCrits,
			final ISamplingStoppingCriterion[] samplingStoppingCrits) {

		this.trainingStatistics = new CRFStatistics("Train");
		log.info("Start training procedure...");

		this.trainingStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, Map<Integer, State>> finalStates = new LinkedHashMap<>();

		trainingInstances = new ArrayList<>(trainingInstances);
		Map<Instance, State> selectedbestStates = new HashMap<>();

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {
			log.info("############");
			log.info("# Epoch: " + (epoch + 1) + " #");
			log.info("############");

			final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

			int instanceIndex = 0;

			for (Instance instance : trainingInstances) {

				final List<State> producedStateChain = new ArrayList<>();

				Map<Integer, StatePair> currentStatePairs = new HashMap<>();

				for (State state : initializer.getInitMultiStates(instance)) {
					currentStatePairs.put(state.getCurrentPredictions().getAnnotations().size(),
							new StatePair(state, null));
				}

				finalStates.put(instance, new HashMap<>());

				for (Entry<Integer, StatePair> statePairMap : currentStatePairs.entrySet()) {
					objectiveFunction.score(statePairMap.getValue().currentState);
					finalStates.get(instance).put(statePairMap.getKey(), statePairMap.getValue().currentState);

					/*
					 * add random / first
					 */
					if (producedStateChain.isEmpty())
						producedStateChain.add(statePairMap.getValue().currentState);
				}

				int samplingStep;
				for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {
					for (IExplorationStrategy explorer : explorerList) {

						State bestState = null;

						for (Entry<Integer, StatePair> statePair : currentStatePairs.entrySet()) {

							final List<StatePair> proposalStatePairs;

							final List<State> propStates = explorer.explore(statePair.getValue().currentState);

							if (propStates.isEmpty()) {
								proposalStatePairs = Arrays.asList(new StatePair(statePair.getValue().currentState,
										statePair.getValue().currentState));
							} else {
								proposalStatePairs = new ArrayList<>();

								if (sampleBasedOnObjectiveFunction) {
									objectiveFunction.score(propStates);
								} else {
									model.score(propStates);
								}

								for (State np : propStates) {
									proposalStatePairs.add(new StatePair(statePair.getValue().currentState, np));
								}
							}

							final StatePair candidateState = sampler.sampleCandidate(proposalStatePairs, 1).get(0);

							if (sampleBasedOnObjectiveFunction) {
								model.score(candidateState.candidateState);
							} else {
								objectiveFunction.score(candidateState.candidateState);
								objectiveFunction.score(candidateState.currentState);
							}
							
							boolean isAccepted = sampler.getAcceptanceStrategy(epoch)
									.isAccepted(candidateState.candidateState, candidateState.currentState);

							/*
							 * Update model weights
							 */
							model.updateWeights(learner,  candidateState.candidateState,candidateState.currentState);

							if (isAccepted) {
								/*
								 * On acceptance chose candidate state as next state
								 */
								currentStatePairs.put(statePair.getKey(),
										new StatePair(candidateState.candidateState, null));
							} else {
								/*
								 * Otherwise chose current state as next state.
								 */
								currentStatePairs.put(statePair.getKey(),
										new StatePair(candidateState.currentState, null));
							}

							finalStates.get(instance).put(statePair.getKey(),
									currentStatePairs.get(statePair.getKey()).currentState);

							/*
							 * Select best following state.
							 */
							if (bestState == null || bestState
									.getModelScore() < currentStatePairs.get(statePair.getKey()).currentState
											.getModelScore())
								bestState = currentStatePairs.get(statePair.getKey()).currentState;

						}

						/*
						 * Update learner based on pairwise multi states
						 */
						List<Integer> is = new ArrayList<>(currentStatePairs.keySet());

						for (int i = 0; i < is.size(); i++) {
							for (int j = i + 1; j < is.size(); j++) {
								model.updateWeights(learner, currentStatePairs.get(is.get(i)).currentState,
										currentStatePairs.get(is.get(j)).currentState);
							}
						}

						/*
						 * Add best state of the multi states
						 */
						producedStateChain.add(bestState);
					}

					if (meetsSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain))
						break;

				}
				this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
				LogUtils.logMultipleStates(log,
						TRAIN_CONTEXT + " [" + (epoch + 1) + "/" + numberOfEpochs + "]" + "[" + ++instanceIndex + "/"
								+ trainingInstances.size() + "]" + "[" + (samplingStep + 1) + "]",
						instance, currentStatePairs.values());
				log.info("Time: " + this.trainingStatistics.getTotalDuration());
			}

			for (Entry<Instance, Map<Integer, State>> i : finalStates.entrySet()) {

				State bestState = null;
				for (State s : i.getValue().values()) {

					if (bestState == null || bestState.getModelScore() < s.getModelScore())
						bestState = s;

				}

				selectedbestStates.put(i.getKey(), bestState);
			}

			if (meetsTrainingStoppingCriterion(trainingStoppingCrits, selectedbestStates))
				break;

		}

		this.trainingStatistics.endTrainingTime = System.currentTimeMillis();

		return selectedbestStates;
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

	public Map<Instance, State> predict(List<Instance> instancesToPredict,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(this.model, instancesToPredict, 1, stoppingCriterion).entrySet().stream()
				.collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue().get(0)));
	}

	public Map<Instance, State> predictHighRecall(List<Instance> instancesToPredict, final int n,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(this.model, instancesToPredict, n, stoppingCriterion).entrySet().stream()
				.collect(Collectors.toMap(m -> m.getKey(), m -> merge(m, n)));
	}

	public Map<Instance, State> predictHighRecall(Model model, List<Instance> instancesToPredict, final int n,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(model, instancesToPredict, n, stoppingCriterion).entrySet().stream()
				.collect(Collectors.toMap(m -> m.getKey(), m -> merge(m, n)));
	}

	public Map<Instance, State> predict(Model model, List<Instance> instancesToPredict,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(model, instancesToPredict, 1, stoppingCriterion).entrySet().stream()
				.collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue().get(0)));
	}

	public Map<Instance, List<State>> collectNBestStates(List<Instance> instancesToPredict, final int n,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(this.model, instancesToPredict, n, stoppingCriterion);
	}

	private Map<Instance, List<State>> predictP(Model model, List<Instance> instancesToPredict, final int n,
			ISamplingStoppingCriterion... stoppingCriterion) {

		if (n != 1)
			throw new IllegalStateException("This prediction does not support values for (n != 1). value of n = " + n);

		this.testStatistics = new CRFStatistics("Test");
		this.testStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, Map<Integer, List<State>>> finalStates = new LinkedHashMap<>();

		Map<Instance, List<State>> selectedbestStates = new HashMap<>();
		int instanceIndex = 0;
		for (Instance instance : instancesToPredict) {

			final List<State> producedStateChain = new ArrayList<>();

			Map<Integer, StatePair> currentStatePairs = new HashMap<>();

			for (State state : initializer.getInitMultiStates(instance)) {
				currentStatePairs.put(state.getCurrentPredictions().getAnnotations().size(),
						new StatePair(state, null));
			}

			finalStates.put(instance, new HashMap<>());

			for (Entry<Integer, StatePair> statePairMap : currentStatePairs.entrySet()) {
				objectiveFunction.score(statePairMap.getValue().currentState);
				finalStates.get(instance).put(statePairMap.getKey(),
						Arrays.asList(statePairMap.getValue().currentState));

				/*
				 * add random / first
				 */
				if (producedStateChain.isEmpty())
					producedStateChain.add(statePairMap.getValue().currentState);
			}
//			List<State> currentStates = new ArrayList<>();

			int samplingStep;
			for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

				for (IExplorationStrategy explorer : explorerList) {

					State bestStateOfAllCardinalities = null;

					for (Entry<Integer, StatePair> statePair : currentStatePairs.entrySet()) {

						final List<StatePair> proposalStatePairs;

						final List<State> propStates = explorer.explore(statePair.getValue().currentState);

						if (propStates.isEmpty()) {
							proposalStatePairs = Arrays.asList(new StatePair(statePair.getValue().currentState,
									statePair.getValue().currentState));
						} else {
							proposalStatePairs = new ArrayList<>();

							model.score(propStates);

							for (State np : propStates) {
								proposalStatePairs.add(new StatePair(statePair.getValue().currentState, np));
							}
						}

						Collections.sort(proposalStatePairs, (s1, s2) -> -Double
								.compare(s1.candidateState.getModelScore(), s2.candidateState.getModelScore()));

						final StatePair candidateState = proposalStatePairs.get(0);

						boolean isAccepted = AcceptStrategies.strictModelAccept()
								.isAccepted(candidateState.candidateState, candidateState.currentState);

						if (isAccepted) {
							/*
							 * Score for comparison. for new docs this does not work.
							 */
							objectiveFunction.score(candidateState.candidateState);
							/*
							 * On acceptance chose candidate state as next state
							 */
							currentStatePairs.put(statePair.getKey(),
									new StatePair(candidateState.candidateState, null));

//							currentStates = 
//									proposalStatePairs.subList(0, Math.min(proposalStatePairs.size(), n)).stream().map(sp->sp.currentState);
//							objectiveFunction.score(currentStates);

						} else {
							/*
							 * Otherwise chose current state as next state.
							 */
							currentStatePairs.put(statePair.getKey(), new StatePair(candidateState.currentState, null));
						}

						/*
						 * Best state for specific cardinality
						 */

//						if (n == 1)
						finalStates.get(instance).put(statePair.getKey(),
								Arrays.asList(currentStatePairs.get(statePair.getKey()).currentState));
//						else
//							finalStates.put(instance, currentStates);

						/*
						 * Select best following state.
						 */
						if (bestStateOfAllCardinalities == null || bestStateOfAllCardinalities
								.getModelScore() < currentStatePairs.get(statePair.getKey()).currentState
										.getModelScore())
							bestStateOfAllCardinalities = currentStatePairs.get(statePair.getKey()).currentState;

					}

					producedStateChain.add(bestStateOfAllCardinalities);

				}
				if (meetsSamplingStoppingCriterion(stoppingCriterion, producedStateChain)) {
					break;
				}
			}

			this.testStatistics.endTrainingTime = System.currentTimeMillis();

			LogUtils.logMultipleStates(log,
					TEST_CONTEXT + "[" + ++instanceIndex + "/" + instancesToPredict.size() + "] [" + samplingStep + "]",
					instance, currentStatePairs.values());
//			computeCoverage(true, coverageObjectiveFunction, Arrays.asList(instance));
			log.info("***********************************************************");
			log.info("\n");
			log.info("Time: " + this.testStatistics.getTotalDuration());

		}

		for (Entry<Instance, Map<Integer, List<State>>> i : finalStates.entrySet()) {

			State bestState = null;
			for (List<State> s : i.getValue().values()) {

				if (bestState == null || bestState.getModelScore() < s.get(0).getModelScore())
					bestState = s.get(0);

			}

			selectedbestStates.put(i.getKey(), Arrays.asList(bestState));
		}

		this.testStatistics.endTrainingTime = System.currentTimeMillis();
		return selectedbestStates;
	}

	/**
	 * Merges the predictions of multiple states into one single state.
	 * 
	 * @param m
	 * @return
	 */
	private State merge(Entry<Instance, List<State>> m, final int n) {
		List<AbstractAnnotation> mergedAnnotations = new ArrayList<>();

		outer: for (int i = 0; i < m.getValue().size(); i++) {

			for (AbstractAnnotation abstractAnnotation : m.getValue().get(i).getCurrentPredictions().getAnnotations()) {

				if (mergedAnnotations.size() == n)
					break outer;
				mergedAnnotations.add(abstractAnnotation);
			}

		}

		State s = new State(m.getKey(), new Annotations(mergedAnnotations));
		objectiveFunction.score(s);
		return s;
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

	public String getModelName() {
		return model.getName();
	}

}

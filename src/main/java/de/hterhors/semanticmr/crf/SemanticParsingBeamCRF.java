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
import de.hterhors.semanticmr.crf.sampling.AbstractBeamSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IBeamSamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IBeamTrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class SemanticParsingBeamCRF {
	public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");

	private static Logger log = LogManager.getFormatterLogger("SlotFilling");

	private static class CRFStatistics {
		private final String context;

		private long startTime;
		private long endTime;

		public CRFStatistics(String context) {
			this.context = context;
		}

		private long getTotalDuration() {
			return endTime - startTime;
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
	final static public int MAX_SAMPLING = 1000;

	private static final String TRAIN_CONTEXT = "===========BEAM-TRAIN============\n";
	private static final String TEST_CONTEXT = "===========BEAM-TEST============\n";

	final List<IExplorationStrategy> explorerList;

	final Model model;

	final IObjectiveFunction objectiveFunction;

	final AbstractBeamSampler sampler;

	private final IStateInitializer initializer;

	private CRFStatistics trainingStatistics;

	private CRFStatistics predictStatistics;

	final private int beamSize;

	public SemanticParsingBeamCRF(Model model, IExplorationStrategy explorer, AbstractBeamSampler sampler,
			IStateInitializer initializer, IObjectiveFunction objectiveFunction, int beamSize) {
		this(model, Arrays.asList(explorer), sampler, initializer, objectiveFunction, beamSize);
	}

	public SemanticParsingBeamCRF(Model model, List<IExplorationStrategy> explorer, AbstractBeamSampler sampler,
			IStateInitializer initializer, IObjectiveFunction objectiveFunction, int beamSize) {
		this.model = model;
		this.explorerList = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
		this.initializer = initializer;
		this.beamSize = beamSize;
	}

	public Map<Instance, List<State>> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs) {
		return train(learner, trainingInstances, numberOfEpochs, new IBeamTrainingStoppingCriterion[] {},
				new IBeamSamplingStoppingCriterion[] {});

	}

	public Map<Instance, List<State>> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final IBeamSamplingStoppingCriterion... samplingStoppingCrits) {
		return train(learner, trainingInstances, numberOfEpochs, new IBeamTrainingStoppingCriterion[] {},
				samplingStoppingCrits);

	}

	public Map<Instance, List<State>> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final IBeamTrainingStoppingCriterion... trainingStoppingCrits) {
		return train(learner, trainingInstances, numberOfEpochs, trainingStoppingCrits,
				new IBeamSamplingStoppingCriterion[] {});
	}

	public Map<Instance, List<State>> train(final AdvancedLearner learner, final List<Instance> trainingInstances,
			final int numberOfEpochs, final IBeamTrainingStoppingCriterion[] trainingStoppingCrits,
			final IBeamSamplingStoppingCriterion[] samplingStoppingCrits) {

		this.trainingStatistics = new CRFStatistics("Train");
		log.info("Start training procedure...");

		this.trainingStatistics.startTime = System.currentTimeMillis();

		final Map<Instance, List<State>> finalStates = new LinkedHashMap<>();

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {

			log.info("############");
			log.info("# Epoch: " + (epoch + 1) + " #");
			log.info("############");

			final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

			int instanceIndex = 0;

			for (Instance instance : trainingInstances) {

				final List<List<State>> producedStateChain = new ArrayList<>();

				List<StatePair> currentStatePairs = Arrays
						.asList(new StatePair(initializer.getInitState(instance), null));

				for (StatePair statePair : currentStatePairs) {
					objectiveFunction.score(statePair.currentState);

				}
				finalStates.put(instance,
						currentStatePairs.stream().map(p -> p.currentState).collect(Collectors.toList()));

				producedStateChain.add(finalStates.get(instance));

				int samplingStep;
				for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {
					for (IExplorationStrategy explorer : explorerList) {

						final List<StatePair> proposalStatePairs = new ArrayList<>();

						for (StatePair statePair : currentStatePairs) {

							final List<State> propStates = explorer.explore(statePair.currentState);

							if (propStates.isEmpty()) {
//								for (State np : currentStatePairs.stream().map(p -> p.currentState)
//										.collect(Collectors.toList())) {
//								}
								proposalStatePairs.add(new StatePair(statePair.currentState, statePair.currentState));
							} else {

								if (sampleBasedOnObjectiveFunction) {
									objectiveFunction.score(propStates);
								} else {
									model.score(propStates);
								}

								for (State np : propStates) {
									proposalStatePairs.add(new StatePair(statePair.currentState, np));
								}
							}
						}

						final List<StatePair> candidateStatePairs = sampler.sampleCandidate(proposalStatePairs,
								beamSize);

						currentStatePairs = new ArrayList<>();

						for (StatePair beamStatePair : candidateStatePairs) {

							scoreSelectedStates(sampleBasedOnObjectiveFunction, beamStatePair.currentState,
									beamStatePair.candidateState);

							boolean isAccepted = sampler.getAcceptanceStrategy(epoch)
									.isAccepted(beamStatePair.candidateState, beamStatePair.currentState);

							/*
							 * Update model weights
							 */
							model.updateWeights(learner, beamStatePair.currentState, beamStatePair.candidateState);

							if (isAccepted) {
								/*
								 * On acceptance chose candidate state as next state
								 */
								currentStatePairs.add(new StatePair(beamStatePair.candidateState, null));
							} else {
								/*
								 * Otherwise chose current state as next state.
								 */
								currentStatePairs.add(new StatePair(beamStatePair.currentState, null));
							}

						}

						finalStates.put(instance,
								currentStatePairs.stream().map(p -> p.currentState).collect(Collectors.toList()));

						producedStateChain.add(finalStates.get(instance));

					}

					if (meetsBeamSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain))
						break;

				}

				this.trainingStatistics.endTime = System.currentTimeMillis();

				LogUtils.logMultipleStates(log,
						TRAIN_CONTEXT + " [" + (epoch + 1) + "/" + numberOfEpochs + "]" + "[" + ++instanceIndex + "/"
								+ trainingInstances.size() + "]" + "[" + (samplingStep + 1) + "]",
						instance, currentStatePairs);

				log.info("Time: " + this.trainingStatistics.getTotalDuration());
			}

			if (meetsBeamTrainingStoppingCriterion(trainingStoppingCrits, finalStates))
				break;

		}
		this.trainingStatistics.endTime = System.currentTimeMillis();

		return finalStates;
	}

	private boolean meetsBeamSamplingStoppingCriterion(IBeamSamplingStoppingCriterion[] stoppingCriterion,
			final List<List<State>> producedStateChain) {
		for (IBeamSamplingStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain))
				return true;
		}
		return false;
	}

	private boolean meetsBeamTrainingStoppingCriterion(IBeamTrainingStoppingCriterion[] stoppingCriterion,
			final Map<Instance, List<State>> producedStateChain) {
		for (IBeamTrainingStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain.values()))
				return true;
		}
		return false;
	}

	/**
	 * scores the state with the model on objective to add model score and with
	 * objective if previously was scored with model to add objective.
	 * 
	 * @param sampleBasedOnObjectiveFunction
	 * @param currentState
	 * @param candidateState
	 */
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
		return this.predictStatistics;
	}

	public Map<Instance, State> predict(List<Instance> instancesToPredict,
			IBeamSamplingStoppingCriterion... stoppingCriterion) {
		return selectBest(predict(this.model, instancesToPredict, stoppingCriterion));
	}

	private Map<Instance, List<State>> predict(Model model, List<Instance> instancesToPredict,
			IBeamSamplingStoppingCriterion... stoppingCriterion) {
		this.predictStatistics = new CRFStatistics("Predict");
		this.predictStatistics.startTime = System.currentTimeMillis();

		final Map<Instance, List<State>> finalStates = new LinkedHashMap<>();

		int instanceIndex = 0;
		for (Instance instance : instancesToPredict) {

			final List<List<State>> producedStateChain = new ArrayList<>();

			List<StatePair> currentStatePairs = Arrays.asList(new StatePair(initializer.getInitState(instance), null));

			for (StatePair statePair : currentStatePairs) {
				objectiveFunction.score(statePair.currentState);
			}
			finalStates.put(instance, currentStatePairs.stream().map(p -> p.currentState).collect(Collectors.toList()));

			producedStateChain.add(finalStates.get(instance));

			int samplingStep;
			for (samplingStep = 0; samplingStep < MAX_SAMPLING; samplingStep++) {

				for (IExplorationStrategy explorer : explorerList) {

//					final Map<State, List<State>> proposalStates = new HashMap<>();
					final List<StatePair> proposalStatePairs = new ArrayList<>(); // pair(proposalStates);

					for (StatePair statePair : currentStatePairs) {
						final List<State> propStates = explorer.explore(statePair.currentState);

						if (propStates.isEmpty()) {
//							proposalStates.put(statePair.currentState,
//									currentStatePairs.stream().map(p -> p.currentState).collect(Collectors.toList()));
							proposalStatePairs.add(new StatePair(statePair.currentState, statePair.currentState));
						} else {
							model.score(propStates);

							for (State propState : propStates) {
								proposalStatePairs.add(new StatePair(statePair.currentState, propState));
							}
						}

					}

					Collections.sort(proposalStatePairs, (s1, s2) -> -Double.compare(s1.candidateState.getModelScore(),
							s2.candidateState.getModelScore()));

					currentStatePairs = new ArrayList<>();

					for (StatePair beamStatePair : proposalStatePairs.subList(0,
							Math.min(proposalStatePairs.size(), beamSize))) {

						boolean isAccepted = AcceptStrategies.strictModelAccept()
								.isAccepted(beamStatePair.candidateState, beamStatePair.currentState);

						if (isAccepted) {
							objectiveFunction.score(beamStatePair.candidateState);
							currentStatePairs.add(new StatePair(beamStatePair.candidateState, null));
						} else {
							currentStatePairs.add(new StatePair(beamStatePair.currentState, null));
						}

					}

					finalStates.put(instance,
							currentStatePairs.stream().map(p -> p.currentState).collect(Collectors.toList()));

					producedStateChain.add(finalStates.get(instance));

				}
				if (meetsBeamSamplingStoppingCriterion(stoppingCriterion, producedStateChain)) {
					break;
				}
			}

			this.predictStatistics.endTime = System.currentTimeMillis();

			LogUtils.logMultipleStates(log,
					TEST_CONTEXT + "[" + ++instanceIndex + "/" + instancesToPredict.size() + "] [" + samplingStep + "]",
					instance, currentStatePairs);
			log.info("Time: " + this.predictStatistics.getTotalDuration());

		}
		this.predictStatistics.endTime = System.currentTimeMillis();
		return finalStates;
	}

	/**
	 * Merges the predictions of multiple states into one single state.
	 * 
	 * @param m
	 * @return
	 */
	private Map<Instance, State> selectBest(Map<Instance, List<State>> m) {

		Map<Instance, State> bestStates = new HashMap<>();

		for (Entry<Instance, List<State>> e : m.entrySet()) {

			List<State> sorted = e.getValue();
			Collections.sort(sorted, (s1, s2) -> -Double.compare(s1.getModelScore(), s2.getModelScore()));

			bestStates.put(e.getKey(), sorted.get(0));
		}

		return bestStates;
	}

}

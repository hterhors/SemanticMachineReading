package de.hterhors.semanticmr.crf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.SlotFillingObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

public class SemanticParsingCRF implements ISemanticParsingCRF {
	public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");

	private static Logger log = LogManager.getFormatterLogger("SlotFilling");

	/**
	 * The maximum number of sampling steps per instance. This prevents infinite
	 * loops if no stopping criterion ever matches.
	 */
	final static public int MAX_SAMPLING = 200;

	private static final String COVERAGE_CONTEXT = "===========COVERAGE============\n";
	private static final String TRAIN_CONTEXT = "===========TRAIN============\n";
	private static final String INTERMEDIATE_CONTEXT = "===========INTERMEDIATE============\n";
	private static final String TEST_CONTEXT = "===========TEST============\n";

	public final List<IExplorationStrategy> explorerList;

	final Model model;

	final IObjectiveFunction objectiveFunction;

	final AbstractSampler sampler;

	private IStateInitializer initializer;

	public void setInitializer(IStateInitializer initializer) {
		this.initializer = initializer;
	}

	public IStateInitializer getInitializer() {
		return initializer;
	}

	private CRFStatistics trainingStatistics;

	private CRFStatistics testStatistics;

	private IObjectiveFunction coverageObjectiveFunction = new SlotFillingObjectiveFunction(EScoreType.MICRO,
			new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE, EEvaluationDetail.DOCUMENT_LINKED));

	public SemanticParsingCRF(Model model, IExplorationStrategy explorer, AbstractSampler sampler,
			IObjectiveFunction objectiveFunction) {
		this(model, Arrays.asList(explorer), sampler, objectiveFunction);
	}

	public SemanticParsingCRF(Model model, List<IExplorationStrategy> explorer, AbstractSampler sampler,
			IObjectiveFunction objectiveFunction) {
		this.model = model;
		this.explorerList = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
	}

	public SemanticParsingCRF(Model model, List<IExplorationStrategy> explorerList, AbstractSampler sampler,
			IStateInitializer stateInitializer, IObjectiveFunction trainingObjectiveFunction) {
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

		final Map<Instance, State> finalStates = new LinkedHashMap<>();
//		Map<Integer, Double> lastModelWeights = new HashMap<>();

		trainingInstances = new ArrayList<>(trainingInstances);

		/**
		 * TODO: PARAMETERIZE
		 */
		boolean includeLeaveOnePropertyOut = false;
//		Score: SPECIES_GENDER_WEIGHT_AGE_CATEGORY_AGE	0.94	0.95	0.94

//		Score: SPECIES_GENDER_WEIGHT_AGE_CATEGORY_AGE	0.93	0.94	0.91

//		standard: Score [getF1()=0.425, getPrecision()=0.598, getRecall()=0.330, tp=61, fp=41, fn=124, tn=0]
//				only root: Score [getF1()=0.548, getPrecision()=0.575, getRecall()=0.523, tp=23, fp=17, fn=21, tn=0]
//				CRFStatistics [context=Train, getTotalDuration()=85465]
//				CRFStatistics [context=Test, getTotalDuration()=299]
//				Compute coverage...
//				Coverage Training: Score [getF1()=0.931, getPrecision()=1.000, getRecall()=0.871, tp=651, fp=0, fn=96, tn=0]
//				Compute coverage...
//				Coverage Development: Score [getF1()=0.876, getPrecision()=0.967, getRecall()=0.800, tp=148, fp=5, fn=37, tn=0]
//				modelName: Injury9847
//		standard: Score [getF1()=0.432, getPrecision()=0.608, getRecall()=0.335, tp=62, fp=40, fn=123, tn=0]
//				only root: Score [getF1()=0.571, getPrecision()=0.600, getRecall()=0.545, tp=24, fp=16, fn=20, tn=0]
//				CRFStatistics [context=Train, getTotalDuration()=65265]
//				CRFStatistics [context=Test, getTotalDuration()=189]
//				Compute coverage...
//				Coverage Training: Score [getF1()=0.931, getPrecision()=1.000, getRecall()=0.871, tp=651, fp=0, fn=96, tn=0]
//				Compute coverage...
//				Coverage Development: Score [getF1()=0.876, getPrecision()=0.967, getRecall()=0.800, tp=148, fp=5, fn=37, tn=0]
//				modelName: Injury1364

//		Collections.sort(trainingInstances);
		Random random = new Random(1000L);
		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {
			Collections.shuffle(trainingInstances, new Random(random.nextLong()));

			Map<SlotType, Boolean> leaveOnePropertyOut = null;
			if (includeLeaveOnePropertyOut) {
				leaveOnePropertyOut = SlotType.storeExcludance();
				List<SlotType> types = leaveOnePropertyOut.entrySet().stream().filter(a -> !a.getValue())
						.map(a -> a.getKey()).collect(Collectors.toList());
				types.get(random.nextInt(types.size())).exclude();
			}
			log.info("############");
			log.info("# Epoch: " + (epoch + 1) + " #");
			log.info("############");

			final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

			int instanceIndex = 0;

			for (Instance instance : trainingInstances) {
				++instanceIndex;

				State currentState = initializer.getInitState(instance);
				objectiveFunction.score(currentState);
				finalStates.put(instance, currentState);
				int samplingStep = 0;
				int intermediateCounter = 0;
				int proposalStateCounter = 0;

				final List<State> producedStateChain = new ArrayList<>();
				producedStateChain.add(currentState);

				sampling: for (samplingStep = 0; samplingStep < 10; samplingStep++) {

					for (IExplorationStrategy explorer : explorerList) {

						List<State> proposalStates = explorer.explore(currentState);
						if (proposalStates.isEmpty())
							proposalStates.add(currentState);
						proposalStateCounter += proposalStates.size();
						if (sampleBasedOnObjectiveFunction) {
							objectiveFunction.score(proposalStates);
						} else {
							model.score(proposalStates);
						}

						final State candidateState = sampler.sampleCandidate(proposalStates);

						scoreSelectedStates(sampleBasedOnObjectiveFunction, currentState, candidateState);

						boolean isAccepted = sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState,
								currentState);

						model.updateWeights(learner, currentState, candidateState);

						intermediateCounter++;
						if (isAccepted) {
							currentState = candidateState;

//								LogUtils.logState(log,
//										INTERMEDIATE_CONTEXT + " [" + (epoch + 1) + "/" + numberOfEpochs + "]" + "["
//												+ instanceIndex + "/" + trainingInstances.size() + "]" + "["
//												+ (samplingStep + 1) + "]" + "[" + (intermediateCounter + 1) + "]" + "["
//												+ proposalStateCounter + "]",
//										instance, currentState);
						}
						producedStateChain.add(currentState);

						finalStates.put(instance, currentState);

						if (currentState.getObjectiveScore() == 1.0D)
							break sampling;

					}

					if (meetsSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain))
						break sampling;
				}
				this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
				LogUtils.logState(log,
						TRAIN_CONTEXT + " [" + (epoch + 1) + "/" + numberOfEpochs + "]" + "[" + instanceIndex + "/"
								+ trainingInstances.size() + "]" + "[" + (samplingStep + 1) + "]",
						instance, currentState);
				log.info("Time: " + this.trainingStatistics.getTotalDuration());
			}
			if (includeLeaveOnePropertyOut)
				SlotType.restoreExcludance(leaveOnePropertyOut);

//			Map<Integer, Double> currentModelWeights = model.getFactorTemplates().stream()
//					.flatMap(t -> t.getWeights().getFeatures().entrySet().stream())
//					.collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
//
//			double modelWeightsDiff = computeThreshold(currentModelWeights, lastModelWeights);
//			Score a = new Score();
//			NerlaEvaluator eval = new NerlaEvaluator(EEvaluationDetail.ENTITY_TYPE);
//			for (Entry<Instance, State> e : finalStates.entrySet()) {
//
//				List<EntityTemplate> goldAnnotations = e.getValue().getGoldAnnotations().getAnnotations();
//				List<EntityTemplate> predictedAnnotations = e.getValue().getCurrentPredictions().getAnnotations();
//
//				List<Integer> bestAssignment = ((CartesianEvaluator) predictionObjectiveFunction.getEvaluator())
//						.getBestAssignment(goldAnnotations, predictedAnnotations);
//				Score score = simpleEvaluate(false, eval, bestAssignment, goldAnnotations, predictedAnnotations);
//				a.add(score);
//
//			}
//
//			Score s = new Score();
//			for (Entry<Instance, State> e : predict(testInstances, new MaxChainLengthCrit(10)).entrySet()) {
//
//				List<EntityTemplate> goldAnnotations = e.getValue().getGoldAnnotations().getAnnotations();
//				List<EntityTemplate> predictedAnnotations = e.getValue().getCurrentPredictions().getAnnotations();
//
//				List<Integer> bestAssignment = ((CartesianEvaluator) predictionObjectiveFunction.getEvaluator())
//						.getBestAssignment(goldAnnotations, predictedAnnotations);
//				Score score = simpleEvaluate(false, eval, bestAssignment, goldAnnotations, predictedAnnotations);
//				s.add(score);
//
//			}
//			log.info("DIFF\t" + modelWeightsDiff + "\t" + s.getF1() + "\t" + s.getPrecision() + "\t" + s.getRecall()
//					+ "\t" + a.getF1() + "\t" + a.getPrecision() + "\t" + a.getRecall());
//			if (modelWeightsDiff < 0.01)
//				break;

			if (meetsTrainingStoppingCriterion(trainingStoppingCrits, finalStates))
				break;

//			lastModelWeights = currentModelWeights;
//			Collections.shuffle(trainingInstances);

		}
		this.trainingStatistics.endTrainingTime = System.currentTimeMillis();

		return finalStates;

	}

	private Score simpleEvaluate(boolean print, NerlaEvaluator evaluator, List<Integer> bestAssignment,
			List<EntityTemplate> goldAnnotations, List<EntityTemplate> predictedAnnotationsBaseline) {
		Score simpleScore = new Score();

		for (int goldIndex = 0; goldIndex < bestAssignment.size(); goldIndex++) {
			final int predictIndex = bestAssignment.get(goldIndex);
			/*
			 * Treatments
			 */
			List<AbstractAnnotation> goldTreatments = new ArrayList<>(goldAnnotations.get(goldIndex)
					.getMultiFillerSlot(SlotType.get("hasTreatmentType")).getSlotFiller());

			List<AbstractAnnotation> predictTreatments = new ArrayList<>(predictedAnnotationsBaseline.get(predictIndex)
					.getMultiFillerSlot(SlotType.get("hasTreatmentType")).getSlotFiller());
			Score s;
			if (goldTreatments.isEmpty() && predictTreatments.isEmpty())
				s = new Score(1, 0, 0);
			else
				s = evaluator.prf1(goldTreatments, predictTreatments);

			simpleScore.add(s);
			/*
			 * OrganismModel
			 */
			List<AbstractAnnotation> goldOrganismModel = Arrays.asList(
					goldAnnotations.get(goldIndex).getSingleFillerSlotOfName("hasOrganismModel").getSlotFiller())
					.stream().filter(a -> a != null).collect(Collectors.toList());
			List<AbstractAnnotation> predictOrganismModel = Arrays.asList(predictedAnnotationsBaseline.get(predictIndex)
					.getSingleFillerSlotOfName("hasOrganismModel").getSlotFiller()).stream().filter(a -> a != null)
					.collect(Collectors.toList());

			simpleScore.add(evaluator.prf1(goldOrganismModel, predictOrganismModel));

			/*
			 * InjuryModel
			 */
			List<AbstractAnnotation> goldInjuryModel = Arrays
					.asList(goldAnnotations.get(goldIndex).getSingleFillerSlotOfName("hasInjuryModel").getSlotFiller())
					.stream().filter(a -> a != null).collect(Collectors.toList());
			List<AbstractAnnotation> predictInjuryModel = Arrays.asList(predictedAnnotationsBaseline.get(predictIndex)
					.getSingleFillerSlotOfName("hasInjuryModel").getSlotFiller()).stream().filter(a -> a != null)
					.collect(Collectors.toList());

			simpleScore.add(evaluator.prf1(goldInjuryModel, predictInjuryModel));

		}

		return simpleScore;
	}

	private double computeThreshold(Map<Integer, Double> currentModelWeights, Map<Integer, Double> lastModelWeights) {
		double diff = 0;
		for (Integer currentKey : currentModelWeights.keySet()) {
			if (lastModelWeights.containsKey(currentKey)) {
				diff += Math.abs(lastModelWeights.get(currentKey) - currentModelWeights.get(currentKey));
			} else {
				diff += currentModelWeights.get(currentKey);
			}
		}
		for (Integer currentKey : lastModelWeights.keySet()) {
			if (!currentModelWeights.containsKey(currentKey)) {
				diff += lastModelWeights.get(currentKey);
			}
		}

		return diff;
	}

	private boolean meetsSamplingStoppingCriterion(ISamplingStoppingCriterion[] stoppingCriterion,
			final List<State> producedStateChain) {
		for (ISamplingStoppingCriterion sc : stoppingCriterion) {
			if (sc.meetsCondition(producedStateChain)) {
				return true;
			}
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
				.collect(Collectors.toMap(m -> m.getKey(), m -> merge(m)));
	}

	public Map<Instance, State> predictHighRecall(Model model, List<Instance> instancesToPredict, final int n,
			ISamplingStoppingCriterion... stoppingCriterion) {
		return predictP(model, instancesToPredict, n, stoppingCriterion).entrySet().stream()
				.collect(Collectors.toMap(m -> m.getKey(), m -> merge(m)));
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
		this.testStatistics = new CRFStatistics("Test");
		this.testStatistics.startTrainingTime = System.currentTimeMillis();

		final Map<Instance, List<State>> finalStates = new LinkedHashMap<>();

		int instanceIndex = 0;
		for (Instance instance : instancesToPredict) {

			List<State> currentStates = new ArrayList<>();

			State currentState = initializer.getInitState(instance);

			finalStates.put(instance, Arrays.asList(currentState));
			objectiveFunction.score(currentState);

			int samplingStep = 0;

			final List<State> producedStateChain = new ArrayList<>();
			producedStateChain.add(currentState);

			for (samplingStep = 0; samplingStep < 10; samplingStep++) {

				for (IExplorationStrategy explorer : explorerList) {

					final List<State> proposalStates = explorer.explore(currentState);

					if (proposalStates.isEmpty())
						proposalStates.add(currentState);

					model.score(proposalStates);

					Collections.sort(proposalStates,
							(s1, s2) -> -Double.compare(s1.getModelScore(), s2.getModelScore()));

					final State candidateState = proposalStates.get(0);

					boolean accepted = AcceptStrategies.strictModelAccept().isAccepted(candidateState, currentState);

					if (accepted) {
						currentState = candidateState;
						objectiveFunction.score(currentState);
					}

					producedStateChain.add(currentState);

					if (n == 1) {
						finalStates.put(instance, Arrays.asList(currentState));
					} else {

						currentStates = new ArrayList<>();

						final State prevCurrentState = producedStateChain.get(producedStateChain.size() - 2);

						for (int i = 0; i < Math.min(proposalStates.size(), n); i++) {

							accepted = AcceptStrategies.strictModelAccept().isAccepted(proposalStates.get(i),
									prevCurrentState); // prev current state

							if (accepted) {
								objectiveFunction.score(proposalStates.get(i));
								currentStates.add(proposalStates.get(i));
							} else {
								/*
								 * Quick break cause monotone decreasing model score distribution and strict
								 * evaluation.
								 */
								break;
							}
						}
						if (!finalStates.containsKey(instance) || !currentStates.isEmpty())
							finalStates.put(instance, currentStates);
					}
				}
				if (meetsSamplingStoppingCriterion(stoppingCriterion, producedStateChain)) {
					break;
				}
			}
			this.testStatistics.endTrainingTime = System.currentTimeMillis();

			LogUtils.logState(log,
					TEST_CONTEXT + "[" + ++instanceIndex + "/" + instancesToPredict.size() + "] [" + samplingStep + "]",
					instance, currentState);
			log.info("***********************************************************");
			log.info("\n");
			log.info("Time: " + this.testStatistics.getTotalDuration());

		}
		this.testStatistics.endTrainingTime = System.currentTimeMillis();
		return finalStates;

	}

	/**
	 * Merges the predictions of multiple states into one single state.
	 * 
	 * @param m
	 * @return
	 */
	private State merge(Entry<Instance, List<State>> m) {
		List<AbstractAnnotation> mergedAnnotations = new ArrayList<>();

		for (int i = 0; i < m.getValue().size(); i++) {

			for (AbstractAnnotation abstractAnnotation : m.getValue().get(i).getCurrentPredictions().getAnnotations()) {
				mergedAnnotations.add(abstractAnnotation);
			}

		}

		State s = new State(m.getKey(), new Annotations(mergedAnnotations));
		objectiveFunction.score(s);
		return s;
	}

//	private void compare(State currentState, State candidateState) {
//
//		Map<String, Double> differences = getDifferences(collectFeatures(currentState),
//				collectFeatures(candidateState));
//		if (differences.isEmpty())
//			return;
//
//		List<Entry<String, Double>> sortedWeightsPrevState = new ArrayList<>(collectFeatures(currentState).entrySet());
//		List<Entry<String, Double>> sortedWeightsCandState = new ArrayList<>(
//				collectFeatures(candidateState).entrySet());
//
//		Collections.sort(sortedWeightsPrevState, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
//		Collections.sort(sortedWeightsCandState, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
//
//		log.info(currentState.getInstance().getName());
//		log.info("_____________GoldAnnotations:_____________");
//		log.info(currentState.getGoldAnnotations());
//		log.info("_____________PrevState:_____________");
//		sortedWeightsPrevState.stream().filter(k -> differences.containsKey(k.getKey())).forEach(log::info);
//		log.info("ModelScore: " + currentState.getModelScore() + ": " + currentState.getCurrentPredictions());
//		log.info("_____________CandState:_____________");
//		sortedWeightsCandState.stream().filter(k -> differences.containsKey(k.getKey())).forEach(log::info);
//		log.info("ModelScore: " + candidateState.getModelScore() + ": " + candidateState.getCurrentPredictions());
//		log.info("------------------");
//	}

//	private static Map<String, Double> getDifferences(Map<String, Double> currentFeatures,
//			Map<String, Double> candidateFeatures) {
//		Map<String, Double> differences = new HashMap<>();
//
//		Set<String> keys = new HashSet<>();
//
//		keys.addAll(candidateFeatures.keySet());
//		keys.addAll(currentFeatures.keySet());
//
//		for (String key : keys) {
//
//			if (candidateFeatures.containsKey(key) && currentFeatures.containsKey(key)) {
//				double diff = 0;
//				if ((diff = Math.abs(currentFeatures.get(key) - candidateFeatures.get(key))) != 0.0D) {
//					// This should or can not happen as feature weights are shared throughout states
//					differences.put(key, diff);
//				}
//			} else if (currentFeatures.containsKey(key)) {
//				differences.put(key, currentFeatures.get(key));
//			} else {
//				differences.put(key, candidateFeatures.get(key));
//			}
//
//		}
//		return differences;
//	}

//	private static Map<String, Double> collectFeatures(State currentState) {
//		Map<String, Double> features = new HashMap<>();
//
//		for (FactorGraph fg : currentState.getFactorGraphs()) {
//			for (Factor f : fg.getFactors()) {
//
//				for (Entry<Integer, Double> feature : f.getFeatureVector().getFeatures().entrySet()) {
//					if (f.getFactorScope().getTemplate().getWeights().getFeatures().containsKey(feature.getKey()))
//						features.put(
//								f.getFactorScope().getTemplate().getClass().getSimpleName() + ":"
//										+ Model.getFeatureForIndex(feature.getKey()),
//								feature.getValue() * f.getFactorScope().getTemplate().getWeights().getFeatures()
//										.get(feature.getKey()));
//
//				}
//			}
//		}
//		return features;
//	}

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
	public Map<Instance, State> computeCoverage(final boolean printDetailedLog, IObjectiveFunction predictionOF,
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

			int samplingStep = 0;

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
		return finalStates;

	}

	public String getModelName() {
		return model.getName();
	}

	public void scoreWithModel(List<State> nextStates) {
		this.model.score(nextStates);
	}

}

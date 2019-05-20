package de.hterhors.semanticmr.projects.olp2.extraction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateRetrievalCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.SemanticParsingCRF;
import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.SlotFillingObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.ContextBetweenSlotFillerTemplate;
import de.hterhors.semanticmr.crf.templates.shared.IntraTokenTemplate;
import de.hterhors.semanticmr.crf.templates.shared.TokenContextTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.BeamSearchEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.examples.slotfilling.SlotFillingExample;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonNerlaProvider;
import de.hterhors.semanticmr.nerla.NerlaCollector;
import de.hterhors.semanticmr.projects.AbstractSemReadProject;
import de.hterhors.semanticmr.projects.olp2.corpus.preprocessing.StartPreprocessing;
import de.hterhors.semanticmr.projects.psink.normalization.WeightNormalization;

public class Olp2ExtractionMain extends AbstractSemReadProject {

	public Olp2ExtractionMain() {
		super(SystemScope.Builder.getSpecsHandler().addScopeSpecification(StartPreprocessing.de_specificationProvider)
				.apply().registerNormalizationFunction(new WeightNormalization()).build());

		CartesianEvaluator cartesian = new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE);
		BeamSearchEvaluator beam = new BeamSearchEvaluator(EEvaluationDetail.ENTITY_TYPE, 2);

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/de/corpus/sf/"), shuffleCorpusDistributor);

		NerlaCollector nerlaProvider = new NerlaCollector(instanceProvider.getInstances());
		nerlaProvider.addNerlaProvider(
				new JsonNerlaProvider(new File("src/main/resources/examples/olp2/de/nerla/nerla.json")));

		AnnotationCandidateRetrievalCollection candidateProvider = nerlaProvider.collect();

//		AnnotationCandidateProviderCollection candidateProvider = new AnnotationCandidateProviderCollection(
//				instanceProvider.getInstances());
//		candidateProvider.setEntityTypeCandidateProvider();

		IObjectiveFunction objectiveFunction = new SlotFillingObjectiveFunction(cartesian);

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));

		List<AbstractFeatureTemplate<?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new IntraTokenTemplate());
		featureTemplates.add(new TokenContextTemplate());
		featureTemplates.add(new ContextBetweenSlotFillerTemplate());

		IStateInitializer stateInitializer = (instance) -> new State(instance,
				new Annotations(new EntityTemplate(AnnotationBuilder.toAnnotation("Goal"))));

		int numberOfEpochs = 1;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
//		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		ISamplingStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);
		ISamplingStoppingCriterion noModelChangeCrit = new ConverganceCrit(3, s -> s.getModelScore());
		ISamplingStoppingCriterion[] sampleStoppingCrits = new ISamplingStoppingCriterion[] { maxStepCrit,
				noModelChangeCrit };
		SlotFillingExplorer explorer = new SlotFillingExplorer(objectiveFunction,candidateProvider);

		final File modelDir = new File("models/olp2/test1/");
		final String modelName = "Model3";

		Model model;
		try {
			model = Model.load(modelDir, modelName);
		} catch (Exception e) {
		}
		model = new Model(featureTemplates);

		SemanticParsingCRF crf = new SemanticParsingCRF(model, explorer, sampler, stateInitializer, objectiveFunction);

		if (!model.isTrained()) {
			crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs,
					sampleStoppingCrits);
			model.save(modelDir, modelName, true);
		}

		Map<Instance, State> testResults = crf.test(instanceProvider.getRedistributedTestInstances(),
				sampleStoppingCrits);

		evaluate(log, testResults);

		log.info(crf.getTrainingStatistics());
		log.info(crf.getTestStatistics());
	}

	private static Logger log = LogManager.getFormatterLogger(SlotFillingExample.class);

	public static void main(String[] args) {
		new Olp2ExtractionMain();

	}

}

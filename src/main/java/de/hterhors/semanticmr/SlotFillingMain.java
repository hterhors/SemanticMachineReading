package de.hterhors.semanticmr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.CRF;
import de.hterhors.semanticmr.crf.exploration.EntityTemplateExplorer;
import de.hterhors.semanticmr.crf.exploration.constraints.EHardConstraintType;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.SlotFillingObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.NoChangeCrit;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.slotfilling.InBetweenContextTemplate;
import de.hterhors.semanticmr.crf.templates.slotfilling.IntraTokenTemplate;
import de.hterhors.semanticmr.crf.templates.slotfilling.TokenContextTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.EvaluationResultPrinter;
import de.hterhors.semanticmr.examples.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonNerlaProvider;
import de.hterhors.semanticmr.nerla.NerlaCollector;

public class SlotFillingMain {

	public static void main(String[] args) throws Exception {
		new SlotFillingMain();
	}

	private static final File instancesFileDir = new File("src/main/resources/corpus/data/instances/");

	private static final File entities = new File("src/main/resources/specifications/csv/OrganismModel/entities.csv");
	private static final File slots = new File("src/main/resources/specifications/csv/OrganismModel/slots.csv");
	private static final File structures = new File(
			"src/main/resources/specifications/csv/OrganismModel/structures.csv");
	private static final File hierarchies = new File(
			"src/main/resources/specifications/csv/OrganismModel/hierarchies.csv");

	private final File slotPairConstraitsSpecifications = new File(
			"src/main/resources/specifications/slotPairExcludingConstraints.csv");

	public final static CSVScopeReader systemsScopeReader = new CSVScopeReader(entities, hierarchies, slots,
			structures);

	public SlotFillingMain() throws Exception {

		SystemScope systemScope = SystemScope.Builder.getSpecsHandler().addScopeSpecification(systemsScopeReader)
				.apply().registerNormalizationFunction(new WeightNormalization()).build();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider.removeEmptyInstances = true;
		InstanceProvider.removeInstancesWithToManyAnnotations = true;

		InstanceProvider instanceProvider = new InstanceProvider(instancesFileDir, shuffleCorpusDistributor);

		NerlaCollector nerlaProvider = new NerlaCollector(instanceProvider.getInstances());
		nerlaProvider
				.addNerlaProvider(new JsonNerlaProvider(new File("src/main/resources/corpus/data/nerla/nerla.json")));

		AnnotationCandidateProviderCollection candidateProvider = nerlaProvider.collect();

//				candidateProvider.setEntityTypeCandidateProvider();

		HardConstraintsProvider constraintsProvider = new HardConstraintsProvider();
		constraintsProvider.addHardConstraints(EHardConstraintType.SLOT_PAIR_EXCLUSION,
				slotPairConstraitsSpecifications);

		AbstractEvaluator evaluator = new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE);

		IObjectiveFunction objectiveFunction = new SlotFillingObjectiveFunction(evaluator);

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.1, 0), new L2(0.01));

		List<AbstractFeatureTemplate<?, ?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new IntraTokenTemplate());
		featureTemplates.add(new TokenContextTemplate());
		featureTemplates.add(new InBetweenContextTemplate());

		IStateInitializer stateInitializer = ((instance) -> new State(instance,
				new Annotations(new EntityTemplate(AnnotationBuilder
						.toAnnotation(instance.getGoldAnnotations().getAnnotations().get(0).getEntityType())))));

		int numberOfEpochs = 10;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		IStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);
		IStoppingCriterion noModelChangeCrit = new NoChangeCrit(3, s -> s.getModelScore());

		EntityTemplateExplorer explorer = new EntityTemplateExplorer(candidateProvider, constraintsProvider);

		final File modelDir = new File("models/slotfill/test1/");
		final String modelName = "Model3";

		Model model;
//		try {
//			model = Model.load(modelDir, modelName);
//		} catch (Exception e) {
//		}
		model = new Model(featureTemplates);

		CRF crf = new CRF(model, explorer, sampler, stateInitializer, objectiveFunction);

		if (!model.wasLoaded()) {
			crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, maxStepCrit,
					noModelChangeCrit);
			model.save(modelDir, modelName, true);
		}

		Map<Instance, State> testResults = crf.test(instanceProvider.getRedistributedTestInstances(), maxStepCrit,
				noModelChangeCrit);

		EvaluationResultPrinter.evaluate(crf, testResults);

		crf.printTrainingStatistics(System.out);
		crf.printTestStatistics(System.out);
	}

}

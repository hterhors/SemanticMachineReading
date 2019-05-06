package de.hterhors.semanticmr.examples.slotfilling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.SemanticParsingCRF;
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
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
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
import de.hterhors.semanticmr.examples.slotfilling.specs.SFSpecs;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonNerlaProvider;
import de.hterhors.semanticmr.nerla.NerlaCollector;
import de.hterhors.semanticmr.projects.AbstractSemReadProject;
import de.hterhors.semanticmr.projects.psink.normalization.WeightNormalization;

/**
 * Example of how to perform slot filling.
 * 
 * @author hterhors
 *
 */
public class SlotFillingMain extends AbstractSemReadProject {

	/**
	 * Start the slot filling procedure.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		new SlotFillingMain();
	}

	/**
	 * File of additional hard constraints during exploration. There are different
	 * types of hard constraints (not all of them are implemented yet). This file
	 * contains slot pair exclusion pairs. E.g. if Slot A is filled by Value X than
	 * Slot B must not be filled with value Y.
	 */
	private final File slotPairConstraitsSpecifications = new File(
			"src/main/resources/specifications/slotPairExclusionConstraints.csv");

	/**
	 * The directory of the corpus instances. In this example each instance is
	 * stored in its own json-file.
	 */
	private final File instanceDirectory = new File("src/main/resources/corpus/data/instances/");

	public SlotFillingMain() {

		super(SystemScope.Builder.getSpecsHandler().addScopeSpecification(SFSpecs.systemsScopeReader).apply()
				.registerNormalizationFunction(new WeightNormalization()).build());

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider.removeEmptyInstances = true;
		InstanceProvider.removeInstancesWithToManyAnnotations = true;

		InstanceProvider instanceProvider = new InstanceProvider(instanceDirectory, shuffleCorpusDistributor);

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
		IStoppingCriterion noModelChangeCrit = new ConverganceCrit(3, s -> s.getModelScore());

		EntityTemplateExplorer explorer = new EntityTemplateExplorer(candidateProvider, constraintsProvider);

		final File modelDir = new File("models/slotfill/test1/");
		final String modelName = "Model3";

		Model model;
//		try {
//			model = Model.load(modelDir, modelName);
//		} catch (Exception e) {
//		}
		model = new Model(featureTemplates);

		SemanticParsingCRF crf = new SemanticParsingCRF(model, explorer, sampler, stateInitializer, objectiveFunction);

		if (!model.wasLoaded()) {
			crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, maxStepCrit,
					noModelChangeCrit);
			model.save(modelDir, modelName, true);
		}

		Map<Instance, State> testResults = crf.test(instanceProvider.getRedistributedTestInstances(), maxStepCrit,
				noModelChangeCrit);

		EvaluationResultPrinter.evaluate(testResults);

		crf.printTrainingStatistics(System.out);
		crf.printTestStatistics(System.out);
	}

}

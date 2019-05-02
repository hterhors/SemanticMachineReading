package de.hterhors.semanticmr.examples.nerla;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.nerla.InMEMDictionaryBasedCandidateProvider;
import de.hterhors.semanticmr.candprov.nerla.NerlaCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.CRF;
import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.NerlaObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.NoChangeCrit;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.IntraTokenNerlaTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.MorphologicalNerlaTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.NerlaTokenContextTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.EvaluationResultPrinter;
import de.hterhors.semanticmr.init.reader.csv.CSVSpecifictationsReader;
import de.hterhors.semanticmr.init.specifications.SpecificationsProvider;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;

public class NamedEntityRecognitionAndLinkingExample {

	private static final File entitySpecifications = new File(
			"src/main/resources/examples/nerla/specs/csv/entitySpecifications.csv");
	private static final File slotSpecifications = new File(
			"src/main/resources/examples/nerla/specs/csv/slotSpecifications.csv");
	private static final File entityStructureSpecifications = new File(
			"src/main/resources/examples/nerla/specs/csv/entityStructureSpecifications.csv");
	private static final File slotPairConstraitsSpecifications = new File(
			"src/main/resources/examples/nerla/specs/csv/slotPairExcludingConstraints.csv");

	public final static SpecificationsProvider specificationProvider = new SpecificationsProvider(
			new CSVSpecifictationsReader(entitySpecifications, entityStructureSpecifications, slotSpecifications,
					slotPairConstraitsSpecifications));

	public static void main(String[] args) throws IOException {

		SystemInitializer.initialize(specificationProvider).apply();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/nerla/corpus/instances/"), shuffleCorpusDistributor);

		NerlaCandidateProviderCollection candidateProvider = new NerlaCandidateProviderCollection(
				new InMEMDictionaryBasedCandidateProvider(
						new File("src/main/resources/examples/nerla/dicts/organismModel.dict")));

		IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(EEvaluationDetail.DOCUMENT_LINKED);

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));

		List<AbstractFeatureTemplate<?, ?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new IntraTokenNerlaTemplate());
		featureTemplates.add(new NerlaTokenContextTemplate());
		featureTemplates.add(new MorphologicalNerlaTemplate());

		IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations()));

		int numberOfEpochs = 4;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		IStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);
		IStoppingCriterion noModelChangeCrit = new NoChangeCrit(3, s -> s.getModelScore());

		EntityRecLinkExplorer explorer = new EntityRecLinkExplorer(candidateProvider);

		final File modelDir = new File("models/nerla/test1/");
		final String modelName = "ModelName34";

		Model model;
		try {
			model = Model.load(modelDir, modelName);
		} catch (Exception e) {
		}
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

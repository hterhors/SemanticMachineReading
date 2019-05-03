package de.hterhors.semanticmr.examples.olp2.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.CRF;
import de.hterhors.semanticmr.crf.exploration.EntityTemplateExplorer;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.SlotFillingObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
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
import de.hterhors.semanticmr.eval.BeamSearchEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.EvaluationResultPrinter;
import de.hterhors.semanticmr.examples.olp2.corpus.preprocessing.StartPreprocessing;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.init.specifications.ScopeInitializer;
import de.hterhors.semanticmr.json.JsonNerlaProvider;
import de.hterhors.semanticmr.nerla.NerlaCollector;

public class Olp2ExtractionMain {

	public static void main(String[] args) throws IOException, DocumentLinkedAnnotationMismatchException {

		ScopeInitializer crfInitializer = ScopeInitializer.addScope(StartPreprocessing.de_specificationProvider)
				.apply();

		CartesianEvaluator cartesian = new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE);
		BeamSearchEvaluator beam = new BeamSearchEvaluator(EEvaluationDetail.ENTITY_TYPE, 2);

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/de/corpus/sf/"), shuffleCorpusDistributor);

		NerlaCollector nerlaProvider = new NerlaCollector(instanceProvider.getInstances());
		nerlaProvider.addNerlaProvider(
				new JsonNerlaProvider(new File("src/main/resources/examples/olp2/de/nerla/nerla.json")));

		AnnotationCandidateProviderCollection candidateProvider = nerlaProvider.collect();

//		AnnotationCandidateProviderCollection candidateProvider = new AnnotationCandidateProviderCollection(
//				instanceProvider.getInstances());
//		candidateProvider.setEntityTypeCandidateProvider();

		IObjectiveFunction objectiveFunction = new SlotFillingObjectiveFunction(cartesian);

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));

		List<AbstractFeatureTemplate<?, ?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new IntraTokenTemplate());
		featureTemplates.add(new TokenContextTemplate());
		featureTemplates.add(new InBetweenContextTemplate());

		IStateInitializer stateInitializer = (instance) -> new State(instance,
				new Annotations(new EntityTemplate(AnnotationBuilder.toAnnotation("Goal"))));

		int numberOfEpochs = 1;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
//		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		IStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);
		IStoppingCriterion noModelChangeCrit = new NoChangeCrit(3, s -> s.getModelScore());

		EntityTemplateExplorer explorer = new EntityTemplateExplorer(candidateProvider);

		final File modelDir = new File("models/olp2/test1/");
		final String modelName = "Model3";

		Model model;
		try {
			model = Model.load(modelDir, modelName);
		} catch (Exception e) {
		}
		model = new Model(featureTemplates);

		CRF crf = new CRF(crfInitializer, model, explorer, sampler, stateInitializer, objectiveFunction);

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

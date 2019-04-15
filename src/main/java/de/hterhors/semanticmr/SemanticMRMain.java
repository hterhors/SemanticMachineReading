package de.hterhors.semanticmr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.DocumentCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.IInstanceDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.ObjectiveFunction;
import de.hterhors.semanticmr.crf.Trainer;
import de.hterhors.semanticmr.crf.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.impl.RandomSwitchSamplingStrategy;
import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.stopcrit.impl.MaxChainLength;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.TestTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.examples.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.json.JsonNerlaProvider;
import de.hterhors.semanticmr.nerla.NerlaCollector;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;

public class SemanticMRMain {

	public static void main(String[] args) throws IOException {

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSpecs().specificationProvider)
				.addNormalizationFunction(EntityType.get("Weight"), new WeightNormalization()).apply();

		IInstanceDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(0.1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(new File("src/main/resources/corpus/data/instances/"),
				shuffleCorpusDistributor);

		NerlaCollector nerlaProvider = new NerlaCollector(instanceProvider.getInstances());
		nerlaProvider
				.addNerlaProvider(new JsonNerlaProvider(new File("src/main/resources/corpus/data/nerla/nerla.json")));

		DocumentCandidateProviderCollection candidateProvider = nerlaProvider.collect();

		HardConstraintsProvider constraintsProvider = new HardConstraintsProvider(initializer);

		ObjectiveFunction objectiveFunction = new ObjectiveFunction();

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));

		List<AbstractFeatureTemplate<?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new TestTemplate());

		Model model = new Model(featureTemplates, learner);

		IStateInitializer stateInitializer = ((instance) -> new State(instance,
				new Annotations(new EntityTemplate(AbstractSlotFiller
						.toSlotFiller(instance.getGoldAnnotations().getAnnotations().get(0).getEntityType())))));

		int numberOfEpochs = 10;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
//		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy(100L));
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		IStoppingCriterion stoppingCriterion = new MaxChainLength(10);

		EntityTemplateExploration explorer = new EntityTemplateExploration(candidateProvider, constraintsProvider);

		Trainer trainer = new Trainer(model, explorer, sampler, stateInitializer, stoppingCriterion, objectiveFunction,
				numberOfEpochs);

		Map<Instance, State> results = trainer.trainModel(instanceProvider.getRedistributedTrainingInstances());

		results.entrySet().forEach(System.out::println);

		trainer.printStatistics(System.out);

		System.out.println(model);

	}

}

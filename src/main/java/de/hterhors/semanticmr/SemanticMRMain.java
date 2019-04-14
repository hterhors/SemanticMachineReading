package de.hterhors.semanticmr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.DocumentCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.EntityTemplateCandidateProvider;
import de.hterhors.semanticmr.candprov.EntityTypeCandidateProvider;
import de.hterhors.semanticmr.candprov.GeneralCandidateProvider;
import de.hterhors.semanticmr.corpus.json.JsonReader;
import de.hterhors.semanticmr.corpus.json.converter.JsonInstanceWrapperToInstance;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonInstanceWrapper;
import de.hterhors.semanticmr.crf.ObjectiveFunction;
import de.hterhors.semanticmr.crf.Trainer;
import de.hterhors.semanticmr.crf.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;
import de.hterhors.semanticmr.crf.sampling.ISamplingStrategy;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.stopcrit.impl.MaxChainLength;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.TestTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class SemanticMRMain {

	public static void main(String[] args) throws IOException {

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSpecs().specificationProvider)
				.addNormalizationFunction(EntityType.get("Weight"), new WeightNormalization()).apply();

		List<Instance> trainingInstances = readTrainingInstances(initializer);

		DocumentCandidateProviderCollection documentCandidateProviderCollection = buildCandidateProvider(
				trainingInstances);

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
		AbstractSampler sampler = new EpochSwitchSampler(e -> e % 2 == 0);

		IStoppingCriterion stoppingCriterion = new MaxChainLength(10);

		EntityTemplateExploration explorer = new EntityTemplateExploration(documentCandidateProviderCollection,
				initializer.getHardConstraints());

		Trainer trainer = new Trainer(model, explorer, sampler, stateInitializer, stoppingCriterion, objectiveFunction,
				numberOfEpochs);

		Map<Instance, State> results = trainer.trainModel(trainingInstances);

		results.entrySet().forEach(System.out::println);

		trainer.printTrainingStatistics(System.out);

		System.out.println(model);

	}

	private static DocumentCandidateProviderCollection buildCandidateProvider(List<Instance> trainingInstances) {
		EntityTemplate subTemplate = new EntityTemplate(AbstractSlotFiller.toSlotFiller("MouseModel"))
				.setSingleSlotFiller(SlotType.get("hasAge"),
						AbstractSlotFiller.toSlotFiller("Age", "Eight-week-old", 36431));

		EntityTemplateCandidateProvider entityTemplateCandidateProvider = new EntityTemplateCandidateProvider(
				trainingInstances.get(0).getDocument());
		entityTemplateCandidateProvider.addSlotFiller(subTemplate);

		EntityTypeCandidateProvider entityCandidateProvider = EntityTypeCandidateProvider.getInstance();

		GeneralCandidateProvider literalCandidateProvider = new GeneralCandidateProvider(
				trainingInstances.get(0).getDocument());
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Weight", "recovery", 1329));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Adult", "Department", 253));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Female", "female", 36454));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "ChaseABC", 1814));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "locomotor", 1319));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "blabla", 1234));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "blabla2", 2345));

		DocumentCandidateProviderCollection documentCandidateProviderCollection = new DocumentCandidateProviderCollection();
		documentCandidateProviderCollection.setEntityTypeCandidateProvider(entityCandidateProvider);
		documentCandidateProviderCollection.addEntityTemplateCandidateProvider(entityTemplateCandidateProvider);
		documentCandidateProviderCollection.addLiteralCandidateProvider(literalCandidateProvider);
		return documentCandidateProviderCollection;
	}

	private static List<Instance> readTrainingInstances(SystemInitializer initializer) throws IOException {
		List<JsonInstanceWrapper> jsonInstances = new JsonReader().readInstances(
				new String(Files.readAllBytes(new File("src/main/resources/corpus/json/OrganismModel.json").toPath())));

		List<Instance> trainingInstances = new JsonInstanceWrapperToInstance(jsonInstances)
				.convertToInstances(initializer);
		return trainingInstances;
	}

}

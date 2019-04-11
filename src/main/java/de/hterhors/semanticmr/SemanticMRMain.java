package de.hterhors.semanticmr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.DocumentCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.EntityTemplateCandidateProvider;
import de.hterhors.semanticmr.candprov.EntityTypeCandidateProvider;
import de.hterhors.semanticmr.candprov.LiteralCandidateProvider;
import de.hterhors.semanticmr.corpus.json.converter.JsonToObjectConverter;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonAnnotationsWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentLinkedAnnotationWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentPositionWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentTokenWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonEntityTemplateWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonEntityTypeWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonInstanceWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonLiteralAnnotationWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonMultiFillerSlotWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSingleFillerSlotWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSlotTypeWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonTextualContentWrapper;
import de.hterhors.semanticmr.crf.ObjectiveFunction;
import de.hterhors.semanticmr.crf.Trainer;
import de.hterhors.semanticmr.crf.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.stopcrit.impl.MaxChainLength;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.TestTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.init.specifications.SystemInitializionHandler;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.annotations.EntityType;
import de.hterhors.semanticmr.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class SemanticMRMain {

	public static void main(String[] args) {

		List<JsonInstanceWrapper> toJsonInstances = new ArrayList<>();

		List<JsonDocumentTokenWrapper> tokenList = new ArrayList<>();
		tokenList.add(new JsonDocumentTokenWrapper(0, 0, 0, 0, 0, "Hallo"));
		tokenList.add(new JsonDocumentTokenWrapper(1, 1, 1, 1, 1, "World"));

		List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations = new ArrayList<>();
		List<JsonLiteralAnnotationWrapper> literalAnnotations = new ArrayList<>();
		List<JsonEntityTypeWrapper> entityTypeAnnotations = new ArrayList<>();
		List<JsonEntityTemplateWrapper> entityTemplateAnnotations = new ArrayList<>();

		Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> subSingleFillerSlots = new HashMap<>();

		subSingleFillerSlots.put(new JsonSlotTypeWrapper("hasWeight"),
				new JsonSingleFillerSlotWrapper(
						new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Weight"),
								new JsonTextualContentWrapper("100 g"), new JsonDocumentPositionWrapper(350))));

		Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots = new HashMap<>();

		singleFillerSlots.put(new JsonSlotTypeWrapper("hasGender"),
				new JsonSingleFillerSlotWrapper(new JsonEntityTypeWrapper("Male")));

		singleFillerSlots.put(new JsonSlotTypeWrapper("hasSubAnimal"),
				new JsonSingleFillerSlotWrapper(new JsonEntityTemplateWrapper(new JsonEntityTypeWrapper("MouseModel"),
						subSingleFillerSlots, new HashMap<>())));

		singleFillerSlots.put(new JsonSlotTypeWrapper("hasWeight"),
				new JsonSingleFillerSlotWrapper(
						new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Weight"),
								new JsonTextualContentWrapper("200 g"), new JsonDocumentPositionWrapper(1234))));

		Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots = new HashMap<>();
		List<JsonDocumentLinkedAnnotationWrapper> multiSlotFiller = new ArrayList<>();
		multiSlotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("rats in da hood"), new JsonDocumentPositionWrapper(666)));
		multiSlotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("rat in da hood"), new JsonDocumentPositionWrapper(999)));

		multiFillerSlots.put(new JsonSlotTypeWrapper("hasMentions"),
				new JsonMultiFillerSlotWrapper(multiSlotFiller, null, null, null));

		entityTemplateAnnotations.add(new JsonEntityTemplateWrapper(new JsonEntityTypeWrapper("RatModel"),
				singleFillerSlots, multiFillerSlots));

		JsonAnnotationsWrapper goldAnnotations = new JsonAnnotationsWrapper(docLinkedAnnotations, literalAnnotations,
				entityTypeAnnotations, entityTemplateAnnotations);

		toJsonInstances
				.add(new JsonInstanceWrapper(new JsonDocumentWrapper("Hello World", tokenList), goldAnnotations));

		JsonToObjectConverter converter = new JsonToObjectConverter(toJsonInstances);

		SystemInitializionHandler initializer = SystemInitializionHandler
				.initialize(new CSVSpecs().specificationProvider)
				.addNormalizationFunction(EntityType.get("Weight"), new WeightNormalization()).apply();

		List<Instance> instances = converter.convertToInstances(initializer);

		ObjectiveFunction objectiveFunction = new ObjectiveFunction();

		List<AbstractFeatureTemplate<?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new TestTemplate());

		IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations(
				new EntityTemplate(instance.getGoldAnnotations().getAnnotations().get(0).getEntityType()))));

		int numberOfEpochs = 10;

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));
		Model model = new Model(featureTemplates, learner);

		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();

		IStoppingCriterion stoppingCriterion = new MaxChainLength(10);

		EntityTemplate template1 = new EntityTemplate(EntityType.get("MouseModel"));
		template1.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "100 g", 350));

		EntityTemplate template2 = new EntityTemplate(EntityType.get("MouseModel"));
		template2.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "400 g", 1234));

//		template1.updateSingleFillerSlot(SlotType.get("hasSubAnimal"), template2);

		EntityTemplate goldTemplate = new EntityTemplate(EntityType.get("RatModel"));
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasGender"), AbstractSlotFiller.toSlotFiller("Male"));
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasSubAnimal"), template1);
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "200 g", 1234));
		goldTemplate.addToMultiFillerSlot(SlotType.get("hasMentions"),
				AbstractSlotFiller.toSlotFiller("Mention", "rats in da hood", 666));
		goldTemplate.addToMultiFillerSlot(SlotType.get("hasMentions"),
				AbstractSlotFiller.toSlotFiller("Mention", "rat in da hood", 999));

		EntityTemplateCandidateProvider entityTemplateCandidateProvider = new EntityTemplateCandidateProvider(
				instances.get(0).getDocument());
		entityTemplateCandidateProvider.addSlotFiller(template1);
		entityTemplateCandidateProvider.addSlotFiller(template2);

		EntityTemplateCandidateProvider entityTemplateCandidateProvider2 = new EntityTemplateCandidateProvider(
				instances.get(0).getDocument());
		entityTemplateCandidateProvider2.addSlotFiller(template1);
		entityTemplateCandidateProvider2.addSlotFiller(template2);

		EntityTypeCandidateProvider entityCandidateProvider = EntityTypeCandidateProvider.getInstance();

		LiteralCandidateProvider literalCandidateProvider = new LiteralCandidateProvider(
				instances.get(0).getDocument());
		literalCandidateProvider
				.addSlotFiller(new LiteralAnnotation(EntityType.get("Mention"), new TextualContent("rating")));
		literalCandidateProvider
				.addSlotFiller(new LiteralAnnotation(EntityType.get("Mention"), new TextualContent("rat")));
		literalCandidateProvider.addSlotFiller(new DocumentLinkedAnnotation(EntityType.get("Weight"),
				new TextualContent("200 g"), new DocumentPosition(12345)));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "rats in da hood", 666));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "rat in da hood", 999));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Weight", "200 g", 1234));

		Annotations goldAnnotations1 = new Annotations(goldTemplate);
		Annotations goldAnnotations2 = new Annotations(goldTemplate);

		List<Instance> trainingInstances = new ArrayList<>(instances);

		System.out.println(trainingInstances.get(0).getGoldAnnotations().getAnnotations().get(0).toPrettyString());
		System.out.println(goldTemplate.toPrettyString());
		System.out.println(trainingInstances.get(0).getGoldAnnotations().getAnnotations().get(0).equals(goldTemplate));

		// trainingInstances.add(new Instance(document, goldAnnotations1));
//		trainingInstances.add(new Instance(document2, goldAnnotations2));

		DocumentCandidateProviderCollection documentCandidateProviderCollection = new DocumentCandidateProviderCollection();
		documentCandidateProviderCollection.setEntityTypeCandidateProvider(entityCandidateProvider);
		documentCandidateProviderCollection.addEntityTemplateCandidateProvider(entityTemplateCandidateProvider);
		documentCandidateProviderCollection.addEntityTemplateCandidateProvider(entityTemplateCandidateProvider2);
		documentCandidateProviderCollection.addLiteralCandidateProvider(literalCandidateProvider);

		EntityTemplateExploration explorer = new EntityTemplateExploration(documentCandidateProviderCollection,
				initializer.getHardConstraints());

		Trainer trainer = new Trainer(model, explorer, sampler, stateInitializer, stoppingCriterion, objectiveFunction,
				numberOfEpochs);

		Map<Instance, State> results = trainer.trainModel(trainingInstances);

		results.entrySet().forEach(System.out::println);

		trainer.printTrainingStatistics(System.out);

	}

}

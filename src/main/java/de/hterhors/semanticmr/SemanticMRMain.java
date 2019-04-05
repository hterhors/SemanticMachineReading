package de.hterhors.semanticmr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.ObjectiveFunction;
import de.hterhors.semanticmr.crf.Trainer;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.crf.templates.TestTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.exploration.DetermineStructureDepth;
import de.hterhors.semanticmr.exploration.EntityTemplateExploration;
import de.hterhors.semanticmr.exploration.candidateprovider.EntityTemplateCandidateProvider;
import de.hterhors.semanticmr.exploration.candidateprovider.EntityTypeCandidateProvider;
import de.hterhors.semanticmr.exploration.candidateprovider.ISlotFillerCandidateProvider;
import de.hterhors.semanticmr.exploration.candidateprovider.LiteralCandidateProvider;
import de.hterhors.semanticmr.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.init.reader.csv.CSVSpecifictationsReader;
import de.hterhors.semanticmr.init.specifications.SpecificationsProvider;
import de.hterhors.semanticmr.init.specifications.SystemInitializionHandler;
import de.hterhors.semanticmr.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.DocumentLink;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.Literal;
import de.hterhors.semanticmr.structure.slotfiller.container.DocumentPosition;
import de.hterhors.semanticmr.structure.slotfiller.container.TextualContent;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class SemanticMRMain {

	public static void main(String[] args) {

		File entitySpecifications = new File("src/main/resources/specifications/csv/entitySpecifications.csv");
		File slotSpecifications = new File("src/main/resources/specifications/csv/slotSpecifications.csv");
		File entityStructureSpecifications = new File(
				"src/main/resources/specifications/csv/entityStructureSpecifications.csv");
		File slotPairConstraitsSpecifications = new File(
				"src/main/resources/specifications/csv/slotPairConstraintsSpecifications.csv");

		SpecificationsProvider specificationProvider = new SpecificationsProvider(
				new CSVSpecifictationsReader(entitySpecifications, entityStructureSpecifications, slotSpecifications,
						slotPairConstraitsSpecifications));

		SystemInitializionHandler initializer = new SystemInitializionHandler();
		initializer.register(SlotType.getInitializationInstance());
		initializer.register(EntityType.getInitializationInstance());
		initializer.initialize(specificationProvider)
				.registerNormalizationFunction(EntityType.get("Weight"), new WeightNormalization()).close();

		EntityTemplate template1 = new EntityTemplate(EntityType.get("RatModel"));
		template1.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "100 g", 1234));

		EntityTemplate template2 = new EntityTemplate(EntityType.get("RatModel"));
		template2.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "400 g", 1234));

		template1.updateSingleFillerSlot(SlotType.get("hasSubAnimal"), template2);

		EntityTemplate goldTemplate = new EntityTemplate(EntityType.get("RatModel"));
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasGender"), AbstractSlotFiller.toSlotFiller("Male"));
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasSubAnimal"), template1);
		goldTemplate.updateSingleFillerSlot(SlotType.get("hasWeight"),
				AbstractSlotFiller.toSlotFiller("Weight", "200 g", 1234));
		goldTemplate.addToMultiFillerSlot(SlotType.get("hasMentions"),
				AbstractSlotFiller.toSlotFiller("Mention", "rats in da hood", 666));
		goldTemplate.addToMultiFillerSlot(SlotType.get("hasMentions"),
				AbstractSlotFiller.toSlotFiller("Mention", "rat in da hood", 999));

		EntityTemplateCandidateProvider entityTemplateCandidateProvider = new EntityTemplateCandidateProvider();
		entityTemplateCandidateProvider.addSlotFiller(template1);
		entityTemplateCandidateProvider.addSlotFiller(template2);

		ISlotFillerCandidateProvider<EntityType> entityCandidateProvider = new EntityTypeCandidateProvider();

		LiteralCandidateProvider literalCandidateProvider = new LiteralCandidateProvider();
		literalCandidateProvider.addSlotFiller(new Literal(EntityType.get("Mention"), new TextualContent("rating")));
		literalCandidateProvider.addSlotFiller(new Literal(EntityType.get("Mention"), new TextualContent("rat")));
		literalCandidateProvider.addSlotFiller(
				new DocumentLink(EntityType.get("Weight"), new TextualContent("200 g"), new DocumentPosition(12345)));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "rats in da hood", 666));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Mention", "rat in da hood", 999));
		literalCandidateProvider.addSlotFiller(AbstractSlotFiller.toSlotFiller("Weight", "200 g", 1234));

		List<ISlotFillerCandidateProvider<?>> slotFillerCandidateProvider = new ArrayList<>();
		slotFillerCandidateProvider.add(entityCandidateProvider);
		slotFillerCandidateProvider.add(literalCandidateProvider);
		slotFillerCandidateProvider.add(entityTemplateCandidateProvider);

		EntityTemplateExploration explorer = new EntityTemplateExploration(slotFillerCandidateProvider,
				new HardConstraintsProvider(specificationProvider));

		ObjectiveFunction objectiveFunction = new ObjectiveFunction();

		System.out.println(goldTemplate.toPrettyString());

		List<AbstractFactorTemplate> factorTemplates = new ArrayList<>();

		factorTemplates.add(new TestTemplate());

		int maxNumberOfSamplingSteps = 10;
		int numberOfEpochs = 10;
		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0, 0, false), new L2(0.0001));
		Model model = new Model(factorTemplates, learner);
		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();

		Trainer trainer = new Trainer(maxNumberOfSamplingSteps, numberOfEpochs, explorer, objectiveFunction, model,
				sampler);

		List<Instance> trainingInstances = new ArrayList<>();

		Document document = new Document();

		trainingInstances.add(new Instance(document, goldTemplate));

		trainer.train(trainingInstances);

	}

}

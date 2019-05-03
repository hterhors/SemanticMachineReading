package de.hterhors.semanticmr.examples.olp2.corpus.qa;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.olp2.corpus.preprocessing.StartPreprocessing;
import de.hterhors.semanticmr.examples.olp2.corpus.preprocessing.XMLReader;
import de.hterhors.semanticmr.init.specifications.ScopeInitializer;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;

public class CreateJsonCorpus {

	final private XMLReader reader;

	public static void main(String[] args) throws Exception {
		new CreateJsonCorpus("de");

	}

	public CreateJsonCorpus(final String language) throws Exception {
		ScopeInitializer init = null;
		if (language.equals("en"))
			init = ScopeInitializer.addScope(StartPreprocessing.en_specificationProvider).apply();

		if (language.equals("de"))
			init = ScopeInitializer.addScope(StartPreprocessing.de_specificationProvider).apply();

		this.reader = new XMLReader(new File("olp2/SemiStructured/"));

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/" + language + "/corpus/sf/"), shuffleCorpusDistributor);

		RuleBasedQA qaData = new RuleBasedQA(this.reader, instanceProvider.getInstances());

		List<Instance> instances = new ArrayList<>();

		for (Entry<Instance, List<QuestionAnswerPair>> i : qaData.questionsForInstances.entrySet()) {

			Instance instance = i.getKey();

			System.out.println("Build instance: " + instance.getName());

			List<AbstractAnnotation> listOfAnnotations = new ArrayList<>();

			EntityTemplate ita = new EntityTemplate(AnnotationBuilder.toAnnotation("QAInstance"));

			EntityTemplate teamA = new EntityTemplate(getTeam(instance.getName(), "Team1"));

			for (EntityTypeAnnotation playerAnnotation : getPlayerAnnotations(instance.getName(), "Team1")) {

				teamA.addMultiSlotFiller(SlotType.get("hasPlayers"), playerAnnotation);
			}

			EntityTemplate teamB = new EntityTemplate(getTeam(instance.getName(), "Team2"));

			for (EntityTypeAnnotation playerAnnotation : getPlayerAnnotations(instance.getName(), "Team2")) {

				teamB.addMultiSlotFiller(SlotType.get("hasPlayers"), playerAnnotation);
			}

			ita.setSingleSlotFiller(SlotType.get("hasTeamA"), teamA);
			ita.setSingleSlotFiller(SlotType.get("hasTeamB"), teamB);

			for (QuestionAnswerPair pair : i.getValue()) {

				EntityTemplate qa = new EntityTemplate(AnnotationBuilder.toAnnotation("QAQuestion"));
				ita.addMultiSlotFiller(SlotType.get("hasQAQuestions"), qa);

				qa.setSingleSlotFiller(SlotType.get("hasQuestion"),
						AnnotationBuilder.toAnnotation("Question", pair.question));
				qa.setSingleSlotFiller(SlotType.get("hasAnswer"),
						AnnotationBuilder.toAnnotation("Answer", pair.answer));

			}
			listOfAnnotations.add(ita);
			instances.add(new Instance(EInstanceContext.UNSPECIFIED, instance.getDocument(),
					new Annotations(listOfAnnotations)));
		}

		for (Instance instance : instances) {
			System.out.println("Write: " + instance.getName());
			InstancesToJsonInstanceWrapper w = new InstancesToJsonInstanceWrapper(Arrays.asList(instance));

			JsonInstanceIO io = new JsonInstanceIO(true);

			final String ins = io.writeInstances(w.convertToWrapperInstances(init));

			PrintStream ps = new PrintStream(new File(
					"src/main/resources/examples/olp2/" + language + "/corpus/qa/" + instance.getName() + ".json"));
			ps.println(ins);
			ps.close();

		}

	}

	private EntityTypeAnnotation getTeam(String name, String team) {
		return AnnotationBuilder.toAnnotation(reader.getTeam(name, team));
	}

	private List<EntityTypeAnnotation> getPlayerAnnotations(String instanceName, String team) {
		return reader.getPlayerAnnotations(instanceName, team).stream().map(p -> AnnotationBuilder.toAnnotation(p))
				.collect(Collectors.toList());
	}

}

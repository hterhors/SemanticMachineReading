package de.hterhors.semanticmr.examples.olp2.corpus.qa;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.olp2.corpus.preprocessing.XMLReader;
import de.hterhors.semanticmr.examples.olp2.extraction.Olp2ExtractionMain;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;

public class ToJsonCorpus {

	public final static String DEFAULT_NOT_EXISTENT_ANSWER = "N/A";

	final private XMLReader reader;

	public static void main(String[] args) throws Exception {
		new ToJsonCorpus();

	}

	public ToJsonCorpus() throws Exception {
		SystemInitializer init = SystemInitializer.initialize(Olp2ExtractionMain.de_specificationProvider).apply();

		RuleBasedQA qaData = new RuleBasedQA();

		this.reader = new XMLReader(new File("olp2/SemiStructured/"));

		List<Instance> instances = new ArrayList<>();

		for (Entry<Instance, Set<Integer>> moie : qaData.moi.entrySet()) {

			List<AbstractAnnotation<? extends AbstractAnnotation<?>>> listOfAnnotations = new ArrayList<>();

			EntityTemplate ita = new EntityTemplate(AnnotationBuilder.toAnnotation("QAInstance"));

			EntityTemplate teamA = new EntityTemplate(getTeam(moie.getKey().getName(), "Team1"));

			for (EntityTypeAnnotation playerAnnotation : getPlayerAnnotations(moie.getKey().getName(), "Team1")) {

				teamA.addMultiSlotFiller(SlotType.get("hasPlayers"), playerAnnotation);
			}

			EntityTemplate teamB = new EntityTemplate(getTeam(moie.getKey().getName(), "Team1"));

			for (EntityTypeAnnotation playerAnnotation : getPlayerAnnotations(moie.getKey().getName(), "Team2")) {

				teamB.addMultiSlotFiller(SlotType.get("hasPlayers"), playerAnnotation);
			}

			ita.setSingleSlotFiller(SlotType.get("hasTeamA"), teamA);
			ita.setSingleSlotFiller(SlotType.get("hasTeamB"), teamB);

			for (int i = 1; i <= RuleBasedQA.NUM_OF_QUESTIONS; i++) {
				String val;

				if (moie.getValue().contains(i)) {
					val = qaData.instancesPerQuestion.get(i - 1).get(moie.getKey());
				} else {
					val = DEFAULT_NOT_EXISTENT_ANSWER;
				}

				ita.setSingleSlotFiller(SlotType.get("hasQuestion" + i), AnnotationBuilder.toAnnotation("Answer", val));
			}

			listOfAnnotations.add(ita);

			instances.add(new Instance(EInstanceContext.UNSPECIFIED, moie.getKey().getDocument(),
					new Annotations(listOfAnnotations)));

		}

		for (Instance instance : instances) {

			InstancesToJsonInstanceWrapper w = new InstancesToJsonInstanceWrapper(Arrays.asList(instance));

			JsonInstanceIO io = new JsonInstanceIO(true);

			final String ins = io.writeInstances(w.convertToWrapperInstances(init));

			PrintStream ps = new PrintStream(
					new File("src/main/resources/examples/olp2/de/corpus/qa/" + instance.getName() + ".json"));
			ps.println(ins);
			ps.close();

		}

	}

	private EntityTypeAnnotation getTeam(String name, String team) {
		return AnnotationBuilder.toAnnotation(reader.teams.get(name + ".xml").get(team));
	}

	private List<EntityTypeAnnotation> getPlayerAnnotations(String instanceName, String team) {
		return reader.player.get(instanceName + ".xml").get(team).stream().map(p -> AnnotationBuilder.toAnnotation(p))
				.collect(Collectors.toList());
	}

}

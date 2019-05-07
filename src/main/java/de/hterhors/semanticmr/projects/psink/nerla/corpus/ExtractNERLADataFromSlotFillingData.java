package de.hterhors.semanticmr.projects.psink.nerla.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.slotfilling.specs.SFSpecs;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.projects.psink.normalization.WeightNormalization;

public class ExtractNERLADataFromSlotFillingData {

	public static void main(String[] args) throws IOException {
		new ExtractNERLADataFromSlotFillingData();
	}

	public ExtractNERLADataFromSlotFillingData() throws FileNotFoundException {
		SystemScope.Builder.getSpecsHandler().addScopeSpecification(SFSpecs.systemsScopeReader).build();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/slotfilling/corpus/instances/"), shuffleCorpusDistributor);

		for (Instance instance : instanceProvider.getInstances()) {
			List<Instance> newInstances = new ArrayList<>();

			Set<AbstractAnnotation> annotations = new HashSet<>();

			for (EntityTemplate annotation : instance.getGoldAnnotations().<EntityTemplate>getAnnotations()) {

				EntityTemplateAnnotationFilter filter = annotation.filter().docLinkedAnnoation().nonEmpty()
						.singleSlots().build();

				for (Entry<SlotType, AbstractAnnotation> a : filter.getSingleAnnotations().entrySet()) {
					annotations.add(a.getValue());
				}

			}

			newInstances.add(new Instance(EInstanceContext.UNSPECIFIED, instance.getDocument(),
					new Annotations(new ArrayList<>(annotations))));

			InstancesToJsonInstanceWrapper conv = new InstancesToJsonInstanceWrapper(newInstances);

			JsonInstanceIO io = new JsonInstanceIO(true);

			String jsonString = io.writeInstances(conv.convertToWrapperInstances());

			PrintStream ps = new PrintStream(
					new File("src/main/resources/examples/nerla/corpus/instances/" + instance.getName() + ".json"));

			ps.println(jsonString);
			ps.close();

		}
	}

}

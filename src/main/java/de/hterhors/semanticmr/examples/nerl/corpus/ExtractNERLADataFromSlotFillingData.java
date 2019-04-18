package de.hterhors.semanticmr.examples.nerl.corpus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSlotFillingSpecs;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.json.structure.JsonInstanceIO;

public class ExtractNERLADataFromSlotFillingData {

	public static void main(String[] args) throws IOException {

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSlotFillingSpecs().specificationProvider).apply();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(new File("src/main/resources/corpus/data/instances/"),
				shuffleCorpusDistributor);

		for (Instance instance : instanceProvider.getInstances()) {
			List<Instance> newInstances = new ArrayList<>();

			List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations = new ArrayList<>();

			for (EntityTemplate annotation : instance.getGoldAnnotations().<EntityTemplate>getAnnotations()) {

				EntityTemplateAnnotationFilter filter = annotation.filter().docLinkedAnnoation().nonEmpty()
						.singleSlots().build();

				for (Entry<SlotType, AbstractSlotFiller<? extends AbstractSlotFiller<?>>> a : filter
						.getSingleAnnotations().entrySet()) {
					annotations.add(a.getValue());
				}

			}

			newInstances.add(
					new Instance(EInstanceContext.UNSPECIFIED, instance.getDocument(), new Annotations(annotations)));
			InstancesToJsonInstanceWrapper conv = new InstancesToJsonInstanceWrapper(newInstances);

			JsonInstanceIO io = new JsonInstanceIO(true);

			String jsonString = io.writeInstances(conv.convertToWrapperInstances(initializer));

			PrintStream ps = new PrintStream(
					new File("src/main/resources/examples/nerla/corpus/instances/" + instance.getName() + ".json"));

			ps.println(jsonString);
			ps.close();

		}

	}

}

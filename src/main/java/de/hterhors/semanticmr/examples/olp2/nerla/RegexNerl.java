package de.hterhors.semanticmr.examples.olp2.nerla;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.olp2.extraction.Olp2ExtractionMain;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;

public class RegexNerl {

	public static void main(String[] args) throws Exception {

		SystemInitializer initializer = SystemInitializer.initialize(Olp2ExtractionMain.de_specificationProvider)
				.apply();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(0.2F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/de/corpus/instances/"), shuffleCorpusDistributor);

		List<JsonEntityAnnotationWrapper> wrapper = new ArrayList<>();
		for (Instance instance : instanceProvider.getInstances()) {

			System.out.println(instance.getName());

			for (String entityTypeName : initializer.getSpecificationProvider().getSpecifications()
					.getEntityTypeNames()) {

				Matcher fullM = Pattern.compile(entityTypeName, Pattern.CASE_INSENSITIVE)
						.matcher(instance.getDocument().documentContent);

				addAnnotations(wrapper, instance, entityTypeName, fullM);

				for (String string : entityTypeName.split(" ")) {
					Matcher m = Pattern.compile(string).matcher(instance.getDocument().documentContent);
					addAnnotations(wrapper, instance, entityTypeName, m);
				}
			}
			System.out.println(wrapper.size());
		}

		JsonNerlaIO io = new JsonNerlaIO(true);
		String jsonFile = io.writeInstances(wrapper);

		PrintStream ps = new PrintStream(new File("src/main/resources/examples/olp2/de/nerla/nerla.json"));
		ps.println(jsonFile);
		ps.close();
//		anns.forEach(a -> System.out.println(a.toPrettyString()));

	}

	private static void addAnnotations(final List<JsonEntityAnnotationWrapper> wrapper, Instance instance,
			String entityTypeName, Matcher fullM) {
		while (fullM.find()) {
			try {
				AnnotationBuilder.toAnnotation(instance.getDocument(), entityTypeName, fullM.group(), fullM.start());
				JsonEntityAnnotationWrapper w = new JsonEntityAnnotationWrapper(instance.getDocument().documentID,
						entityTypeName, fullM.start(), fullM.group());
				wrapper.add(w);
			} catch (DocumentLinkedAnnotationMismatchException e) {
			}
		}
	}

}

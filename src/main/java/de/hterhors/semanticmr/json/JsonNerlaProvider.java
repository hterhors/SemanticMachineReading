package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;
import de.hterhors.semanticmr.nerla.INerlaProvider;

public class JsonNerlaProvider implements INerlaProvider {

	private final File nerlaFile;

	public JsonNerlaProvider(final File nerlaFile) {
		this.nerlaFile = nerlaFile;

		this.validateFiles();
	}

	@Override
	public Map<Instance, List<DocumentLinkedAnnotation>> getForInstances(List<Instance> instances) throws IOException {

		final List<JsonEntityAnnotationWrapper> jsonNerla = new JsonNerlaIO(true)
				.readInstances(new String(Files.readAllBytes(nerlaFile.toPath())));

		final Map<String, Instance> map = new HashMap<>();

		for (Instance instance : instances) {
			map.put(instance.getDocument().documentID, instance);
		}

		final Map<Instance, List<DocumentLinkedAnnotation>> nerla = new HashMap<>();

		System.out.println("#######################LOAD NERLA#######################");
		System.out.print("Read nerla");
		int count = 0;

		List<String> exceptions = new ArrayList<>();

		for (JsonEntityAnnotationWrapper jsonEntityAnnotationWrapper : jsonNerla) {
			count++;
			if (count % 500 == 0)
				System.out.print(".");
			if (count % 5000 == 0)
				System.out.print(" - " + count + " - ");

			Instance instance = map.get(jsonEntityAnnotationWrapper.getDocumentID());

			nerla.putIfAbsent(instance, new ArrayList<>());

			try {
				nerla.get(instance)
						.add(toDocumentLinkedAnnotation(instance.getDocument(), jsonEntityAnnotationWrapper));
			} catch (DocumentLinkedAnnotationMismatchException e) {
				exceptions.add(e.getMessage());
			}
		}
		System.out.println("... done");
		if (!exceptions.isEmpty()) {
			System.out.println("WARN: Could not load all annotations. " + exceptions.size()
					+ " exceptions were thrown during loading: ");
			exceptions.stream().limit(10).forEach(System.out::println);
			System.out.println("...");
		}
		System.out.println("Total number of nerla loaded: " + (count - exceptions.size()) + " / " + count);
		System.out.println("########################################################");
		return nerla;
	}

	private DocumentLinkedAnnotation toDocumentLinkedAnnotation(Document document, JsonEntityAnnotationWrapper s)
			throws DocumentLinkedAnnotationMismatchException {
		return new DocumentLinkedAnnotation(document, EntityType.get(s.getEntityType()),
				new TextualContent(s.getSurfaceForm()), new DocumentPosition(s.getOffset()));
	}

	private void validateFiles() {
		if (!nerlaFile.exists())
			throw new IllegalArgumentException("File does not exist: " + nerlaFile.getAbsolutePath());

		if (!nerlaFile.getName().endsWith(".json"))
			System.out.println("Warn! Unexpected file format: " + nerlaFile.getName());

	}

}

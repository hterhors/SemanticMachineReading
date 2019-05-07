package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;
import de.hterhors.semanticmr.nerla.INerlaProvider;

public class JsonNerlaProvider implements INerlaProvider {
	private static Logger log = LogManager.getFormatterLogger(JsonNerlaProvider.class);

	private final File nerlaFile;

	public JsonNerlaProvider(final File nerlaFile) {
		this.nerlaFile = nerlaFile;

		this.validateFiles();
	}

	@Override
	public Map<Instance, List<DocumentLinkedAnnotation>> getForInstances(List<Instance> instances) throws IOException {
		log.info("Read named entity recognition and linking annotations...");

		final List<JsonEntityAnnotationWrapper> jsonNerla = new JsonNerlaIO(true)
				.fromJsonString(new String(Files.readAllBytes(nerlaFile.toPath())));

		final Map<String, Instance> map = new HashMap<>();

		for (Instance instance : instances) {
			map.put(instance.getDocument().documentID, instance);
		}

		final Map<Instance, List<DocumentLinkedAnnotation>> nerla = new HashMap<>();

		int count = 0;
		List<String> exceptions = new ArrayList<>();

		for (JsonEntityAnnotationWrapper jsonEntityAnnotationWrapper : jsonNerla) {
			if (count++ % 5000 == 0)
				log.debug(" - " + count + " - ");

			Instance instance = map.get(jsonEntityAnnotationWrapper.getDocumentID());

			if (instance == null)
				continue;

			nerla.putIfAbsent(instance, new ArrayList<>());

			try {
				nerla.get(instance)
						.add(toDocumentLinkedAnnotation(instance.getDocument(), jsonEntityAnnotationWrapper));
			} catch (DocumentLinkedAnnotationMismatchException e) {
				exceptions.add(e.getMessage());
			}
		}
		if (!exceptions.isEmpty()) {
			log.warn("Could not load all annotations. " + exceptions.size() + " exceptions were thrown:");
			exceptions.stream().limit(10).forEach(log::warn);
			log.warn("...");
		}
		log.info("Read annotations... done");
		log.info("Total number of annotations loaded: " + (count - exceptions.size()) + " / " + count);
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
			log.warn("Unexpected file format: " + nerlaFile.getName());

	}

}

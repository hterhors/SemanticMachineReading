package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;

public class JSONNerlaReader {
	private static Logger log = LogManager.getFormatterLogger(JSONNerlaReader.class);

	private final File nerlaFileOrDir;
	private List<JsonEntityAnnotationWrapper> jsonNerla;

	public JSONNerlaReader(final File nerlaFileOrDir, Set<Instance> instances) {

		this.nerlaFileOrDir = nerlaFileOrDir;

		Set<String> basicInstanceNames = instances.stream().map(i -> i.getName().replaceAll("\\.json", ""))
				.collect(Collectors.toSet());

		this.validateFiles();
		log.info("Read named entity recognition and linking annotations...");

		jsonNerla = new ArrayList<>();

		if (nerlaFileOrDir != null)
			if (nerlaFileOrDir.isDirectory()) {
				jsonNerla = new ArrayList<>();
				for (File nerlaJsonFile : nerlaFileOrDir.listFiles()) {

					String n = nerlaJsonFile.getName().replaceAll("\\.nerla", "").replaceAll("\\.json", "");

					if (!basicInstanceNames.isEmpty() && !basicInstanceNames.contains(n))
						continue;
					try {
						jsonNerla.addAll(new JsonNerlaIO(true)
								.fromJsonString(new String(Files.readAllBytes(nerlaJsonFile.toPath()))));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					jsonNerla = new JsonNerlaIO(true)
							.fromJsonString(new String(Files.readAllBytes(nerlaFileOrDir.toPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

	}

	public JSONNerlaReader(final File nerlaFileOrDir) {
		this(nerlaFileOrDir, Collections.emptySet());
	}

	public List<DocumentLinkedAnnotation> getForInstance(Instance instance) {

		final List<DocumentLinkedAnnotation> nerlas = new ArrayList<>();

		int count = 0;
		List<String> exceptions = new ArrayList<>();

		for (JsonEntityAnnotationWrapper jsonEntityAnnotationWrapper : jsonNerla) {

			if (instance == null)
				continue;

			if (!jsonEntityAnnotationWrapper.getDocumentID().equals(instance.getName()))
				continue;

			if (count++ % 5000 == 0)
				log.debug(" - " + count + " - ");

			try {
				nerlas.add(new DocumentLinkedAnnotation(instance.getDocument(),
						EntityType.get(jsonEntityAnnotationWrapper.getEntityType()),
						new TextualContent(jsonEntityAnnotationWrapper.getSurfaceForm()),
						new DocumentPosition(jsonEntityAnnotationWrapper.getOffset())));
			} catch (Exception e) {
//			} catch (DocumentLinkedAnnotationMismatchException e) {
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
		return nerlas;
	}

	private void validateFiles() {
		if (!nerlaFileOrDir.exists())
			throw new IllegalArgumentException("File does not exist: " + nerlaFileOrDir.getAbsolutePath());

		if (nerlaFileOrDir.isDirectory()) {
		} else if (!nerlaFileOrDir.getName().endsWith(".json"))
			log.warn("Unexpected file format: " + nerlaFileOrDir.getName());

	}

}

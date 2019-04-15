package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.structure.annotations.container.TextualContent;

public class JsonNerlaReader {

	private final File nerlaFile;

	private final SystemInitializer initializer;

	public JsonNerlaReader(final SystemInitializer initializer, final File nerlaFile) {
		this.nerlaFile = nerlaFile;
		this.initializer = initializer;
	}

	public Map<String, List<EntityTypeAnnotation>> read() throws IOException {

		final List<JsonEntityAnnotationWrapper> jsonInstances = new JsonNerlaIO(true)
				.readInstances(new String(Files.readAllBytes(nerlaFile.toPath())));

		final Map<String, List<EntityTypeAnnotation>> nerla = new HashMap<>();
		System.out.println("#######################LOAD NERLA#######################");
		System.out.print("Read nerla");
		int count = 0;
		for (JsonEntityAnnotationWrapper jsonEntityAnnotationWrapper : jsonInstances) {
			count++;
			if (count % 500 == 0)
				System.out.print(".");
			if (count % 5000 == 0)
				System.out.print(" - " + count + " - ");

			nerla.putIfAbsent(jsonEntityAnnotationWrapper.getDocumentID(), new ArrayList<>());
			nerla.get(jsonEntityAnnotationWrapper.getDocumentID())
					.add(toEntityTypeAnnotation(jsonEntityAnnotationWrapper));
		}
		System.out.println("... done");
		System.out.println("Total number of nerla loaded: " + count);
		System.out.println("########################################################");
		return nerla;
	}

	private EntityTypeAnnotation toEntityTypeAnnotation(JsonEntityAnnotationWrapper s) {

		return new DocumentLinkedAnnotation(EntityType.get(s.getEntityType()), new TextualContent(s.getSurfaceForm()),
				new DocumentPosition(s.getOffset()));
	}

}

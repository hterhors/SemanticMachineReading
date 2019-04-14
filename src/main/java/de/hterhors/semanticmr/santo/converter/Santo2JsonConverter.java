package de.hterhors.semanticmr.santo.converter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.corpus.json.JsonWriter;
import de.hterhors.semanticmr.corpus.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.converter.reader.TextualAnnotationsReader;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;

public class Santo2JsonConverter {

	final private File documentFile;
	final private File textualAnnotationsFile;
	final private File rdfAnnotationsFile;

	final private static String documentFileNameEnding = ".csv";
	final private static String textualAnnotationsFileNameEnding = ".annodb";
	final private static String rdfAnnotationsFileNameEnding = ".n-triples";

	final private SystemInitializer initializer;

	private Map<Triple, RDFRelatedAnnotation> annotations;

	private String ontologyNameSpace;
	private String resourceNameSpace;

	private SantoRDFConverter rdfConverter;

	private final Document document;

	private final String documentID;

	public Santo2JsonConverter(SystemInitializer initializer, File documentFile, File textualAnnotationsFile,
			File rdfAnnotationsFile, final String ontologyNameSpace, final String resourceNameSpace)
			throws IOException {
		this.initializer = initializer;
		this.documentFile = documentFile;
		this.textualAnnotationsFile = textualAnnotationsFile;
		this.rdfAnnotationsFile = rdfAnnotationsFile;

		this.resourceNameSpace = resourceNameSpace;
		this.ontologyNameSpace = ontologyNameSpace;

		this.validateFiles();

		this.documentID = textualAnnotationsFile.getName().replaceAll("_export", "");

		this.document = new Document(this.documentID, SantoDocumentConverter.convert(documentFile));

		this.annotations = new TextualAnnotationsReader(this.document, textualAnnotationsFile).getAnnotations();

		this.rdfConverter = new SantoRDFConverter(initializer, annotations, rdfAnnotationsFile, ontologyNameSpace,
				resourceNameSpace);
	}

	public void convert(final File writeToFile, String rootEntityTypes, boolean includeSubEntities) throws IOException {
		convert(writeToFile, new HashSet<>(Arrays.asList(rootEntityTypes)), includeSubEntities);
	}

	public void convert(final File writeToFile, Set<String> rootEntityTypes, boolean includeSubEntities)
			throws IOException {
		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> rdfAnnotations = rdfConverter
				.extract(rootEntityTypes, includeSubEntities);

		List<Instance> instances = new ArrayList<>();

		Annotations goldAnnotations = new Annotations(rdfAnnotations);

		instances.add(new Instance(document, goldAnnotations));

		InstancesToJsonInstanceWrapper conv = new InstancesToJsonInstanceWrapper(instances);

		JsonWriter writer = new JsonWriter(true);
		String json = writer.writeInstances(conv.convertToWrapperInstances(initializer));

		final PrintStream ps = new PrintStream(writeToFile);
		ps.println(json);
		ps.close();
	}

	private void validateFiles() {
		if (!this.documentFile.exists())
			throw new IllegalArgumentException("Document file does not exist: " + documentFile.getAbsolutePath());
		if (!this.textualAnnotationsFile.exists())
			throw new IllegalArgumentException(
					"Textual annotations file does not exist: " + documentFile.getAbsolutePath());
		if (!this.rdfAnnotationsFile.exists())
			throw new IllegalArgumentException(
					"RDF annotations file does not exist: " + documentFile.getAbsolutePath());
		if (!documentFile.getName().endsWith(documentFileNameEnding))
			System.out.println("WARN: unexpected file ending of document file: " + documentFile.getName());
		if (!textualAnnotationsFile.getName().endsWith(textualAnnotationsFileNameEnding))
			System.out.println("WARN: unexpected file ending of textual annotation file: " + documentFile.getName());
		if (!rdfAnnotationsFile.getName().endsWith(rdfAnnotationsFileNameEnding))
			System.out.println("WARN: unexpected file ending of rdf annotation file: " + documentFile.getName());

	}

	public void addIgnoreProperty(String propertyToIgnore) {
		rdfConverter.addIgnoreProperty(propertyToIgnore);
	}

}

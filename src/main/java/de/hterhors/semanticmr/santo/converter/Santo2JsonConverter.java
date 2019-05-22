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

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.converter.reader.TextualAnnotationsReader;

public class Santo2JsonConverter {

	final private File documentFile;
	final private File textualAnnotationsFile;
	final private File rdfAnnotationsFile;

	final private static String documentFileNameEnding = ".csv";
	final private static String textualAnnotationsFileNameEnding = ".annodb";
	final private static String rdfAnnotationsFileNameEnding = ".n-triples";

	private Map<Triple, RDFRelatedAnnotation> annotations;

	private String ontologyNameSpace;
	private String resourceNameSpace;

	private SantoRDFConverter rdfConverter;

	private final Document document;

	private final String documentID;

	public Santo2JsonConverter(SystemScope systemScope, final String documentID, File documentFile,
			File textualAnnotationsFile, File rdfAnnotationsFile, final String ontologyNameSpace,
			final String resourceNameSpace) throws IOException {
		this.documentFile = documentFile;
		this.textualAnnotationsFile = textualAnnotationsFile;
		this.rdfAnnotationsFile = rdfAnnotationsFile;

		this.resourceNameSpace = resourceNameSpace;
		this.ontologyNameSpace = ontologyNameSpace;

		this.validateFiles();

		this.documentID = documentID;
		this.document = new Document(this.documentID, SantoDocumentConverter.convert(documentFile));

		this.annotations = new TextualAnnotationsReader(this.document, textualAnnotationsFile).getAnnotations();

		this.rdfConverter = new SantoRDFConverter(systemScope, annotations, rdfAnnotationsFile, ontologyNameSpace,
				resourceNameSpace);
	}

	public void convert(final File writeToFile, String rootEntityTypes, boolean includeSubEntities,
			boolean jsonPrettyString) throws IOException {
		convert(writeToFile, new HashSet<>(Arrays.asList(rootEntityTypes)), includeSubEntities, jsonPrettyString);
	}

	public void convert(final File writeToFile, Set<String> rootEntityTypes, boolean includeSubEntities,
			boolean jsonPrettyString) throws IOException {

		final List<AbstractAnnotation> rdfAnnotations = rdfConverter.extract(document, rootEntityTypes,
				includeSubEntities);

		List<Instance> instances = new ArrayList<>();

		Annotations goldAnnotations = new Annotations(rdfAnnotations);

		instances.add(new Instance(EInstanceContext.UNSPECIFIED, document, goldAnnotations));

		InstancesToJsonInstanceWrapper conv = new InstancesToJsonInstanceWrapper(instances);

		JsonInstanceIO writer = new JsonInstanceIO(jsonPrettyString);
		writer.writeInstances(writeToFile, conv.convertToWrapperInstances());
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

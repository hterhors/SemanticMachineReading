package de.hterhors.semanticmr.santo.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.init.specifications.ScopeInitializer;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.helper.PatternCollection;
import de.hterhors.semanticmr.santo.helper.SantoHelper;

public class SantoRDFConverter {

	public static final String RDF_TYPE_NAMESPACE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	final private ScopeInitializer initializer;

	private Map<String, Map<String, Set<String>>> rdfData;

	private Set<String> rootDataPoints;

	final private Set<String> skipProperties = new HashSet<>();

	final private Map<Triple, RDFRelatedAnnotation> annotations;

	final private File rdfAnnotationsFile;

	final private String ontologyNameSpace;
	final private String dataNameSpace;

	public SantoRDFConverter(ScopeInitializer initializer, Map<Triple, RDFRelatedAnnotation> annotations,
			File rdfAnnotationsFile, final String ontologyNameSpace, final String dataNameSpace) throws IOException {
		this.initializer = initializer;
		this.annotations = annotations;
		this.rdfAnnotationsFile = rdfAnnotationsFile;
		this.dataNameSpace = dataNameSpace;
		this.ontologyNameSpace = ontologyNameSpace;
		this.rdfData = readRDFData(rdfAnnotationsFile);
	}

	public List<AbstractAnnotation> extract(final Document document, final Set<String> rootEntities,
			final boolean includeSubEntities) {

		setRootEntityTypes(rootEntities, includeSubEntities);

		final List<AbstractAnnotation> instances = new ArrayList<>();

		for (String rootDataPoint : rootDataPoints) {

			final String object = rdfData.get(rootDataPoint).get(RDF_TYPE_NAMESPACE).iterator().next();
			/*
			 * TODO: Assume that there is only one rdf:type!
			 */
			final Triple linkedID = new Triple(rootDataPoint, RDF_TYPE_NAMESPACE, object);

			final RDFRelatedAnnotation annotation = annotations.get(linkedID);

			try {
				EntityTemplate entityTemplate = new EntityTemplate(
						toEntityTypeAnnotation(document, SantoHelper.getResource(object), annotation));

				fillRec(document, entityTemplate, rootDataPoint);

				instances.add(entityTemplate);
			} catch (DocumentLinkedAnnotationMismatchException e) {
				e.printStackTrace();
			}

		}

		return instances;

	}

	private void fillRec(Document document, final EntityTemplate object, String subject) {
		/*
		 * Read rdf type separately to create new instance.
		 */

		for (Entry<String, Set<String>> props : rdfData.get(subject).entrySet()) {

			if (skipProperties.contains(props.getKey()))
				continue;

			/*
			 * Do not read root rdf-type again.
			 */
			if (props.getKey().equals(RDF_TYPE_NAMESPACE))
				continue;

			final String slotName = SantoHelper.getResource(props.getKey());

			final SlotType slot = SlotType.get(slotName);

			/*
			 * Get all values.
			 */
			List<AbstractAnnotation> slotFillers = extractValuesFromPredicates(document, subject, slot, props.getKey(),
					props.getValue());

			if (slotFillers.size() == 0)
				continue;

			if (slot.isSingleValueSlot()) {
				if (slotFillers.size() > 1) {
					System.out.println("WARN! Multiple slot filler detected for single filler slot.");
				}
				final AbstractAnnotation slotFiller = slotFillers.get(0);
				object.getSingleFillerSlot(slot).set(slotFiller);
			} else {
				for (AbstractAnnotation slotFiller : slotFillers) {
					object.getMultiFillerSlot(slot).add(slotFiller);
				}
			}
		}
	}

	private List<AbstractAnnotation> extractValuesFromPredicates(Document document, String subject, SlotType slotType,
			String slotName, Set<String> slotFiller) {

		List<AbstractAnnotation> predicateValues = new ArrayList<>();

		for (String object : slotFiller) {

			final String nameSpace = SantoHelper.getNameSpace(object);

			if (object.isEmpty()) {
				throw new IllegalStateException("Object of triple is empty!");
			}
			final Triple linkedID = new Triple(subject, slotName, object);
			if (nameSpace == null) {
				LiteralAnnotation literalAnnotation;
				try {
					literalAnnotation = toLiteralAnnotation(document, slotType, linkedID);
					predicateValues.add(literalAnnotation);
				} catch (DocumentLinkedAnnotationMismatchException e) {
					e.printStackTrace();
				}
			} else if (nameSpace.equals(ontologyNameSpace)) {
				try {
					final EntityTypeAnnotation entityTypeAnnotation = toEntityTypeAnnotation(document, linkedID);
					predicateValues.add(entityTypeAnnotation);
				} catch (DocumentLinkedAnnotationMismatchException e) {
					e.printStackTrace();
				}
			} else if (nameSpace.equals(dataNameSpace)) {
				try {
					final EntityTemplate entityTemplateAnnotation = toEntityTemplate(document, slotName, linkedID);
					fillRec(document, entityTemplateAnnotation, linkedID.object);
					predicateValues.add(entityTemplateAnnotation);
				} catch (DocumentLinkedAnnotationMismatchException e) {
					e.printStackTrace();
				}
			}

		}
		return predicateValues;
	}

	private EntityTemplate toEntityTemplate(Document document, String slotName, Triple linkedID)
			throws DocumentLinkedAnnotationMismatchException {

		final String objectType = rdfData.get(linkedID.object).get(RDF_TYPE_NAMESPACE).iterator().next();

		if (annotations.containsKey(linkedID)) {
			return new EntityTemplate(AnnotationBuilder.toAnnotation(document, SantoHelper.getResource(objectType),
					annotations.get(linkedID).textMention, annotations.get(linkedID).onset));
		} else {
			return new EntityTemplate(AnnotationBuilder.toAnnotation(SantoHelper.getResource(objectType)));
		}

	}

	private EntityTypeAnnotation toEntityTypeAnnotation(Document document, final Triple linkedID)
			throws DocumentLinkedAnnotationMismatchException {

		final String entityTypeName = SantoHelper.getResource(linkedID.object);
		if (annotations.containsKey(linkedID)) {
			return AnnotationBuilder.toAnnotation(document, entityTypeName, annotations.get(linkedID).textMention,
					annotations.get(linkedID).onset);
		} else {
			return AnnotationBuilder.toAnnotation(entityTypeName);
		}
	}

	private LiteralAnnotation toLiteralAnnotation(Document document, SlotType slot, final Triple linkedID)
			throws DocumentLinkedAnnotationMismatchException {

		/**
		 * If the property was of datatype, we have to assume the entity type by taking
		 * any ( in this case the first) entity type from the slots possible entity
		 * types.
		 */
		final String entityTypeName = slot.getSlotFillerEntityTypes().iterator().next().entityTypeName;

		if (annotations.containsKey(linkedID)) {
			return AnnotationBuilder.toAnnotation(document, entityTypeName, linkedID.object,
					annotations.get(linkedID).onset);
		} else {
			return AnnotationBuilder.toAnnotation(entityTypeName, linkedID.object);
		}
	}

	private EntityTypeAnnotation toEntityTypeAnnotation(Document document, String resource,
			RDFRelatedAnnotation annotation) throws DocumentLinkedAnnotationMismatchException {
		if (annotation == null) {
			return AnnotationBuilder.toAnnotation(resource);
		} else {
			return AnnotationBuilder.toAnnotation(document, resource, annotation.textMention, annotation.onset);
		}
	}

	public void addIgnoreProperty(final String propertyToSkip) {
		skipProperties.add(propertyToSkip);
	}

	public Set<String> getSkippedProperties() {
		return Collections.unmodifiableSet(skipProperties);
	}

	/**
	 * Extracts the root data point from the triples.
	 * 
	 * @param domainSuperClass
	 */
	private void setRootEntityTypes(final Set<String> rootEntityTypes, final boolean includeSubEntities) {

		this.rootDataPoints = new HashSet<>();

		final Set<String> subEntities = new HashSet<>();

		for (String rootEntityType : rootEntityTypes) {
			subEntities.addAll(this.initializer.getSpecificationProvider().getSpecifications()
					.getSubEntityTypeNames(rootEntityType));
		}

		for (Entry<String, Map<String, Set<String>>> triple : rdfData.entrySet()) {
			for (Entry<String, Set<String>> predObj : triple.getValue().entrySet()) {

				if (!predObj.getKey().equals(RDF_TYPE_NAMESPACE))
					continue;

				for (String object : predObj.getValue()) {

					final String resource = SantoHelper.getResource(object);

					if (rootEntityTypes.contains(resource) || (includeSubEntities && subEntities.contains(resource)))
						this.rootDataPoints.add(triple.getKey());
				}
			}
		}
	}

	private Map<String, Map<String, Set<String>>> readRDFData(File inputFile) throws IOException {
		final Map<String, Map<String, Set<String>>> rdfData = new HashMap<>();

		Files.readAllLines(inputFile.toPath()).stream().forEach(l -> {
			Matcher m = PatternCollection.TRIPLE_EXTRACTOR_PATTERN.matcher(l);
			if (m.find()) {

				String domain = m.group(1);
				String property = m.group(2);
				String range = m.group(5) == null ? m.group(4) : m.group(5);
				rdfData.putIfAbsent(domain, new HashMap<>());
				rdfData.get(domain).putIfAbsent(property, new HashSet<>());
				rdfData.get(domain).get(property).add(range);

			}

		});

		return rdfData;
	}

}

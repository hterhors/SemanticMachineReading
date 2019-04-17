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

import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.helper.PatternCollection;
import de.hterhors.semanticmr.santo.helper.SantoHelper;

public class SantoRDFConverter {

	public static final String RDF_TYPE_NAMESPACE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	final private SystemInitializer initializer;

	private Map<String, Map<String, Set<String>>> rdfData;

	private Set<String> rootDataPoints;

	final private Set<String> skipProperties = new HashSet<>();

	final private Map<Triple, RDFRelatedAnnotation> annotations;

	final private File rdfAnnotationsFile;

	final private String ontologyNameSpace;
	final private String dataNameSpace;

	public SantoRDFConverter(SystemInitializer initializer, Map<Triple, RDFRelatedAnnotation> annotations,
			File rdfAnnotationsFile, final String ontologyNameSpace, final String dataNameSpace) throws IOException {
		this.initializer = initializer;
		this.annotations = annotations;
		this.rdfAnnotationsFile = rdfAnnotationsFile;
		this.dataNameSpace = dataNameSpace;
		this.ontologyNameSpace = ontologyNameSpace;
		this.rdfData = readRDFData(rdfAnnotationsFile);
	}

	public List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> extract(final Set<String> rootEntities,
			final boolean includeSubEntities) {

		setRootEntityTypes(rootEntities, includeSubEntities);

		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> instances = new ArrayList<>();

		for (String rootDataPoint : rootDataPoints) {

			final String object = rdfData.get(rootDataPoint).get(RDF_TYPE_NAMESPACE).iterator().next();
			/*
			 * TODO: Assume that there is only one rdf:type!
			 */
			final Triple linkedID = new Triple(rootDataPoint, RDF_TYPE_NAMESPACE, object);

			final RDFRelatedAnnotation annotation = annotations.get(linkedID);

			final EntityTemplate entityTemplate = new EntityTemplate(
					toEntityTypeAnnotation(SantoHelper.getResource(object), annotation));

			fillRec(entityTemplate, rootDataPoint);

			instances.add(entityTemplate);

		}

		return instances;

	}

	private void fillRec(final EntityTemplate object, String subject) {
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
			List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFillers = extractValuesFromPredicates(subject,
					slot, props.getKey(), props.getValue());

			if (slotFillers.size() == 0)
				continue;

			if (slot.isSingleValueSlot) {
				if (slotFillers.size() > 1) {
					System.out.println("WARN! Multiple slot filler detected for single filler slot.");
				}
				final AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller = slotFillers.get(0);
				object.getSingleFillerSlot(slot).set(slotFiller);
			} else {
				for (AbstractSlotFiller<? extends AbstractSlotFiller<?>> slotFiller : slotFillers) {
					object.getMultiFillerSlot(slot).add(slotFiller);
				}
			}
		}
	}

	private List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> extractValuesFromPredicates(String subject,
			SlotType slotType, String slotName, Set<String> slotFiller) {

		List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> predicateValues = new ArrayList<>();

		for (String object : slotFiller) {

			final String nameSpace = SantoHelper.getNameSpace(object);

			if (object.isEmpty()) {
				throw new IllegalStateException("Object of triple is empty!");
			}
			final Triple linkedID = new Triple(subject, slotName, object);
			if (nameSpace == null) {
				final LiteralAnnotation literalAnnotation = toLiteralAnnotation(slotType, linkedID);
				predicateValues.add(literalAnnotation);
			} else if (nameSpace.equals(ontologyNameSpace)) {
				final EntityTypeAnnotation entityTypeAnnotation = toEntityTypeAnnotation(linkedID);
				predicateValues.add(entityTypeAnnotation);
			} else if (nameSpace.equals(dataNameSpace)) {
				final EntityTemplate entityTemplateAnnotation = toEntityTemplate(slotName, linkedID);
				fillRec(entityTemplateAnnotation, linkedID.object);
				predicateValues.add(entityTemplateAnnotation);
			}

		}
		return predicateValues;
	}

	private EntityTemplate toEntityTemplate(String slotName, Triple linkedID) {

		final String objectType = rdfData.get(linkedID.object).get(RDF_TYPE_NAMESPACE).iterator().next();

		if (annotations.containsKey(linkedID)) {
			return new EntityTemplate(AbstractSlotFiller.toSlotFiller(SantoHelper.getResource(objectType),
					annotations.get(linkedID).textMention, annotations.get(linkedID).onset));
		} else {
			return new EntityTemplate(AbstractSlotFiller.toSlotFiller(SantoHelper.getResource(objectType)));
		}

	}

	private EntityTypeAnnotation toEntityTypeAnnotation(final Triple linkedID) {

		final String entityTypeName = SantoHelper.getResource(linkedID.object);
		if (annotations.containsKey(linkedID)) {
			return AbstractSlotFiller.toSlotFiller(entityTypeName, annotations.get(linkedID).textMention,
					annotations.get(linkedID).onset);
		} else {
			return AbstractSlotFiller.toSlotFiller(entityTypeName);
		}
	}

	private LiteralAnnotation toLiteralAnnotation(SlotType slot, final Triple linkedID) {

		/**
		 * If the property was of datatype, we have to assume the entity type by taking
		 * any ( in this case the first) entity type from the slots possible entity
		 * types.
		 */
		final String entityTypeName = slot.getSlotFillerEntityTypes().iterator().next().entityTypeName;

		if (annotations.containsKey(linkedID)) {
			return AbstractSlotFiller.toSlotFiller(entityTypeName, linkedID.object, annotations.get(linkedID).onset);
		} else {
			return AbstractSlotFiller.toSlotFiller(entityTypeName, linkedID.object);
		}
	}

	private EntityTypeAnnotation toEntityTypeAnnotation(String resource, RDFRelatedAnnotation annotation) {
		if (annotation == null) {
			return AbstractSlotFiller.toSlotFiller(resource);
		} else {
			return AbstractSlotFiller.toSlotFiller(resource, annotation.textMention, annotation.onset);
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

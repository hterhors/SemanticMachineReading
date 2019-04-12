package de.hterhors.semanticmr.santo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;

import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class Santo2Instances {

	final public static Pattern nameSpaceExtractor = Pattern.compile("(<(http:.*)/([^/]*)>)|(.+)");

	public static final String SCIO_NAMESPACE = "http://psink.de/scio";
	public static final String RDF_TYPE_NAMESPACE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	public static final String DATA_NAMESPACE = "http://scio/data";

	private static final int ANNOTATION_ID_INDEX = 0;
	private static final int CLASS_TYPE_INDEX = 1;
	private static final int ONSET_INDEX = 2;
	private static final int OFFSET_INDEX = 3;
	private static final int TEXT_MENTION_INDEX = 4;
	private static final int META_INDEX = 5;
	private static final int RDF_LINK_INDEX = 6;

	final private SystemInitializer initializer;
	final private Map<Triple, RDFRelatedAnnotation> annotations = new HashMap<>();

	final private Map<String, Map<String, Set<String>>> rdfData = new HashMap<>();
	private Set<String> rootDataPoints;

	final private static Pattern TRIPLE_EXTRACTOR_PATTERN = Pattern
			.compile("(<http:.*?/[^/]*?>) (<http:.*?/[^/]*?>) ((<http:.*?/[^/]*?>)|\"(.*?)\") ?\\.");

	final private Set<String> skipProperties = new HashSet<>();

	public static void main(String[] args) throws IOException {

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSpecs().specificationProvider)
				.addNormalizationFunction(EntityType.get("Weight"), new WeightNormalization()).apply();

		Santo2Instances reader = new Santo2Instances(initializer);
		reader.addPropertyToSkip("<http://www.w3.org/2000/01/rdf-schema#label>");

		reader.loadFiles("data/test/N001 Yoo, Khaled et al. 2013_admin.n-triples",
				"data/test/N001 Yoo, Khaled et al. 2013_admin.annodb");

		Set<String> rootEntityTypes = new HashSet<>();
		rootEntityTypes.add("OrganismModel");
		reader.setRootEntityTypes(rootEntityTypes, true);

		List<EntityTemplate> annotations = reader.convert();

		System.out.println(annotations.size());

		for (EntityTemplate entityTemplate : annotations) {

			System.out.println(entityTemplate.toPrettyString());
		}
	}

	public Santo2Instances(SystemInitializer initializer) {
		this.initializer = initializer;
	}

	public void loadFiles(String rdfFileName, String annotationFile) throws IOException {
		readAnnotations(new File(annotationFile));

		readRDFData(new File(rdfFileName));
	}

	public List<EntityTemplate> convert() {

		final List<EntityTemplate> instances = new ArrayList<>();

		for (String rootDataPoint : rootDataPoints) {
			System.out.println("Root data point: " + rootDataPoint);

			final String object = rdfData.get(rootDataPoint).get(RDF_TYPE_NAMESPACE).iterator().next();
			/*
			 * TODO: Assume that there is only one rdf#type!
			 */
			final Triple linkedID = new Triple(rootDataPoint, RDF_TYPE_NAMESPACE, object);

			final RDFRelatedAnnotation annotation = annotations.get(linkedID);

			final EntityTemplate entityTemplate = new EntityTemplate(
					toEntityTypeAnnotation(getResource(object), annotation));

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

			/*
			 * Do not read root rdf-type again.
			 */
			if (props.getKey().equals(RDF_TYPE_NAMESPACE))
				continue;

			final String slotName = getResource(props.getKey());

			final SlotType slot = SlotType.get(slotName);

			/*
			 * Get all values.
			 */
			List<AbstractSlotFiller<?>> slotFillers = extractValuesFromPredicates(subject, slot, props.getKey(),
					props.getValue());

			if (slotFillers.size() == 0)
				continue;

			if (slot.isSingleValueSlot) {
				if (slotFillers.size() > 1) {
					System.out.println("WARN! Multiple slot filler detected for single filler slot.");
				}
				final AbstractSlotFiller<?> slotFiller = slotFillers.get(0);
				object.getSingleFillerSlot(slot).set(slotFiller);
			} else {
				for (AbstractSlotFiller<?> slotFiller : slotFillers) {
					object.getMultiFillerSlot(slot).add(slotFiller);
				}
			}
		}
	}

	private List<AbstractSlotFiller<?>> extractValuesFromPredicates(String subject, SlotType slotType, String slotName,
			Set<String> slotFiller) {

		List<AbstractSlotFiller<?>> predicateValues = new ArrayList<>();

		for (String object : slotFiller) {

			final String nameSpace = getNameSpace(object);

			if (object.isEmpty()) {
				throw new IllegalStateException("Object of triple is empty!");
			}
			final Triple linkedID = new Triple(subject, slotName, object);
			if (nameSpace == null) {
				final LiteralAnnotation literalAnnotation = toLiteralAnnotation(slotType, linkedID);
				predicateValues.add(literalAnnotation);
			} else if (nameSpace.equals(SCIO_NAMESPACE)) {
				final EntityTypeAnnotation entityTypeAnnotation = toEntityTypeAnnotation(linkedID);
				predicateValues.add(entityTypeAnnotation);
			} else if (nameSpace.equals(DATA_NAMESPACE)) {
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
			return new EntityTemplate(AbstractSlotFiller.toSlotFiller(getResource(objectType),
					annotations.get(linkedID).textMention, annotations.get(linkedID).onset));
		} else {
			return new EntityTemplate(AbstractSlotFiller.toSlotFiller(getResource(objectType)));
		}

	}

	private EntityTypeAnnotation toEntityTypeAnnotation(final Triple linkedID) {

		final String entityTypeName = getResource(linkedID.object);
		System.out.println("linkedID:" + linkedID);
		System.out.println(annotations);
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

	public void addPropertyToSkip(final String propertyToSkip) {
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
	public void setRootEntityTypes(final Set<String> rootEntityTypes, final boolean includeSubEntities) {

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

					final String resource = getResource(object);

					if (rootEntityTypes.contains(resource) || (includeSubEntities && subEntities.contains(resource)))
						this.rootDataPoints.add(triple.getKey());
				}
			}
		}
	}

	private String getResource(String uri) {
		/**
		 * QUICK FIX:
		 */
		if (uri.equals("<http://psink.de/scio/hasTreatmentLocation>"))
			uri = "<http://psink.de/scio/hasLocation>";

		Matcher m = nameSpaceExtractor.matcher(uri);
		m.find();
		return m.group(4) == null ? m.group(3) : m.group(4);
	}

	private void readAnnotations(File annotationFile) throws IOException {
		// System.out.println(annotationFile);
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(annotationFile));
			String[] line;
			while ((line = reader.readNext()) != null) {

				if (line.length == 1 && line[0].isEmpty() || line[0].startsWith("#"))
					continue;

				if (!line[RDF_LINK_INDEX].trim().isEmpty()) {

					final Matcher m = TRIPLE_EXTRACTOR_PATTERN.matcher(line[RDF_LINK_INDEX]);
					while (m.find()) {
						final String subject = m.group(1);
						final String predicate = m.group(2);
						final String object = m.group(5) == null ? m.group(4) : m.group(5);

						final Triple linkID = new Triple(subject, predicate, object);
						try {

							annotations.put(linkID,
									new RDFRelatedAnnotation(line[TEXT_MENTION_INDEX].trim(),
											Integer.parseInt(line[ONSET_INDEX].trim()),
											Integer.parseInt(line[OFFSET_INDEX].trim()),
											line[ANNOTATION_ID_INDEX].trim(), linkID));

						} catch (Exception e) {
							System.out.println(Arrays.toString(line));
							e.printStackTrace();
							throw e;
						}

					}
				} else {
					/*
					 * No RDF related annotation found.
					 */
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * Checks and reads triple from a file.
	 * 
	 * @param inputFile
	 * @throws IOException
	 */
	private void readRDFData(File inputFile) throws IOException {

		Files.readAllLines(inputFile.toPath()).stream().forEach(l -> {
			Matcher m = TRIPLE_EXTRACTOR_PATTERN.matcher(l);
			if (m.find()) {

				String domain = m.group(1);
				String property = m.group(2);

				if (!skipProperties.contains(property)) {

					// Group 4 is the value of the data type property WITHOUT "
					String range = m.group(5) == null ? m.group(4) : m.group(5);
					rdfData.putIfAbsent(domain, new HashMap<>());
					rdfData.get(domain).putIfAbsent(property, new HashSet<>());
					rdfData.get(domain).get(property).add(range);

				}
			}

		});
	}

	private String getNameSpace(String uri) {

		if (uri.isEmpty()) {
			throw new IllegalStateException("Value of property is empty!");
		}

		Matcher m = nameSpaceExtractor.matcher(uri);
		m.find();
		return m.group(4) == null ? m.group(2) : null;
	}
}

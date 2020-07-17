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
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.exce.UnkownEnityTypeException;
import de.hterhors.semanticmr.exce.UnkownSlotTypeException;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.helper.PatternCollection;
import de.hterhors.semanticmr.santo.helper.SantoHelper;

public class SantoRDFConverter {

	public static final String RDF_TYPE_NAMESPACE = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";

	private Map<String, Map<String, Set<String>>> rdfData;

	private Set<String> rootDataPoints;

	final private Set<String> skipProperties = new HashSet<>();

	final private Map<Triple, RDFRelatedAnnotation> annotations;

	final private File rdfAnnotationsFile;

	final private String ontologyNameSpace;
	final private String dataNameSpace;

	final private boolean onlyLeafEntities;
	/**
	 * A set of slot types that should be considered. All others are filtered out.
	 */
	private Set<SlotType> filterSlotTypes;

	public SantoRDFConverter(Set<SlotType> filterSlotTypes, boolean onlyLeafEntities,
			Map<Triple, RDFRelatedAnnotation> annotations, File rdfAnnotationsFile, final String ontologyNameSpace,
			final String dataNameSpace) throws IOException {

		this.filterSlotTypes = filterSlotTypes == null ? Collections.emptySet() : filterSlotTypes;
		this.onlyLeafEntities = onlyLeafEntities;
		this.annotations = annotations;
		this.rdfAnnotationsFile = rdfAnnotationsFile;
		this.dataNameSpace = dataNameSpace;
		this.ontologyNameSpace = ontologyNameSpace;
		this.rdfData = readRDFData(rdfAnnotationsFile);
	}

	public List<AbstractAnnotation> extract(final Document document, final Set<EntityType> rootEntities,
			final boolean includeSubEntities, boolean deepRec) {

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
						toEntityTemplateTypeAnnotation(document, SantoHelper.getResource(object), annotation));

				fillRec(document, entityTemplate, rootDataPoint, deepRec);
				/*
				 * TODO: QUICK FIX to put organism models.
				 */
//				if (entityTemplate.getSingleFillerSlot(SlotType.get("hasOrganismSpecies")).containsSlotFiller()) {
//					entityTemplate.rootAnnotation = fix(
//							entityTemplate.getSingleFillerSlot(SlotType.get("hasOrganismSpecies")));
//				}
				instances.add(entityTemplate);
			} catch (DocumentLinkedAnnotationMismatchException e) {
				e.printStackTrace();
			}

		}

		for (String annotationKey : rdfData.keySet()) {

			Map<String, Set<String>> annotationValues = rdfData.get(annotationKey);

			for (String propertyURI : annotationValues.keySet()) {

				final String property = SantoHelper.getResource(propertyURI);

				try {
					SlotType.get(property);
				} catch (UnkownSlotTypeException e) {
					continue;
				}

				if (!filterSlotTypes.isEmpty() && !filterSlotTypes.contains(SlotType.get(property)))
					continue;

				for (String resource : annotationValues.get(propertyURI)) {

					final Triple linkedID = new Triple(annotationKey, propertyURI, resource);

					final RDFRelatedAnnotation annotation = annotations.get(linkedID);

					if (annotation == null)
						continue;

					try {
						EntityType.get(SantoHelper.getResource(resource));
					} catch (UnkownEnityTypeException e) {
						continue;
					}

					EntityType aet = EntityType.get(SantoHelper.getResource(resource));

					boolean pass = false;

					for (EntityType rootE : rootEntities) {

						if (rootE.isSuperEntityOf(aet)) {
							pass = true;
						}
						if (rootE == aet)
							pass = true;

						if (pass)
							break;

					}

					if (!pass)
						continue;

					try {

						EntityTemplate entityTemplate = new EntityTemplate(toEntityTemplateTypeAnnotation(document,
								SantoHelper.getResource(resource), annotation));
						instances.add(entityTemplate);

					} catch (DocumentLinkedAnnotationMismatchException e) {
						e.printStackTrace();
					}

				}

			}

		}

		return instances;

	}

	private void fillRec(Document document, final EntityTemplate object, String subject, boolean deepRec) {
		/*
		 * Read rdf type separately to create new instance.
		 */

		for (Entry<String, Set<String>> props : rdfData.get(subject).entrySet()) {

//			if(props.getKey().contains("hasTreatment"))
//{
//	System.out.println();
//}
//			if (subject.contains("Investigation_")) {
//
//				String id = subject.split("_")[1];
//				id = id.substring(0, id.length() - 1);
//				object.setSingleSlotFiller(SlotType.get("hasID"), AnnotationBuilder.toAnnotation("ID", id));
//
//			}

			if (skipProperties.contains(props.getKey()))
				continue;

			/*
			 * Do not read root rdf-type again.
			 */
			if (props.getKey().equals(RDF_TYPE_NAMESPACE))
				continue;

			final String slotName = SantoHelper.getResource(props.getKey());
			if (slotName.endsWith("Deprecated"))
				continue;

			final SlotType slot = SlotType.get(slotName);

			if (!filterSlotTypes.isEmpty() && !filterSlotTypes.contains(slot))
				continue;

//			System.out.println(slot);

			/*
			 * Get all values.
			 */
			List<AbstractAnnotation> slotFillers = extractValuesFromPredicates(document, subject, slot, props.getKey(),
					props.getValue(), deepRec);

//			System.out.println(slotFillers);
			if (slotFillers.size() == 0)
				continue;

			if (slot.isSingleValueSlot()) {
				if (slotFillers.size() > 1) {
					System.out.println("WARN! Multiple slot filler detected for single filler slot: " + slot);
					System.out.println("WARN! Apply strategy: \"Take first.\"");
				}
				final AbstractAnnotation slotFiller = slotFillers.get(0);
				object.setSingleSlotFiller(slot, slotFiller);
			} else {
				for (AbstractAnnotation slotFiller : slotFillers) {
					object.addMultiSlotFiller(slot, slotFiller);
				}
			}
		}
	}

	private List<AbstractAnnotation> extractValuesFromPredicates(Document document, String subject, SlotType slotType,
			String slotName, Set<String> slotFiller, boolean deepRec) {

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
					if (onlyLeafEntities && !isLeafEntity(literalAnnotation.entityType))
						continue;
					predicateValues.add(literalAnnotation);
				} catch (DocumentLinkedAnnotationMismatchException e) {
					System.out.println("WARN! " + e.getMessage());
//					e.printStackTrace();
				}
			} else if (nameSpace.equals(ontologyNameSpace)) {
				try {
					final EntityTypeAnnotation entityTypeAnnotation = toEntityTypeAnnotation(document, linkedID);
					if (onlyLeafEntities && !isLeafEntity(entityTypeAnnotation.entityType))
						continue;
					predicateValues.add(entityTypeAnnotation);
				} catch (Exception e) {
					System.out.println("WARN! " + e.getMessage());
//					e.printStackTrace();
				}
			} else if (nameSpace.equals(dataNameSpace)) {
				try {
//					if(slotName.contains("type"))
//						System.out.println("nops");

//					System.out.println("->" + slotName + "" + linkedID);

//					final RecKey recKey = new RecKey(document.documentID, slotName, linkedID);
					final EntityTemplate entityTemplateAnnotation;
//					if (usedPointerMap.containsKey(recKey)) {
//						entityTemplateAnnotation = usedPointerMap.get(recKey);
//					} else {
					entityTemplateAnnotation = toEntityTemplate(document, slotName, linkedID);
//						usedPointerMap.put(recKey, entityTemplateAnnotation);

					if (onlyLeafEntities && !isLeafEntity(entityTemplateAnnotation.getEntityType()))
						continue;

					if (deepRec)
						fillRec(document, entityTemplateAnnotation, linkedID.object, deepRec);

//					}
					predicateValues.add(entityTemplateAnnotation);

				} catch (DocumentLinkedAnnotationMismatchException e) {
					System.out.println("WARN! " + e.getMessage());
//					e.printStackTrace();
				}
			}

		}
		return predicateValues;
	}

	private boolean isLeafEntity(EntityType entityType) {
		return entityType.getTransitiveClosureSubEntityTypes().isEmpty();
	}

	static class RecKey {
		public final String documentName;
		public final String slotName;
		public final Triple linkedID;

		public RecKey(String documentName, String slotName, Triple linkedID) {
			super();
			this.documentName = documentName;
			this.slotName = slotName;
			this.linkedID = linkedID;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((documentName == null) ? 0 : documentName.hashCode());
			result = prime * result + ((linkedID == null) ? 0 : linkedID.hashCode());
			result = prime * result + ((slotName == null) ? 0 : slotName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecKey other = (RecKey) obj;
			if (documentName == null) {
				if (other.documentName != null)
					return false;
			} else if (!documentName.equals(other.documentName))
				return false;
			if (linkedID == null) {
				if (other.linkedID != null)
					return false;
			} else if (!linkedID.equals(other.linkedID))
				return false;
			if (slotName == null) {
				if (other.slotName != null)
					return false;
			} else if (!slotName.equals(other.slotName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RecKey [documentName=" + documentName + ", slotName=" + slotName + ", linkedID=" + linkedID + "]";
		}

	}

	private Map<RecKey, EntityTemplate> usedPointerMap = new HashMap<>();

	private EntityTemplate toEntityTemplate(Document document, String slotName, Triple linkedID)
			throws DocumentLinkedAnnotationMismatchException {

		final String objectType = rdfData.get(linkedID.object).get(RDF_TYPE_NAMESPACE).iterator().next();
		/**
		 * TODO: QUICK FIX: REMOVE!
		 * 
		 * Quick fix for missing rdf tripple in annodb file. Search for missing rdf type
		 * in annotations
		 */

		EntityTemplate et;
		RDFRelatedAnnotation FAKE_QUICK_FIX = null;
		if (annotations.containsKey(linkedID)) {
			et = new EntityTemplate(AnnotationBuilder.toAnnotation(document, SantoHelper.getResource(objectType),
					annotations.get(linkedID).textMention, annotations.get(linkedID).onset));
		} else if ((FAKE_QUICK_FIX = QUICK_FIX(linkedID)) != null) {
//			System.err.println("APPLY QUICK FIX!: SantoRDFConverter.toEntityTemplate()");
			et = new EntityTemplate(AnnotationBuilder.toAnnotation(document, SantoHelper.getResource(objectType),
					FAKE_QUICK_FIX.textMention, FAKE_QUICK_FIX.onset));
		}

		else {
			et = new EntityTemplate(AnnotationBuilder.toAnnotation(SantoHelper.getResource(objectType)));
		}

		return et;

	}

	private RDFRelatedAnnotation QUICK_FIX(Triple linkedID) {

		Triple fakeRDFTypeTriple = new Triple(linkedID.object, RDF_TYPE_NAMESPACE, null);
		for (Entry<Triple, RDFRelatedAnnotation> a : annotations.entrySet()) {
			if (a.getKey().subject.equals(fakeRDFTypeTriple.subject)
					&& a.getKey().predicate.equals(fakeRDFTypeTriple.predicate))
				return a.getValue();

		}

		return null;
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
		final String entityTypeName = slot.getSlotFillerEntityTypes().iterator().next().name;

		if (annotations.containsKey(linkedID)) {
			return AnnotationBuilder.toAnnotation(document, entityTypeName, linkedID.object,
					annotations.get(linkedID).onset);
		} else {
			return AnnotationBuilder.toAnnotation(entityTypeName, linkedID.object);
		}
	}

	private EntityTypeAnnotation toEntityTemplateTypeAnnotation(Document document, String resource,
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
	private void setRootEntityTypes(final Set<EntityType> rootEntityTypes, final boolean includeSubEntities) {

		this.rootDataPoints = new HashSet<>();

		final Set<String> subEntities = new HashSet<>();
		final Set<String> rootEntityNames = rootEntityTypes.stream().map(r -> r.name).collect(Collectors.toSet());
		for (EntityType rootEntityType : rootEntityTypes) {
			for (EntityType string : rootEntityType.getTransitiveClosureSubEntityTypes()) {
				subEntities.add(string.name);
			}
		}

		for (Entry<String, Map<String, Set<String>>> triple : rdfData.entrySet()) {
			for (Entry<String, Set<String>> predObj : triple.getValue().entrySet()) {

				if (!predObj.getKey().equals(RDF_TYPE_NAMESPACE))
					continue;

				for (String object : predObj.getValue()) {

					final String resource = SantoHelper.getResource(object);

					if (rootEntityNames.contains(resource) || (includeSubEntities && subEntities.contains(resource)))
						this.rootDataPoints.add(triple.getKey());
				}
			}
		}
	}

	private Map<String, Map<String, Set<String>>> readRDFData(File inputFile) throws IOException {
		final Map<String, Map<String, Set<String>>> rdfData = new HashMap<>();

		Files.readAllLines(inputFile.toPath()).stream().filter(l -> !l.startsWith("#")).forEach(l -> {
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

package de.hterhors.semanticmr.json.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.json.structure.wrapper.JsonAnnotationsWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonDocumentLinkedAnnotationWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonDocumentPositionWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonDocumentTokenWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonDocumentWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonEntityTemplateWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonEntityTypeWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonInstanceWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonLiteralAnnotationWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonMultiFillerSlotWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonRootAnnotationWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonSingleFillerSlotWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonSlotTypeWrapper;
import de.hterhors.semanticmr.json.structure.wrapper.JsonTextualContentWrapper;

public class JsonInstanceWrapperToInstance {

	private List<JsonInstanceWrapper> jsonInstances;

	public JsonInstanceWrapperToInstance(List<JsonInstanceWrapper> jsonInstances) {
		this.jsonInstances = jsonInstances;
	}

	public List<Instance> convertToInstances() {
		return jsonInstances.stream().map(instanceWrapper -> toInstance(instanceWrapper)).collect(Collectors.toList());
	}

	private Instance toInstance(JsonInstanceWrapper instanceWrapper) {
		Document document = toDocument(instanceWrapper.getDocument());
		return new Instance(instanceWrapper.getContext(), document,
				toAnnotations(document, instanceWrapper.getGoldAnnotations()));
	}

	private Document toDocument(JsonDocumentWrapper document) {
		return new Document(document.getDocumentID(), toTokenList(document.getTokenList()));
	}

	private List<DocumentToken> toTokenList(List<JsonDocumentTokenWrapper> tokenListWrapper) {
		return tokenListWrapper.stream().map(w -> new DocumentToken(w.getSentenceIndex(), w.getSenTokenIndex(),
				w.getDocTokenIndex(), w.getSenCharOnset(), w.getDocCharOnset(), w.getText()))
				.collect(Collectors.toList());
	}

	private Annotations toAnnotations(Document document, JsonAnnotationsWrapper goldAnnotationsWrapper) {

		return new Annotations(collectAnnotations(document, goldAnnotationsWrapper.getDocLinkedAnnotations(),
				goldAnnotationsWrapper.getLiteralAnnotations(), goldAnnotationsWrapper.getEntityTypeAnnotations(),
				goldAnnotationsWrapper.getEntityTemplateAnnotations()));
	}

	private List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> collectAnnotations(Document document,
			List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations,
			List<JsonLiteralAnnotationWrapper> literalAnnotations, List<JsonEntityTypeWrapper> entityTypeAnnotations,
			List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {

		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations = new ArrayList<>();

		if (docLinkedAnnotations != null)
			for (JsonDocumentLinkedAnnotationWrapper wrapper : docLinkedAnnotations) {
				try {
					annotations.add(toDocumentLinkedAnnotation(document, wrapper));
				} catch (Exception e) {
					System.out.println("Can not map annotation to document: " + e.getMessage());
				}
			}
		if (literalAnnotations != null)
			for (JsonLiteralAnnotationWrapper wrapper : literalAnnotations) {
				annotations.add(toLiteralAnnotation(wrapper));
			}
		if (entityTypeAnnotations != null)
			for (JsonEntityTypeWrapper wrapper : entityTypeAnnotations) {
				annotations.add(toEntityTypeAnnotation(wrapper));
			}
		if (entityTemplateAnnotations != null)
			for (JsonEntityTemplateWrapper wrapper : entityTemplateAnnotations) {
				annotations.add(toEntityTemplate(document, wrapper));
			}
		return annotations;
	}

	private LiteralAnnotation toLiteralAnnotation(JsonLiteralAnnotationWrapper wrapper) {
		return new LiteralAnnotation(toEntityType(wrapper.getEntityType()),
				toTextualContent(wrapper.getTextualContent()));
	}

	private DocumentLinkedAnnotation toDocumentLinkedAnnotation(Document document,
			JsonDocumentLinkedAnnotationWrapper wrapper) throws DocumentLinkedAnnotationMismatchException {
		return new DocumentLinkedAnnotation(document, toEntityType(wrapper.getEntityType()),
				toTextualContent(wrapper.getTextualContent()), toDocumentPosition(wrapper.getDocumentPosition()));
	}

	private DocumentPosition toDocumentPosition(JsonDocumentPositionWrapper documentPosition) {
		return new DocumentPosition(documentPosition.getCharOffset());
	}

	private TextualContent toTextualContent(JsonTextualContentWrapper textualContent) {
		return new TextualContent(textualContent.getSurfaceForm());
	}

	private EntityType toEntityType(JsonEntityTypeWrapper wrapper) {
		return EntityType.get(wrapper.getEntityTypeName());
	}

	private EntityTypeAnnotation toEntityTypeAnnotation(JsonEntityTypeWrapper wrapper) {
		return EntityTypeAnnotation.get(EntityType.get(wrapper.getEntityTypeName()));
	}

	private EntityTemplate toEntityTemplate(Document document, JsonEntityTemplateWrapper wrapper) {

		EntityTemplate entityTemplate = new EntityTemplate(toRootAnnotation(document, wrapper.getRootAnnotation()));

		for (Entry<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleSlotWrapper : wrapper.getSingleFillerSlots()
				.entrySet()) {
			entityTemplate.setSingleSlotFiller(toSlotType(singleSlotWrapper.getKey()),
					toSlotFiller(document, singleSlotWrapper.getValue()));
		}

		for (Entry<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiSlotWrapper : wrapper.getMultiFillerSlots()
				.entrySet()) {

			List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations = collectAnnotations(document,
					multiSlotWrapper.getValue().getDocLinkedAnnotations(),
					multiSlotWrapper.getValue().getLiteralAnnotations(),
					multiSlotWrapper.getValue().getEntityTypeAnnotations(),
					multiSlotWrapper.getValue().getEntityTemplateAnnotations());

			for (AbstractSlotFiller<? extends AbstractSlotFiller<?>> abstractSlotFiller : annotations) {
				entityTemplate.addMultiSlotFiller(toSlotType(multiSlotWrapper.getKey()), abstractSlotFiller);
			}

		}

		return entityTemplate;
	}

	private EntityTypeAnnotation toRootAnnotation(Document document, JsonRootAnnotationWrapper wrapper) {
		if (wrapper.getDocLinkedAnnotation() != null) {
			try {
				return toDocumentLinkedAnnotation(document, wrapper.getDocLinkedAnnotation());
			} catch (Exception e) {
				System.out.println("Can not map annotation to document: " + e.getMessage());
			}
		} else if (wrapper.getEntityTypeAnnotation() != null) {
			return toEntityTypeAnnotation(wrapper.getEntityTypeAnnotation());
		} else if (wrapper.getLiteralAnnotation() != null) {
			return toLiteralAnnotation(wrapper.getLiteralAnnotation());
		}
		throw new IllegalStateException("Root annotation has no value.");
	}

	private AbstractSlotFiller<? extends AbstractSlotFiller<?>> toSlotFiller(Document document,
			JsonSingleFillerSlotWrapper wrapper) {
		if (wrapper.getDocLinkedAnnotation() != null) {
			try {
				return toDocumentLinkedAnnotation(document, wrapper.getDocLinkedAnnotation());
			} catch (Exception e) {
				System.out.println("Can not map annotation to document: " + e.getMessage());
			}
		} else if (wrapper.getEntityTemplateAnnotation() != null) {
			return toEntityTemplate(document, wrapper.getEntityTemplateAnnotation());
		} else if (wrapper.getEntityTypeAnnotation() != null) {
			return toEntityTypeAnnotation(wrapper.getEntityTypeAnnotation());
		} else if (wrapper.getLiteralAnnotation() != null) {
			return toLiteralAnnotation(wrapper.getLiteralAnnotation());
		}
		return null;
	}

	private SlotType toSlotType(JsonSlotTypeWrapper wrapper) {
		return SlotType.get(wrapper.getSlotTypeName());
	}

}

package de.hterhors.semanticmr.corpus.json.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.corpus.json.wrapper.JsonAnnotationsWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentLinkedAnnotationWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentPositionWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentTokenWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonDocumentWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonEntityTemplateWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonEntityTypeWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonInstanceWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonLiteralAnnotationWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonMultiFillerSlotWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSingleFillerSlotWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSlotTypeWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonTextualContentWrapper;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializionHandler;
import de.hterhors.semanticmr.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.annotations.EntityType;
import de.hterhors.semanticmr.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class JsonToObjectConverter {

	private List<JsonInstanceWrapper> jsonInstances;

	public JsonToObjectConverter(List<JsonInstanceWrapper> jsonInstances) {
		this.jsonInstances = jsonInstances;
	}

	public List<Instance> convertToInstances(SystemInitializionHandler initializer) {
		return jsonInstances.stream().map(instanceWrapper -> toInstance(instanceWrapper)).collect(Collectors.toList());
	}

	private Instance toInstance(JsonInstanceWrapper instanceWrapper) {
		return new Instance(toDocument(instanceWrapper.getDocument()),
				toAnnotations(instanceWrapper.getGoldAnnotations()));
	}

	private Document toDocument(JsonDocumentWrapper document) {
		return new Document(document.getDocumentID(), toTokenList(document.getTokenList()));
	}

	private List<DocumentToken> toTokenList(List<JsonDocumentTokenWrapper> tokenListWrapper) {
		return tokenListWrapper.stream().map(w -> new DocumentToken(w.getSentenceIndex(), w.getSenTokenIndex(),
				w.getDocTokenIndex(), w.getSenCharOnset(), w.getDocCharOnset(), w.getText()))
				.collect(Collectors.toList());
	}

	private Annotations toAnnotations(JsonAnnotationsWrapper goldAnnotationsWrapper) {

		return new Annotations(collectAnnotations(goldAnnotationsWrapper.getDocLinkedAnnotations(),
				goldAnnotationsWrapper.getLiteralAnnotations(), goldAnnotationsWrapper.getEntityTypeAnnotations(),
				goldAnnotationsWrapper.getEntityTemplateAnnotations()));
	}

	private List<AbstractSlotFiller<?>> collectAnnotations(
			List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations,
			List<JsonLiteralAnnotationWrapper> literalAnnotations, List<JsonEntityTypeWrapper> entityTypeAnnotations,
			List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {

		final List<AbstractSlotFiller<?>> annotations = new ArrayList<>();

		if (docLinkedAnnotations != null)
			for (JsonDocumentLinkedAnnotationWrapper wrapper : docLinkedAnnotations) {
				annotations.add(toDocumentLinkedAnnotation(wrapper));
			}
		if (literalAnnotations != null)
			for (JsonLiteralAnnotationWrapper wrapper : literalAnnotations) {
				annotations.add(toLiteralAnnotation(wrapper));
			}
		if (entityTypeAnnotations != null)
			for (JsonEntityTypeWrapper wrapper : entityTypeAnnotations) {
				annotations.add(toEntityType(wrapper));
			}
		if (entityTemplateAnnotations != null)
			for (JsonEntityTemplateWrapper wrapper : entityTemplateAnnotations) {
				annotations.add(toEntityTemplate(wrapper));
			}
		return annotations;
	}

	private LiteralAnnotation toLiteralAnnotation(JsonLiteralAnnotationWrapper wrapper) {
		return new LiteralAnnotation(toEntityType(wrapper.getEntityType()),
				toTextualContent(wrapper.getTextualContent()));
	}

	private DocumentLinkedAnnotation toDocumentLinkedAnnotation(JsonDocumentLinkedAnnotationWrapper wrapper) {
		return new DocumentLinkedAnnotation(toEntityType(wrapper.getEntityType()),
				toTextualContent(wrapper.getTextualContent()), toDocumentPosition(wrapper.getDocumentPosition()));
	}

	private DocumentPosition toDocumentPosition(JsonDocumentPositionWrapper documentPosition) {
		return new DocumentPosition(documentPosition.getCharOffset());
	}

	private TextualContent toTextualContent(JsonTextualContentWrapper textualContent) {
		return new TextualContent(textualContent.getSurfaceForm());
	}

	private EntityType toEntityType(JsonEntityTypeWrapper wrapper) {
		return EntityType.get(wrapper.getEntityType());
	}

	private EntityTemplate toEntityTemplate(JsonEntityTemplateWrapper wrapper) {
		EntityTemplate entityTemplate = new EntityTemplate(toEntityType(wrapper.getEntityType()));

		for (Entry<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleSlotWrapper : wrapper.getSingleFillerSlots()
				.entrySet()) {
			entityTemplate.updateSingleFillerSlot(toSlotType(singleSlotWrapper.getKey()),
					toSlotFiller(singleSlotWrapper.getValue()));
		}

		for (Entry<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiSlotWrapper : wrapper.getMultiFillerSlots()
				.entrySet()) {

			List<AbstractSlotFiller<?>> annotations = collectAnnotations(
					multiSlotWrapper.getValue().getDocLinkedAnnotations(),
					multiSlotWrapper.getValue().getLiteralAnnotations(),
					multiSlotWrapper.getValue().getEntityTypeAnnotations(),
					multiSlotWrapper.getValue().getEntityTemplateAnnotations());

			for (AbstractSlotFiller<?> abstractSlotFiller : annotations) {
				entityTemplate.addToMultiFillerSlot(toSlotType(multiSlotWrapper.getKey()), abstractSlotFiller);
			}

		}

		return entityTemplate;
	}

	private AbstractSlotFiller<?> toSlotFiller(JsonSingleFillerSlotWrapper wrapper) {
		if (wrapper.getDocLinkedAnnotation() != null) {
			return toDocumentLinkedAnnotation(wrapper.getDocLinkedAnnotation());
		} else if (wrapper.getEntityTemplateAnnotation() != null) {
			return toEntityTemplate(wrapper.getEntityTemplateAnnotation());
		} else if (wrapper.getEntityTypeAnnotation() != null) {
			return toEntityType(wrapper.getEntityTypeAnnotation());
		} else if (wrapper.getLiteralAnnotation() != null) {
			return toLiteralAnnotation(wrapper.getLiteralAnnotation());
		}
		throw new IllegalStateException("Single filler slot has no value.");
	}

	private SlotType toSlotType(JsonSlotTypeWrapper wrapper) {
		return SlotType.get(wrapper.getSlotType());
	}

}

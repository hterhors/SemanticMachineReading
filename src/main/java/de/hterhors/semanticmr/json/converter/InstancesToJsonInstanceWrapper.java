package de.hterhors.semanticmr.json.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.hterhors.semanticmr.crf.structure.slots.MultiFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
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

public class InstancesToJsonInstanceWrapper {

	private List<Instance> instances;

	public InstancesToJsonInstanceWrapper(List<Instance> instances) {
		this.instances = instances;
	}

	public List<JsonInstanceWrapper> convertToWrapperInstances(SystemInitializer initializer) {
		return instances.stream().map(instance -> toInstanceWrapper(instance)).collect(Collectors.toList());
	}

	private JsonInstanceWrapper toInstanceWrapper(Instance instance) {
		return new JsonInstanceWrapper(instance.getOriginalContext(), toDocumentWrapper(instance.getDocument()),
				toAnnotationsWrapper(instance.getGoldAnnotations()));
	}

	private JsonDocumentWrapper toDocumentWrapper(Document document) {
		return new JsonDocumentWrapper(document.documentID, toTokenWrapperList(document.tokenList));
	}

	private List<JsonDocumentTokenWrapper> toTokenWrapperList(List<DocumentToken> tokenList) {
		return tokenList.stream().map(w -> new JsonDocumentTokenWrapper(w.sentenceIndex, w.senTokenIndex,
				w.docTokenIndex, w.senCharOffset, w.docCharOffset, w.text)).collect(Collectors.toList());
	}

	private JsonAnnotationsWrapper toAnnotationsWrapper(Annotations annotations) {
		return new JsonAnnotationsWrapper(extractDocLinkedAnnotationWrapper(annotations.getAnnotations()),
				extractLiteralAnnotationWrapper(annotations.getAnnotations()),
				extractEntityTypeWrapper(annotations.getAnnotations()),
				extractEntityTemplateWrapper(annotations.getAnnotations()));
	}

	private List<JsonEntityTemplateWrapper> extractEntityTemplateWrapper(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
		return annotations.stream().filter(a -> a.getClass() == EntityTemplate.class)
				.map(a -> toEntityTemplateWrapper((EntityTemplate) a)).collect(Collectors.toList());
	}

	private List<JsonEntityTypeWrapper> extractEntityTypeWrapper(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
		return annotations.stream().filter(a -> a.getClass() == EntityTypeAnnotation.class)
				.map(a -> toEntityTypeAnnotationWrapper((EntityTypeAnnotation) a)).collect(Collectors.toList());
	}

	private List<JsonLiteralAnnotationWrapper> extractLiteralAnnotationWrapper(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
		return annotations.stream().filter(a -> a.getClass() == LiteralAnnotation.class)
				.map(a -> toLiteralAnnotationWrapper((LiteralAnnotation) a)).collect(Collectors.toList());
	}

	private List<JsonDocumentLinkedAnnotationWrapper> extractDocLinkedAnnotationWrapper(
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
		return annotations.stream().filter(a -> a.getClass() == DocumentLinkedAnnotation.class)
				.map(a -> toDocumentLinkedAnnotationWrapper((DocumentLinkedAnnotation) a)).collect(Collectors.toList());
	}

	private JsonLiteralAnnotationWrapper toLiteralAnnotationWrapper(LiteralAnnotation wrapper) {
		return new JsonLiteralAnnotationWrapper(toEntityTypeAnnotationWrapper(wrapper.entityType),
				toTextualContentWrapper(wrapper.textualContent));
	}

	private JsonDocumentLinkedAnnotationWrapper toDocumentLinkedAnnotationWrapper(
			DocumentLinkedAnnotation docLinkedAnnotation) {
		return new JsonDocumentLinkedAnnotationWrapper(
				toEntityTypeAnnotationWrapper(docLinkedAnnotation.getEntityType()),
				toTextualContentWrapper(docLinkedAnnotation.textualContent),
				toDocumentPositionWrapper(docLinkedAnnotation.documentPosition));
	}

	private JsonDocumentPositionWrapper toDocumentPositionWrapper(DocumentPosition documentPosition) {
		return new JsonDocumentPositionWrapper(documentPosition.charOffset);
	}

	private JsonTextualContentWrapper toTextualContentWrapper(TextualContent textualContent) {
		return new JsonTextualContentWrapper(textualContent.surfaceForm);
	}

	private JsonEntityTypeWrapper toEntityTypeAnnotationWrapper(EntityTypeAnnotation entityType) {
		return new JsonEntityTypeWrapper(entityType.entityType.entityTypeName);
	}

	private JsonEntityTypeWrapper toEntityTypeAnnotationWrapper(EntityType entityType) {
		return new JsonEntityTypeWrapper(entityType.entityTypeName);
	}

	private JsonEntityTemplateWrapper toEntityTemplateWrapper(EntityTemplate wrapper) {

		final Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots = new HashMap<>();
		final Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots = new HashMap<>();

		for (Entry<SlotType, SingleFillerSlot> singleSlotWrapper : wrapper.getSingleFillerSlots().entrySet()) {
			singleFillerSlots.put(toSlotTypeWrapper(singleSlotWrapper.getKey()),
					toSingleSlotFillerWrapper(singleSlotWrapper.getValue()));
		}

		for (Entry<SlotType, MultiFillerSlot> multiSlot : wrapper.getMultiFillerSlots().entrySet()) {
			multiFillerSlots.put(toSlotTypeWrapper(multiSlot.getKey()),
					new JsonMultiFillerSlotWrapper(
							extractDocLinkedAnnotationWrapper(multiSlot.getValue().getSlotFiller()),
							extractLiteralAnnotationWrapper(multiSlot.getValue().getSlotFiller()),
							extractEntityTypeWrapper(multiSlot.getValue().getSlotFiller()),
							extractEntityTemplateWrapper(multiSlot.getValue().getSlotFiller())));
		}

		return new JsonEntityTemplateWrapper(toRootAnnotationWrapper(wrapper.getRootAnnotation()), singleFillerSlots,
				multiFillerSlots);
	}

	private JsonRootAnnotationWrapper toRootAnnotationWrapper(EntityTypeAnnotation wrapper) {
		final JsonRootAnnotationWrapper rootAnnotationWrapper = new JsonRootAnnotationWrapper(null, null, null);
		if (wrapper instanceof DocumentLinkedAnnotation) {
			rootAnnotationWrapper
					.setDocLinkedAnnotation(toDocumentLinkedAnnotationWrapper((DocumentLinkedAnnotation) wrapper));
		} else if (wrapper instanceof EntityTypeAnnotation) {
			rootAnnotationWrapper
					.setEntityTypeAnnotation(toEntityTypeAnnotationWrapper((EntityTypeAnnotation) wrapper));
		} else if (wrapper instanceof LiteralAnnotation) {
			rootAnnotationWrapper.setLiteralAnnotation(toLiteralAnnotationWrapper((LiteralAnnotation) wrapper));
		}
		return rootAnnotationWrapper;
	}

	private JsonSingleFillerSlotWrapper toSingleSlotFillerWrapper(SingleFillerSlot singleFillerSlot) {
		final JsonSingleFillerSlotWrapper singleFillerSlotWrapper = new JsonSingleFillerSlotWrapper(null, null, null,
				null);

		final AbstractSlotFiller<?> slotFiller = singleFillerSlot.getSlotFiller();

		if (slotFiller instanceof DocumentLinkedAnnotation) {
			singleFillerSlotWrapper
					.setDocLinkedAnnotation(toDocumentLinkedAnnotationWrapper((DocumentLinkedAnnotation) slotFiller));
		} else if (slotFiller instanceof EntityTypeAnnotation) {
			singleFillerSlotWrapper
					.setEntityTypeAnnotation(toEntityTypeAnnotationWrapper((EntityTypeAnnotation) slotFiller));
		} else if (slotFiller instanceof LiteralAnnotation) {
			singleFillerSlotWrapper.setLiteralAnnotation(toLiteralAnnotationWrapper((LiteralAnnotation) slotFiller));
		} else if (slotFiller instanceof EntityTemplate) {
			singleFillerSlotWrapper.setEntityTemplateAnnotation(toEntityTemplateWrapper((EntityTemplate) slotFiller));
		}

		return singleFillerSlotWrapper;
	}

	private JsonSlotTypeWrapper toSlotTypeWrapper(SlotType wrapper) {
		return new JsonSlotTypeWrapper(wrapper.slotTypeName);
	}

}

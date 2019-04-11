package de.hterhors.semanticmr.corpus.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

public class JsonWriter {

	public static void main(String[] args) {

		List<JsonInstanceWrapper> toJsoninstances = new ArrayList<>();

		List<JsonDocumentTokenWrapper> tokenList = new ArrayList<>();
		tokenList.add(new JsonDocumentTokenWrapper(0, 0, 0, 0, 0, "Hallo"));
		tokenList.add(new JsonDocumentTokenWrapper(1, 1, 1, 1, 1, "World"));

		List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations = new ArrayList<>();
		List<JsonLiteralAnnotationWrapper> literalAnnotations = new ArrayList<>();
		List<JsonEntityTypeWrapper> enittyTypeAnnotations = new ArrayList<>();
		List<JsonEntityTemplateWrapper> entityTemplateAnnotations = new ArrayList<>();

		docLinkedAnnotations.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Male"),
				new JsonTextualContentWrapper("male"), new JsonDocumentPositionWrapper(0)));

		literalAnnotations.add(new JsonLiteralAnnotationWrapper(new JsonEntityTypeWrapper("Female"),
				new JsonTextualContentWrapper("female")));

		enittyTypeAnnotations.add(new JsonEntityTypeWrapper("Mixed"));

		Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> subSingleFillerSlots = new HashMap<>();

		subSingleFillerSlots.put(new JsonSlotTypeWrapper("hasWeight"),
				new JsonSingleFillerSlotWrapper(
						new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Weight"),
								new JsonTextualContentWrapper("250 g"), new JsonDocumentPositionWrapper(350))));

		Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> subMultiFillerSlots = new HashMap<>();

		List<JsonDocumentLinkedAnnotationWrapper> subSlotFiller = new ArrayList<>();
		subSlotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("Sub animal mention 1"), new JsonDocumentPositionWrapper(1000)));
		subSlotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("Sub animal mention 2"), new JsonDocumentPositionWrapper(2000)));

		subMultiFillerSlots.put(new JsonSlotTypeWrapper("hasMentions"),
				new JsonMultiFillerSlotWrapper(subSlotFiller, null, null, null));

		Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots = new HashMap<>();

		singleFillerSlots.put(new JsonSlotTypeWrapper("hasAgeCategory"),
				new JsonSingleFillerSlotWrapper(new JsonEntityTypeWrapper("Adult")));

		singleFillerSlots.put(new JsonSlotTypeWrapper("hasSubAnimal"),
				new JsonSingleFillerSlotWrapper(new JsonEntityTemplateWrapper(new JsonEntityTypeWrapper("MouseModel"),
						subSingleFillerSlots, subMultiFillerSlots)));

		Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots = new HashMap<>();

		List<JsonDocumentLinkedAnnotationWrapper> slotFiller = new ArrayList<>();
		slotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("The First Mention"), new JsonDocumentPositionWrapper(100)));
		slotFiller.add(new JsonDocumentLinkedAnnotationWrapper(new JsonEntityTypeWrapper("Mention"),
				new JsonTextualContentWrapper("The Second Mention"), new JsonDocumentPositionWrapper(200)));

		multiFillerSlots.put(new JsonSlotTypeWrapper("hasMentions"),
				new JsonMultiFillerSlotWrapper(slotFiller, null, null, null));

		entityTemplateAnnotations.add(new JsonEntityTemplateWrapper(new JsonEntityTypeWrapper("RatModel"),
				singleFillerSlots, multiFillerSlots));

		JsonAnnotationsWrapper goldAnnotations = new JsonAnnotationsWrapper(docLinkedAnnotations, literalAnnotations,
				enittyTypeAnnotations, entityTemplateAnnotations);

		toJsoninstances
				.add(new JsonInstanceWrapper(new JsonDocumentWrapper("Hello World", tokenList), goldAnnotations));

//		GsonBuilder builder = new GsonBuilder();
//		builder.disableHtmlEscaping();
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
		Type type = new TypeToken<List<JsonInstanceWrapper>>() {
		}.getType();

//		Gson gson = new Gson();//.create();
		String json = gson.toJson(toJsoninstances, type);

		System.out.println(json);
		List<JsonInstanceWrapper> fromJsonInstances = gson.fromJson(json, type);
		System.out.println(fromJsonInstances.equals(toJsoninstances));
		System.out.println(toJsoninstances);
		System.out.println(fromJsonInstances);

	}

}

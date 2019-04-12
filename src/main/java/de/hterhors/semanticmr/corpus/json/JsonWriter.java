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
import de.hterhors.semanticmr.corpus.json.wrapper.JsonRootAnnotationWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSingleFillerSlotWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonSlotTypeWrapper;
import de.hterhors.semanticmr.corpus.json.wrapper.JsonTextualContentWrapper;

public class JsonWriter {

	final private Gson gson;
	final private Type type;

	public JsonWriter() {
		this.gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
		this.type = new TypeToken<List<JsonInstanceWrapper>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		}.getType();

	}

	public String writeInstances(List<JsonInstanceWrapper> instances) {
		return gson.toJson(instances, type);
	}
}

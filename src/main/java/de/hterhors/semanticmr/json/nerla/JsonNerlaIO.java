package de.hterhors.semanticmr.json.nerla;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.jena.ext.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;

public class JsonNerlaIO {

	final private Gson gson;
	final private Type type;

	public JsonNerlaIO(boolean toPrettyString) {
		GsonBuilder builder = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping();

		if (toPrettyString)
			builder.setPrettyPrinting();

		this.gson = builder.create();
		this.type = new TypeToken<List<JsonEntityAnnotationWrapper>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		}.getType();

	}

	public List<JsonEntityAnnotationWrapper> fromJsonString(String json) {
		return gson.fromJson(json, type);
	}

	public String toJsonString(List<JsonEntityAnnotationWrapper> instances) {
		return gson.toJson(instances, type);
	}
}

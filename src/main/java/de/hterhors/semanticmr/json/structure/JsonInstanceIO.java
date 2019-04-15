package de.hterhors.semanticmr.json.structure;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.jena.ext.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.hterhors.semanticmr.json.structure.wrapper.JsonInstanceWrapper;

public class JsonInstanceIO {

	final private Gson gson;
	final private Type type;

	public JsonInstanceIO(boolean toPrettyString) {

		GsonBuilder builder = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping();

		if (toPrettyString)
			builder.setPrettyPrinting();

		this.gson = builder.create();
		this.type = new TypeToken<List<JsonInstanceWrapper>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		}.getType();

	}

	public List<JsonInstanceWrapper> readInstances(String json) {
		return gson.fromJson(json, type);
	}

	public String writeInstances(List<JsonInstanceWrapper> instances) {
		return gson.toJson(instances, type);
	}
}

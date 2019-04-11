package de.hterhors.semanticmr.corpus.json;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.jena.ext.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.hterhors.semanticmr.corpus.json.wrapper.JsonInstanceWrapper;

public class JsonReader {

	final private Gson gson;
	final private Type type;

	public JsonReader() {
		this.gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
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
}

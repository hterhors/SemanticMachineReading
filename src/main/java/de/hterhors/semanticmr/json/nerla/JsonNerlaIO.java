package de.hterhors.semanticmr.json.nerla;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Arrays;
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

//	public String writeNerlas(List<JsonEntityAnnotationWrapper> instances) {
//		return gson.toJson(instances, type);
//	}

	public void writeNerlas(final File outputFile, List<JsonEntityAnnotationWrapper> instances) throws IOException {
		PrintStream ps = new PrintStream(outputFile);
		ps.println(gson.toJson(instances, type));
		ps.close();
	}

	public void writeNerla(final File outputFile, JsonEntityAnnotationWrapper instance) throws IOException {
		PrintStream ps = new PrintStream(outputFile);
		ps.println(gson.toJson(Arrays.asList(instance), type));
		ps.close();
	}

}

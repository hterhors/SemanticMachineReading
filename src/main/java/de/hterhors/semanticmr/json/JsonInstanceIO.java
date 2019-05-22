package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.ext.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.hterhors.semanticmr.json.wrapper.JsonInstanceWrapper;

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

	public List<JsonInstanceWrapper> readInstances(final File inputFile) throws JsonSyntaxException, IOException {

		return gson.fromJson(new String(Files.readAllBytes(inputFile.toPath())), type);
	}

	public void writeInstances(final File outputFile, List<JsonInstanceWrapper> instances) throws IOException {
		PrintStream ps = new PrintStream(outputFile);
		ps.println(gson.toJson(instances, type));
		ps.close();
	}

	public void writeInstance(final File outputFile, JsonInstanceWrapper instance) throws IOException {
		PrintStream ps = new PrintStream(outputFile);
		ps.println(gson.toJson(Arrays.asList(instance), type));
		ps.close();
	}
}

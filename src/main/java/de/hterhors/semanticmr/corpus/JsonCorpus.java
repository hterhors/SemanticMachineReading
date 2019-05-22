package de.hterhors.semanticmr.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.json.wrapper.JsonInstanceWrapper;

public class JsonCorpus {

	private static final String JSON_FILE_ENDING = ".json";
	final List<Instance> instances = new ArrayList<>();

	public void addInstance(Instance instance) {
		instances.add(instance);
	}

	public void write(File corpusDir, boolean prettyString) throws IOException {

		InstancesToJsonInstanceWrapper wrapper = new InstancesToJsonInstanceWrapper(instances);
		List<JsonInstanceWrapper> wrappedInstances = wrapper.convertToWrapperInstances();

		JsonInstanceIO io = new JsonInstanceIO(prettyString);

		for (JsonInstanceWrapper jsonInstanceWrapper : wrappedInstances) {
			io.writeInstance(new File(corpusDir, jsonInstanceWrapper.getDocument().getDocumentID() + JSON_FILE_ENDING),
					jsonInstanceWrapper);
		}

	}

}

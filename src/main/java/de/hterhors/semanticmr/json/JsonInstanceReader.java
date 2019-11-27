package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.Instance.ModifyGoldRule;
import de.hterhors.semanticmr.json.converter.JsonInstanceWrapperToInstance;
import de.hterhors.semanticmr.json.wrapper.JsonInstanceWrapper;

public class JsonInstanceReader {
	private static Logger log = LogManager.getFormatterLogger(JsonInstanceReader.class);

	private final File corpusDirectory;
	
final private 	Collection<ModifyGoldRule> modifyGoldRules;
	public JsonInstanceReader(final File corpusDirectory, Collection<ModifyGoldRule> modifyGoldRules) {
		this.corpusDirectory = corpusDirectory;
		this.modifyGoldRules = modifyGoldRules;
	}

	public List<Instance> readInstances(final int numToRead) throws IOException {
		log.info("Read corpus instances from the file system...");

		if (numToRead != Integer.MAX_VALUE) {
			log.info("Limit instances to: " + numToRead);
		}

		List<File> jsonFiles = Arrays.stream(corpusDirectory.listFiles()).filter(f -> f.getName().endsWith(".json"))
				.collect(Collectors.toList());

		Collections.sort(jsonFiles);

		final List<Instance> trainingInstances = new ArrayList<>();

		int count = 0;
		for (File jsonFile : jsonFiles) {

			if (count == numToRead)
				break;

			count++;
			if (count % 100 == 0) {
				log.debug(" - " + count + " - ");
			}
			List<JsonInstanceWrapper> jsonInstances = new JsonInstanceIO(true).readInstances(jsonFile);
			trainingInstances.addAll(new JsonInstanceWrapperToInstance(jsonInstances).convertToInstances(modifyGoldRules));
		}
		log.info("Read instances... done");
		return trainingInstances;

	}

}

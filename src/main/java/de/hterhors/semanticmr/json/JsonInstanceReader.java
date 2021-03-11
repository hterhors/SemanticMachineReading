package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.Instance.DeduplicationRule;
import de.hterhors.semanticmr.crf.variables.Instance.GoldModificationRule;
import de.hterhors.semanticmr.json.converter.JsonInstanceWrapperToInstance;
import de.hterhors.semanticmr.json.wrapper.JsonInstanceWrapper;

public class JsonInstanceReader {
	private static Logger log = LogManager.getFormatterLogger(JsonInstanceReader.class);

	private final File corpusDirectory;

	final private Collection<GoldModificationRule> modifyGoldRules;
	final private DeduplicationRule duplicationRule;

	public JsonInstanceReader(final File corpusDirectory, Collection<GoldModificationRule> modifyGoldRules,
			DeduplicationRule duplicationRule) {
		this.corpusDirectory = corpusDirectory;
		this.modifyGoldRules = modifyGoldRules;
		this.duplicationRule = duplicationRule;
	}

	public List<Instance> readInstances(final int numToRead, final Set<String> fileNamesToRead) throws IOException {

		log.info("Read corpus instances from the file system...");

		if (numToRead != Integer.MAX_VALUE) {
			log.info("Limit instances to: " + numToRead);
		}

		List<File> jsonFiles = Arrays.stream(corpusDirectory.listFiles()).filter(f -> f.getName().endsWith(".json"))
				.filter(f -> beginsWith(fileNamesToRead, f.getName()) || fileNamesToRead.contains(f.getName())
						|| fileNamesToRead.isEmpty())
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
			trainingInstances.addAll(new JsonInstanceWrapperToInstance(jsonInstances)
					.convertToInstances(modifyGoldRules, duplicationRule));
		}

		for (Iterator<Instance> iterator = trainingInstances.iterator(); iterator.hasNext();) {
			Instance instance = (Instance) iterator.next();
			if (instance.getDocument().documentContent.isEmpty())
				iterator.remove();
		}

		log.info("Read instances... done");
		return trainingInstances;
	}

	private boolean beginsWith(Set<String> fileNamesToRead, String name) {
		for (String string : fileNamesToRead) {
			if (name.startsWith(string))
				return true;
		}

		return false;
	}

	public List<Instance> readInstances(final int numToRead) throws IOException {
		return readInstances(numToRead, Collections.emptySet());
	}

}

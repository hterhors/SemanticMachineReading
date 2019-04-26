package de.hterhors.semanticmr.json;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.json.converter.JsonInstanceWrapperToInstance;
import de.hterhors.semanticmr.json.structure.wrapper.JsonInstanceWrapper;

public class JsonInstancesReader {

	private final File corpusDirectory;

	public JsonInstancesReader(final File corpusDirectory) {
		this.corpusDirectory = corpusDirectory;
	}

	public List<Instance> readInstances(final int numToRead) throws IOException {
		System.out.println("#######################LOAD INSTANCES#######################");

		System.out.print("Read instances");

		List<File> jsonFiles = Arrays.stream(corpusDirectory.listFiles()).filter(f -> f.getName().endsWith(".json"))
				.collect(Collectors.toList());

		Collections.sort(jsonFiles);

		final List<Instance> trainingInstances = new ArrayList<>();

		int count = 0;
		for (File jsonFile : jsonFiles) {

			if (count == numToRead)
				break;

			count++;
			if (count % 10 == 0)
				System.out.print(".");
			if (count % 100 == 0) {
				System.out.print(" - " + count + " - ");
			}
			List<JsonInstanceWrapper> jsonInstances = new JsonInstanceIO(true)
					.readInstances(new String(Files.readAllBytes(jsonFile.toPath())));

			trainingInstances.addAll(new JsonInstanceWrapperToInstance(jsonInstances).convertToInstances());
		}
		System.out.println("... done");
		System.out.println("Total number of instances loaded: " + count);

		return trainingInstances;

	}

}

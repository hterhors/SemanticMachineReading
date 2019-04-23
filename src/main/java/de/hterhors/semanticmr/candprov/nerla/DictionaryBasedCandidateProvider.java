package de.hterhors.semanticmr.candprov.nerla;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

public class DictionaryBasedCandidateProvider implements INerlaCandidateProvider {

	final private Map<EntityType, Set<String>> dictionary = new HashMap<>();

	final private Map<String, Set<EntityType>> reverseDictionary = new HashMap<>();

	public DictionaryBasedCandidateProvider(final File dictionaryFile) throws IOException {

		for (String dictLine : Files.readAllLines(dictionaryFile.toPath())) {

			final String data[] = dictLine.split("\t", 2);

			for (String entry : data[1].split("\\|")) {

				dictionary.putIfAbsent(EntityType.get(data[0]), new HashSet<>());
				dictionary.get(EntityType.get(data[0])).add(entry);
				reverseDictionary.putIfAbsent(entry, new HashSet<>());
				reverseDictionary.get(entry).add(EntityType.get(data[0]));
			}

		}
	}

	@Override
	public Set<EntityType> getEntityTypeCandidates(String text) {
		return reverseDictionary.getOrDefault(text, Collections.emptySet());
	}
}

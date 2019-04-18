package de.hterhors.semanticmr.candprov.nerla;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

public class DictionaryBasedCandidateProvider implements INERLACandidateProvider {

	final private Map<EntityType, Set<String>> dictionary = new HashMap<>();

	public DictionaryBasedCandidateProvider(final File dictionaryFile) throws IOException {

		for (String dictLine : Files.readAllLines(dictionaryFile.toPath())) {

			final String data[] = dictLine.split("\t", 2);

			for (String entry : data[1].split("|")) {

				dictionary.putIfAbsent(EntityType.get(data[0]), new HashSet<>());
				dictionary.get(EntityType.get(data[0])).add(entry);
			}

		}

	}

	@Override
	public List<EntityType> getEntityTypeCandidates(String text) {
		List<EntityType> types = new ArrayList<>();
		for (Entry<EntityType, Set<String>> dictEntry : dictionary.entrySet()) {
			if (dictEntry.getValue().contains(text)) {
				types.add(dictEntry.getKey());
			}
		}
		return types;
	}
}

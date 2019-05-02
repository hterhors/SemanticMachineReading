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

/**
 * The in memory dictionary based candidate provider is the simplest form of
 * providing entity-candidates given free text.
 * 
 * Given an input text, we do a exact string match to entries of the dictionary
 * and return all entity-types to that one or multiple matches exists.
 * 
 * @author hterhors
 *
 */
public class InMEMDictionaryBasedCandidateProvider implements INerlaCandidateProvider {

	/**
	 * The dictionary.
	 */
	final private Map<EntityType, Set<String>> dictionary = new HashMap<>();

	/**
	 * The reversed dictionary for fast look up.
	 */
	final private Map<String, Set<EntityType>> reverseDictionary = new HashMap<>();

	/**
	 * The in memory dictionary based candidate provider is the simplest form of
	 * providing entity-candidates given free text.
	 * 
	 * Given an input text, we do a exact string match to entries of the dictionary
	 * and return all entity-types to that one or multiple matches exists.
	 * 
	 * @param dictionaryFile the dictionary file.
	 * @throws IOException
	 */
	public InMEMDictionaryBasedCandidateProvider(final File dictionaryFile) throws IOException {

		/**
		 * TODO: check file contents format.
		 */
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

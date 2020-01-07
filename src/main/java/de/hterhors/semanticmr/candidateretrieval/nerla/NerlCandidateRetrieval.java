package de.hterhors.semanticmr.candidateretrieval.nerla;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
public class NerlCandidateRetrieval {

	private Set<EntityType> types;

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

	public NerlCandidateRetrieval() {
	}

	/**
	 * TODO: inefficient
	 * 
	 * @param dictionaryFile
	 */
	public void addCandidates(final File dictionaryFile) {
		try {
			for (String dictLine : Files.readAllLines(dictionaryFile.toPath())) {

				final String data[] = dictLine.split("\t", 2);

				for (String entry : data[1].split("\\|")) {

					this.dictionary.putIfAbsent(EntityType.get(data[0]), new HashSet<>());
					this.dictionary.get(EntityType.get(data[0])).add(entry);
					this.reverseDictionary.putIfAbsent(entry, new HashSet<>());
					this.reverseDictionary.get(entry).add(EntityType.get(data[0]));
				}

			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void addCandidates(Map<EntityType, Set<String>> dictionary) {
		for (Entry<EntityType, Set<String>> entityType : dictionary.entrySet()) {
			addCandidate(entityType.getKey(), entityType.getValue());
		}
	}

	public void addCandidate(EntityType entityType, Set<String> words) {

		this.dictionary.putIfAbsent(entityType, new HashSet<>());
		this.dictionary.get(entityType).addAll(words);

		for (String s : words) {
			this.reverseDictionary.putIfAbsent(s, new HashSet<>());
			this.reverseDictionary.get(s).add(entityType);
		}

	}

	public void addCandidate(EntityType entityType) {
		this.types.add(entityType);
	}

	public void addCandidates(Set<EntityType> entityTypes) {
		this.types.addAll(entityTypes);
	}

	/**
	 * TODO: Inefficient
	 * 
	 * @param text
	 * @return
	 */
	public Set<EntityType> getEntityTypeCandidates(String text) {
		if (types.isEmpty())
			return reverseDictionary.getOrDefault(text, Collections.emptySet());
		else {
			final Set<EntityType> types = new HashSet<>();
			types.addAll(types);
			types.addAll(reverseDictionary.getOrDefault(text, Collections.emptySet()));
			return types;
		}
	}
}

package de.hterhors.semanticmr.init.reader.csv;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;
import de.hterhors.semanticmr.init.specifications.Specifications;

public class CSVDataStructureReader implements ISpecificationsReader {
	private static Logger log = LogManager.getFormatterLogger(CSVDataStructureReader.class);

	private final File entitiesFile;
	private final File hierarchiesFile;
	private final File slotsFile;
	private final File structuresFile;

	public CSVDataStructureReader(File entitiesFile) {
		this.entitiesFile = entitiesFile;
		this.hierarchiesFile = null;
		this.slotsFile = null;
		this.structuresFile = null;
	}

	public CSVDataStructureReader(File entitiesFile, File hierarchiesFile, File slotsFile, File structuresFile) {
		this.entitiesFile = entitiesFile;
		this.hierarchiesFile = hierarchiesFile;
		this.slotsFile = slotsFile;
		this.structuresFile = structuresFile;
	}

	@Override
	public Specifications read() throws InvalidSpecificationFileFormatException {
		try {

			List<String[]> entities = Files.readAllLines(entitiesFile.toPath()).stream().filter(l -> !l.startsWith("#"))
					.filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t")).collect(Collectors.toList());
			List<String[]> hierarchies = hierarchiesFile == null ? Collections.emptyList()
					: Files.readAllLines(hierarchiesFile.toPath()).stream().filter(l -> !l.startsWith("#"))
							.filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t")).collect(Collectors.toList());
			List<String[]> slots = slotsFile == null ? Collections.emptyList()
					: Files.readAllLines(slotsFile.toPath()).stream().filter(l -> !l.startsWith("#"))
							.filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t")).collect(Collectors.toList());
			List<String[]> structures = structuresFile == null ? Collections.emptyList()
					: Files.readAllLines(structuresFile.toPath()).stream().filter(l -> !l.startsWith("#"))
							.filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t")).collect(Collectors.toList());

			Set<String> slotTypeNames = slots.stream().map(l -> l[0]).collect(Collectors.toSet());
			Set<String> entityTypeNames = entities.stream().map(l -> l[0]).collect(Collectors.toSet());
			/*
			 * super-entity, set of sub-entities
			 */
			Map<String, Set<String>> subEntityTypes = new HashMap<>();

			/*
			 * sub-entities, set of super-entity
			 */
			Map<String, Set<String>> superEntityTypes = new HashMap<>();

			for (String[] line : hierarchies) {
				subEntityTypes.putIfAbsent(line[0], new HashSet<>());
				subEntityTypes.get(line[0]).add(line[1]);
				superEntityTypes.putIfAbsent(line[1], new HashSet<>());
				superEntityTypes.get(line[1]).add(line[0]);
			}

			/*
			 * parent-entity, set of slots
			 */
			Map<String, Set<String>> slotsForEntity = new HashMap<>();

			/*
			 * slot, set of child-entities
			 */
			Map<String, Set<String>> slotFillerEntityTypes = new HashMap<>();

			for (String[] line : structures) {
				try {

					slotsForEntity.putIfAbsent(line[0], new HashSet<>());
					slotsForEntity.get(line[0]).add(line[1]);
					slotFillerEntityTypes.putIfAbsent(line[1], new HashSet<>());
					slotFillerEntityTypes.get(line[1]).add(line[2]);
				} catch (Exception e) {
					System.out.println(Arrays.toString(line));
					e.printStackTrace();
				}
			}

			Set<String> sa = new HashSet<>();
			for (String[] string : entities) {
				if (sa.contains(string[0]))
					System.out.println("WARN: " + string[0]);
				sa.add(string[0]);

			}

			Set<String> sa2 = new HashSet<>();
			for (String[] string : slots) {
				if (sa2.contains(string[0]))
					System.out.println("WARN: " + string[0]);
				sa2.add(string[0]);

			}

			Map<String, Boolean> isLiteralValueSlotTypes = entities.stream()
					.collect(Collectors.toMap(s -> s[0], s -> new Boolean(s[1].equals("true"))));

			Map<String, Integer> slotMaxSize = new HashMap<>();
			for (String[] s : slots) {
				int value = 1;
				if (s[1].isEmpty()) {
					value = Integer.MAX_VALUE;
				}
				if (Integer.valueOf(s[1]).intValue() <= 0) {
					value = Integer.MAX_VALUE;
				}

				if (!slotMaxSize.containsKey(s[0]) || !new Integer(Integer.MAX_VALUE).equals(slotMaxSize.get(s[0])))
					slotMaxSize.put(s[0], value);

			}

			return new Specifications(entityTypeNames, slotTypeNames, isLiteralValueSlotTypes, slotFillerEntityTypes,
					superEntityTypes, subEntityTypes, slotsForEntity, slotMaxSize);

		} catch (Exception e) {
			throw new InvalidSpecificationFileFormatException(e);
		}
	}

}

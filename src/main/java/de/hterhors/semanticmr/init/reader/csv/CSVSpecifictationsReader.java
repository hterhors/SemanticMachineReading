package de.hterhors.semanticmr.init.reader.csv;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;
import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;

public class CSVSpecifictationsReader implements ISpecificationsReader {

	private final File entitySpecificationFile;
	private final File entityStructureSpecificationFile;
	private final File slotSpecificationFile;
//	private final File slotPairConstriantsSpecificationFile;

	public CSVSpecifictationsReader(File entitySpecificationFile, File entityStructureSpecificationFile,
			File slotSpecificationFile
//			, File slotPairConstriantsSpecificationFile
	) {
		this.entitySpecificationFile = entitySpecificationFile;
		this.entityStructureSpecificationFile = entityStructureSpecificationFile;
		this.slotSpecificationFile = slotSpecificationFile;
//		this.slotPairConstriantsSpecificationFile = slotPairConstriantsSpecificationFile;
	}

	public StructureSpecification read() throws InvalidSpecificationFileFormatException {
		try {
			List<String[]> entities = Files.readAllLines(entitySpecificationFile.toPath()).stream()
					.filter(l -> !l.startsWith("#")).filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t"))
					.collect(Collectors.toList());
			List<String[]> entityStructure = Files.readAllLines(entityStructureSpecificationFile.toPath()).stream()
					.filter(l -> !l.startsWith("#")).filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t"))
					.collect(Collectors.toList());
			List<String[]> slots = Files.readAllLines(slotSpecificationFile.toPath()).stream()
					.filter(l -> !l.startsWith("#")).filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t"))
					.collect(Collectors.toList());

//			List<String[]> slotPairConstriants = Files.readAllLines(slotPairConstriantsSpecificationFile.toPath())
//					.stream().filter(l -> !l.startsWith("#")).filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t"))
//					.collect(Collectors.toList());

			Set<String> slotTypeNames = slots.stream().map(l -> l[1]).collect(Collectors.toSet());
			Set<String> entityTypeNames = entities.stream().map(l -> l[0]).collect(Collectors.toSet());

			Map<String, Set<String>> subEntityTypes = new HashMap<>();
			Map<String, Set<String>> superEntityTypes = new HashMap<>();

			for (String[] line : entityStructure) {
				subEntityTypes.putIfAbsent(line[0], new HashSet<>());
				subEntityTypes.get(line[0]).add(line[1]);
				superEntityTypes.putIfAbsent(line[1], new HashSet<>());
				superEntityTypes.get(line[1]).add(line[0]);
			}

			Map<String, Set<String>> slotsForEntity = new HashMap<>();
			Map<String, Set<String>> slotFillerEntityTypes = new HashMap<>();

			for (String[] line : slots) {
				slotsForEntity.putIfAbsent(line[0], new HashSet<>());
				slotsForEntity.get(line[0]).add(line[1]);
				slotFillerEntityTypes.putIfAbsent(line[1], new HashSet<>());
				slotFillerEntityTypes.get(line[1]).add(line[2]);
			}

			Map<String, Boolean> isLiteralValueSlotTypes = entities.stream()
					.collect(Collectors.toMap(s -> s[0], s -> new Boolean(s[1].equals("true"))));

			Map<String, Boolean> isSingleValueSlotTypes = slots.stream()
					.collect(Collectors.toMap(s -> s[1], s -> new Boolean(s[3].equals("Single"))));

			Map<String, Integer> multiAnnotationSlotMaxSize = slots.stream().filter(s -> s[3].equals("Multi"))
					.collect(Collectors.toMap(s -> s[1], s -> {
						if (s[4].isEmpty()) {
							return Integer.MAX_VALUE;
						}
						final Integer max = Integer.valueOf(s[4]);
						if (max.intValue() <= 0) {
							return Integer.MAX_VALUE;
						}
						return max;
					}));

//			Set<ExcludeSlotTypePairNames> excludeSlotTypePairs = slotPairConstriants.stream()
//					.map(l -> new ExcludeSlotTypePairNames(l[0], l[1], l[2], l[3], l[4])).collect(Collectors.toSet());

			return new StructureSpecification(entityTypeNames, slotTypeNames, isSingleValueSlotTypes,
					isLiteralValueSlotTypes, slotFillerEntityTypes, superEntityTypes, subEntityTypes, slotsForEntity,
					multiAnnotationSlotMaxSize
//					, excludeSlotTypePairs
			);

		} catch (Exception e) {
			throw new InvalidSpecificationFileFormatException(e);
		}
	}

}

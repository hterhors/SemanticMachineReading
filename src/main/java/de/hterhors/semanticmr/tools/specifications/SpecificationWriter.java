package de.hterhors.semanticmr.tools.specifications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.init.specifications.SystemScope;

public class SpecificationWriter {
	SystemScope scope;

	public SpecificationWriter(SystemScope scope) {
		this.scope = scope;
	}

	public void writeEntitySpecificationFile(File file, EntityType rootEntity) throws IOException {

		Set<EntityType> relatedEntities = new HashSet<>();
		addRecursive(rootEntity, relatedEntities);
		PrintStream ps = new PrintStream(file);
		ps.println("#class\tisLiteral");
		for (EntityType entityType : relatedEntities) {
			ps.println(entityType.name + "\t" + entityType.isLiteral);
		}

		ps.close();

	}

	public void writeHierarchiesSpecificationFile(File file, EntityType rootEntity) throws FileNotFoundException {

		Set<EntityType> relatedEntities = new HashSet<>();
		addRecursive(rootEntity, relatedEntities);
		PrintStream ps = new PrintStream(file);
		ps.println("#superClass\tsubClass");
		for (EntityType subET : relatedEntities) {
			for (EntityType superET : subET.getDirectSuperEntityTypes()) {
				ps.println(superET.name + "\t" + subET.name);
			}

		}
		ps.close();

	}

	public void writeSlotsSpecificationFile(File file, EntityType rootEntity) throws IOException {

		Set<EntityType> relatedEntities = new HashSet<>();
		addRecursive(rootEntity, relatedEntities);
		Set<SlotType> slotTypes = new HashSet<>();
		for (EntityType et : relatedEntities) {
			slotTypes.addAll(et.getSlots());
		}

		PrintStream ps = new PrintStream(file);
		ps.println("#Slot\tMaxCardinality");
		for (SlotType slotType : slotTypes) {
			ps.println(slotType.name + "\t"
					+ (slotType.slotMaxCapacity == Integer.MAX_VALUE ? -1 : slotType.slotMaxCapacity));
		}

		ps.close();
	}

	public void writeStructuresSpecificationFile(File input, File output, EntityType rootEntity) throws IOException {

		Set<EntityType> relatedEntities = new HashSet<>();
		addRecursive(rootEntity, relatedEntities);
		Set<SlotType> slotTypes = new HashSet<>();
		for (EntityType et : relatedEntities) {
			slotTypes.addAll(et.getSlots());
		}

		List<String> inputLines = Files.readAllLines(input.toPath());
		PrintStream ps = new PrintStream(output);

		ps.println("#Entity\tSlot\tSlotSuperEntityType");

		for (String inLine : inputLines) {
			if (inLine.startsWith("#"))
				continue;
			String[] data = inLine.split("\t");
			if (relatedEntities.contains(EntityType.get(data[0])) && slotTypes.contains(SlotType.get(data[1]))
					&& relatedEntities.contains(EntityType.get(data[2]))) {
				ps.println(inLine);
			}

		}

		ps.close();

	}

	private void addRecursive(EntityType entity, Set<EntityType> relatedEntities) {

		if (relatedEntities.contains(entity))
			return;

		relatedEntities.add(entity);

		for (SlotType slotType : entity.getSlots()) {
			for (EntityType entityType : slotType.getSlotFillerEntityTypes()) {
//				System.out.println(slotType.name + "-> " + entity.name + "\t" + entityType.name);
				addRecursive(entityType, relatedEntities);
			}
		}

		for (EntityType relatedEntityType : entity.getTransitiveClosureSubEntityTypes()) {
//			System.out.println("R-> " + entity.name + "\t" + relatedEntityType.name);
			addRecursive(relatedEntityType, relatedEntities);
		}

		return;

	}

}

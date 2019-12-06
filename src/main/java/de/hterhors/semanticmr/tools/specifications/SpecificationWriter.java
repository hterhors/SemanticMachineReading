package de.hterhors.semanticmr.tools.specifications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.init.specifications.SystemScope;

public class SpecificationWriter {

	public SpecificationWriter(SystemScope scope) {

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

	public void writeStructuresSpecificationFile(File file, EntityType rootEntity) {

		Set<EntityType> relatedEntities = new HashSet<>();
		addRecursive(rootEntity, relatedEntities);

		Set<EntityType> rootEntities = new HashSet<>();
		for (EntityType entityType : relatedEntities) {
			if (entityType.getDirectSuperEntityTypes().isEmpty()) {
				rootEntities.add(entityType);
			} else {

				for (EntityType superEntity : entityType.getDirectSuperEntityTypes()) {
					if (!relatedEntities.contains(superEntity)) {
						rootEntities.add(entityType);
					}
				}
			}
		}

		System.out.println("#Entity\tSlot\tSlotSuperEntityType");

		for (EntityType root : rootEntities) {
			for (SlotType slotType : root.getSlots()) {

				for (EntityType slotFiller : slotType.getSlotFillerEntityTypes()) {
					if (rootEntities.contains(slotFiller)) {

						System.out.println(root.name + "\t" + slotType.name + "\t" + slotFiller.name);
					} else {
						System.out.println("Fail");
					}
				}

			}
		}

	}

	private void addRecursive(EntityType entity, Set<EntityType> relatedEntities) {

		if (relatedEntities.contains(entity))
			return;

		relatedEntities.add(entity);

		for (SlotType slotType : entity.getSlots()) {
			for (EntityType entityType : slotType.getSlotFillerEntityTypes()) {
//	System.out.println("S-> "+entity +"\t"+entityType);
				addRecursive(entityType, relatedEntities);
			}
		}

		for (EntityType relatedEntityType : entity.getTransitiveClosureSubEntityTypes()) {
//			System.out.println("R-> "+entity +"\t"+relatedEntityType);
			addRecursive(relatedEntityType, relatedEntities);
		}

		return;

	}

}

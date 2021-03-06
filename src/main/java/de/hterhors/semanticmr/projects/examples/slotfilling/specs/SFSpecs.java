package de.hterhors.semanticmr.projects.examples.slotfilling.specs;

import java.io.File;

import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;

public class SFSpecs {
	/**
	 * The file that contains specifications about entities.
	 */
	private static final File entities = new File(
			"src/main/resources/examples/slotfilling/specifications/csv/entities.csv");

	/**
	 * Specification file that contains information about slots.
	 **/
	private static final File slots = new File("src/main/resources/examples/slotfilling/specifications/csv/slots.csv");
	/**
	 * Specification file that contains information about slots of entities.
	 **/
	private static final File structures = new File(
			"src/main/resources/examples/slotfilling/specifications/csv/structures.csv");

	/**
	 * Specification file of entity hierarchies.
	 */
	private static final File hierarchies = new File(
			"src/main/resources/examples/slotfilling/specifications/csv/hierarchies.csv");

	public final static CSVDataStructureReader systemsScopeReader = new CSVDataStructureReader(entities, hierarchies, slots,
			structures);
}

package de.hterhors.semanticmr.projects.soccerplayer;

import java.io.File;

import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;

public class SoccerPlayerSpecs {
	/**
	 * The file that contains specifications about entities.
	 */
	private static final File entities = new File("src/main/resources/soccerplayer/specifications/csv/entities.csv");

	/**
	 * Specification file that contains information about slots.
	 **/
	private static final File slots = new File("src/main/resources/soccerplayer/specifications/csv/slots.csv");
	/**
	 * Specification file that contains information about slots of entities.
	 **/
	private static final File structures = new File(
			"src/main/resources/soccerplayer/specifications/csv/structures.csv");

	/**
	 * Specification file of entity hierarchies.
	 */
	private static final File hierarchies = new File(
			"src/main/resources/soccerplayer/specifications/csv/hierarchies.csv");

	public final static CSVScopeReader systemsScopeReader = new CSVScopeReader(entities, hierarchies, slots,
			structures);
}

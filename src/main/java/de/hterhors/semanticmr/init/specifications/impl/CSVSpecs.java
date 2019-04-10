package de.hterhors.semanticmr.init.specifications.impl;

import java.io.File;

import de.hterhors.semanticmr.init.reader.csv.CSVSpecifictationsReader;
import de.hterhors.semanticmr.init.specifications.SpecificationsProvider;

public class CSVSpecs {

	private final File entitySpecifications = new File(
			"src/main/resources/specifications/csv/entitySpecifications.csv");
	private final File slotSpecifications = new File("src/main/resources/specifications/csv/slotSpecifications.csv");
	private final File entityStructureSpecifications = new File(
			"src/main/resources/specifications/csv/entityStructureSpecifications.csv");
	private final File slotPairConstraitsSpecifications = new File(
			"src/main/resources/specifications/csv/slotPairExcludingConstraints.csv");

	public final SpecificationsProvider specificationProvider = new SpecificationsProvider(new CSVSpecifictationsReader(
			entitySpecifications, entityStructureSpecifications, slotSpecifications, slotPairConstraitsSpecifications));

}

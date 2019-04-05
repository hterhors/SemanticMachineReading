package de.hterhors.semanticmr.init.reader;

import de.hterhors.semanticmr.exceptions.InvalidFileFormatException;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;

public interface ISpecificationsReader {

	/**
	 * Validates the file format.
	 * 
	 * @throws InvalidFileFormatException
	 */
	StructureSpecification read() throws InvalidFileFormatException;

}

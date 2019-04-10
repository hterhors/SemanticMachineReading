package de.hterhors.semanticmr.init.reader;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;

public interface ISpecificationsReader {

	/**
	 * Validates the file format.
	 * 
	 * @throws InvalidSpecificationFileFormatException
	 */
	StructureSpecification read() throws InvalidSpecificationFileFormatException;

}

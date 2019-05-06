package de.hterhors.semanticmr.init.reader;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.specifications.Specifications;

public interface ISpecificationsReader {

	/**
	 * Validates the file format.
	 * 
	 * @throws InvalidSpecificationFileFormatException
	 */
	Specifications read() throws InvalidSpecificationFileFormatException;

}

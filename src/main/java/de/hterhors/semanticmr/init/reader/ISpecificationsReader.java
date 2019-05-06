package de.hterhors.semanticmr.init.reader;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.specifications.Specifications;

public interface ISpecificationsReader {

	/**
	 * Reads the specification file(s) and returns them.
	 * 
	 * @throws InvalidSpecificationFileFormatException
	 */
	Specifications read() throws InvalidSpecificationFileFormatException;

}

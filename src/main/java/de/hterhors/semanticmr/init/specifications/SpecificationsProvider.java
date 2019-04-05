package de.hterhors.semanticmr.init.specifications;

import de.hterhors.semanticmr.exceptions.InvalidSpecificationException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;

/**
 * SpecificationProvider reads the specification file from the file system,
 * validates it and provides all necessary information.
 * 
 * Requires a SpecificationFileReader.
 * 
 * @author hterhors
 *
 */
public class SpecificationsProvider {

	private StructureSpecification specifications;

	public StructureSpecification getSpecifications() {
		return specifications;
	}

	public SpecificationsProvider(ISpecificationsReader specificationFileReader) {
		this.specifications = validateSpecifications(specificationFileReader.read());
	}

	/**
	 * Validates the given specification file for semantic errors.
	 * 
	 * @return
	 * 
	 * @throws InvalidSpecificationException if the file is not in the correct
	 *                                       format.
	 */
	private StructureSpecification validateSpecifications(final StructureSpecification specificationFile) {
		String errorMessage = "Unkown error";

		/*
		 * TODO: Implement validation.
		 */
		if (true)
			return specificationFile;

		throw new InvalidSpecificationException("The provided specification file with name " + this.specifications
				+ "is not in the required file format. Error message: " + errorMessage);

	}

}

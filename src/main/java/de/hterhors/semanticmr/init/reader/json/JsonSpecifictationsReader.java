package de.hterhors.semanticmr.init.reader.json;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.exce.InvalidSpecificationFileFormatException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;
import de.hterhors.semanticmr.init.specifications.Specifications;

public class JsonSpecifictationsReader implements ISpecificationsReader {
	private static Logger log = LogManager.getFormatterLogger(JsonSpecifictationsReader.class);

	public JsonSpecifictationsReader(File specificationFile) {
	}

	@Override
	public Specifications read() throws InvalidSpecificationFileFormatException {

//		this.specificationFile

		return null;
	}

}

package de.hterhors.semanticmr.psink.santo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.santo.converter.Santo2JsonConverter;
import de.hterhors.semanticmr.santo.converter.SantoRDFConverter;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;

public class ScioSanto2Json {

	public static void main(String[] args) throws IOException {

		final String scioNameSpace = "http://psink.de/scio";
		final String resourceNameSpace = "http://scio/data";

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSpecs().specificationProvider).apply();

		Santo2JsonConverter converter = new Santo2JsonConverter(initializer,
				new File("data/test/N001 Yoo, Khaled et al. 2013_export.csv"),
				new File("data/test/N001 Yoo, Khaled et al. 2013_admin.annodb"),
				new File("data/test/N001 Yoo, Khaled et al. 2013_admin.n-triples"), scioNameSpace, resourceNameSpace);

		converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#comment>");
		converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#label>");

		converter.convert(new File("src/main/resources/corpus/json/OrganismModel.json"), "RatModel", false);

	}

}

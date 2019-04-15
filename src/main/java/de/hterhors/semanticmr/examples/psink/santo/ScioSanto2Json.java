package de.hterhors.semanticmr.examples.psink.santo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;
import de.hterhors.semanticmr.santo.converter.Santo2JsonConverter;

public class ScioSanto2Json {

	public static void main(String[] args) throws IOException {

		final String exportDate = "10012019";
		final String scioNameSpace = "http://psink.de/scio";
		final String resourceNameSpace = "http://scio/data";

		SystemInitializer initializer = SystemInitializer.initialize(new CSVSpecs().specificationProvider).apply();

		final String dir = "rawData/export_" + exportDate + "/";
		List<String> fileNames = Arrays.stream(new File(dir).listFiles()).filter(f -> f.getName().endsWith(".csv"))
				.map(f -> f.getName().substring(0, f.getName().length() - 11)).collect(Collectors.toList());
		Collections.sort(fileNames);

		for (String name : fileNames) {

			System.out.println(name);

			Santo2JsonConverter converter = new Santo2JsonConverter(initializer, name,
					new File("rawData/export_10012019/" + name + "_export.csv"),
					new File("rawData/export_10012019/" + name + "_Jessica.annodb"),
					new File("rawData/export_10012019/" + name + "_Jessica.n-triples"), scioNameSpace,
					resourceNameSpace);

			converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#comment>");
			converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#label>");

			converter.convert(new File("src/main/resources/" + name + "_OrganismModel.json"), "OrganismModel", true,
					false);
		}

	}

}

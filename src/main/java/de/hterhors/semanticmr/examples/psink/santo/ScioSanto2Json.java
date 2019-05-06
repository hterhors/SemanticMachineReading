package de.hterhors.semanticmr.examples.psink.santo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.santo.converter.Santo2JsonConverter;

public class ScioSanto2Json {

	private static final File entities = new File("src/main/resources/specifications/csv/Result/entities.csv");
	private static final File slots = new File("src/main/resources/specifications/csv/Result/slots.csv");
	private static final File structures = new File("src/main/resources/specifications/csv/Result/structures.csv");
	private static final File hierarchies = new File("src/main/resources/specifications/csv/Result/hierarchies.csv");

	public final static CSVScopeReader systemsScope = new CSVScopeReader(entities, hierarchies, slots, structures);

	public static void main(String[] args) throws IOException {

		final String exportDate = "10012019";
		final String scioNameSpace = "http://psink.de/scio";
		final String resourceNameSpace = "http://scio/data";

		SystemScope scope = SystemScope.Builder.getSpecsHandler().addScopeSpecification(systemsScope).build();

		final String dir = "rawData/export_" + exportDate + "/";
		List<String> fileNames = Arrays.stream(new File(dir).listFiles()).filter(f -> f.getName().endsWith(".csv"))
				.map(f -> f.getName().substring(0, f.getName().length() - 11)).collect(Collectors.toList());
		Collections.sort(fileNames);

		for (String name : fileNames) {

			System.out.println(name);

			Santo2JsonConverter converter = new Santo2JsonConverter(scope, name,
					new File("rawData/export_10012019/" + name + "_export.csv"),
					new File("rawData/export_10012019/" + name + "_Jessica.annodb"),
					new File("rawData/export_10012019/" + name + "_Jessica.n-triples"), scioNameSpace,
					resourceNameSpace);

			converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#comment>");
			converter.addIgnoreProperty("<http://www.w3.org/2000/01/rdf-schema#label>");

			converter.convert(new File("test/" + name + "_Result.json"), "Result", true, true);
//			converter.convert(new File("src/main/resources/corpus/data/instances/" + name + "_OrganismModel.json"),
//					"OrganismModel", true, false);
		}

	}

}

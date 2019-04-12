package de.hterhors.semanticmr.santo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;

public class ConvertAnnoDBRawData2Documents {

	public static void main(String[] args) throws IOException {

		String name = "N001 Yoo, Khaled et al. 2013";

		List<String> lines = Files.readAllLines(new File("data/" + name + "_export.csv").toPath());

		List<DocumentToken> tokens = lines.stream().filter(l -> !l.isEmpty() && !l.startsWith("#"))
				.map(l -> line2DocumentToken(l)).collect(Collectors.toList());

		tokens.forEach(System.out::println);

		Document doc = new Document(name, tokens);
		
		System.out.println(doc.documentContent);
	}

	public static DocumentToken line2DocumentToken(final String line) {

		final String[] data = line.split(",", 9);

		for (int i = 0; i < data.length; i++) {
			data[i] = data[i];
		}

		return new DocumentToken(Integer.parseInt(data[1].trim()), Integer.parseInt(data[2].trim()),
				Integer.parseInt(data[3].trim()), Integer.parseInt(data[4].trim()), Integer.parseInt(data[6].trim()),
				data[8].trim().substring(1, data[8].trim().length() - 1));

	}

}

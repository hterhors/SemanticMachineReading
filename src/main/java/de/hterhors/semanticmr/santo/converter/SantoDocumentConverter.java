package de.hterhors.semanticmr.santo.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.variables.DocumentToken;

public class SantoDocumentConverter {

	/**
	 * The indices.
	 */
	final static private int sentenceIndex = 1, senTokenIndex = 2, docTokenIndex = 3, senCharOnsetIndex = 4,
			docCharOnsetIndex = 6, textIndex = 8;

	/**
	 * Converts a given SANTO-document formatted file into a list of DocumentTokens.
	 * 
	 * @param documentFile
	 * @return the list of DocumentTokens.
	 *  * @throws IOException
	 */
	public static List<DocumentToken> convert(File documentFile) throws IOException {
		return Files.readAllLines(documentFile.toPath()).stream().filter(l -> !l.isEmpty() && !l.startsWith("#"))
				.map(l -> line2DocumentToken(trimArrayValues(l.split(",", 9)))).collect(Collectors.toList());
	}

	/**
	 * Trims all values of a given string array.
	 * 
	 * @param split
	 * @return the same array where each value is trimmed.
	 */
	private static String[] trimArrayValues(String[] split) {
		for (int i = 0; i < split.length; i++) {
			split[i] = split[i].trim();
		}
		return split;
	}

	/**
	 * Converts a split line of the SANTO-Document export format into a
	 * DocumentToken.
	 * 
	 * @param lineData the line split into its data
	 * @return new DocumentToken.
	 */
	private static DocumentToken line2DocumentToken(final String[] lineData) {
		return new DocumentToken(Integer.parseInt(lineData[sentenceIndex]), Integer.parseInt(lineData[senTokenIndex]),
				Integer.parseInt(lineData[docTokenIndex]), Integer.parseInt(lineData[senCharOnsetIndex]),
				Integer.parseInt(lineData[docCharOnsetIndex]),
				lineData[textIndex].substring(1, lineData[textIndex].length() - 1));
	}

}

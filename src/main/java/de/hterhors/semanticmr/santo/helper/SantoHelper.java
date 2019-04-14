package de.hterhors.semanticmr.santo.helper;

import java.util.regex.Matcher;

public class SantoHelper {
	
	public static String getResource(String uri) {
		/**
		 * QUICK FIX:
		 */
		if (uri.equals("<http://psink.de/scio/hasTreatmentLocation>"))
			uri = "<http://psink.de/scio/hasLocation>";

		Matcher m = PatternCollection.NAME_SPACE_EXTRACTOR.matcher(uri);
		m.find();
		return m.group(4) == null ? m.group(3) : m.group(4);
	}

	public static String getNameSpace(String uri) {

		if (uri.isEmpty()) {
			throw new IllegalStateException("Value of property is empty!");
		}

		Matcher m = PatternCollection.NAME_SPACE_EXTRACTOR.matcher(uri);
		m.find();
		return m.group(4) == null ? m.group(2) : null;
	}
}

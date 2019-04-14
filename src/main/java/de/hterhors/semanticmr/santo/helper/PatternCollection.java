package de.hterhors.semanticmr.santo.helper;

import java.util.regex.Pattern;

public class PatternCollection {

	final static public Pattern TRIPLE_EXTRACTOR_PATTERN = Pattern
			.compile("(<http:.*?/[^/]*?>) (<http:.*?/[^/]*?>) ((<http:.*?/[^/]*?>)|\"(.*?)\") ?\\.");

	final static public Pattern NAME_SPACE_EXTRACTOR = Pattern.compile("(<(http:.*)/([^/]*)>)|(.+)");

}

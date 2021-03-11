package de.hterhors.semanticmr.nerla.annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.hterhors.semanticmr.crf.structure.EntityType;

public abstract class BasicRegExPattern implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static void main(String[] args) {
		System.out.println(CAMEL_CASE_SPLIT_PATTERN);
		
		String x[] = "Hallo Welt".split(" ");
		System.out.println(Arrays.toString(x));
		
	}
	
	/**
	 * Standard set of stop words.
	 */
//	public static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList("i", "me", "my", "myself", "we", "our",
//			"ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she",
//			"her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what",
//			"which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been",
//			"being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if",
//			"or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between",
//			"into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out",
//			"on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why",
//			"how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not",
//			"only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should",
//			"now"));

	public static final Set<String> STOP_WORDS = new HashSet<>(
			Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it",
					"no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they",
					"this", "to", "was", "will", "with", "his", "her", "from", "who", "whom"));
	private final EntityType rootEntityType;

	public EntityType getRootEntityType() {
		return rootEntityType;
	}

	public BasicRegExPattern(EntityType rootEntityType) {
		this.rootEntityType = rootEntityType;
	}

	public abstract Set<String> getStopWords();

	public static final String SPECIAL_CHARS = "\\W|_";

	public static final String CAMEL_CASE_SPLIT_PATTERN = "(?<!(^|[A-Z" + SPECIAL_CHARS + "]))(?=[A-Z" + SPECIAL_CHARS
			+ "])|(?<!^)(?=[A-Z" + SPECIAL_CHARS + "][a-z" + SPECIAL_CHARS + "])";

	public static final String PRE_BOUNDS = "(\\b|(?<= ))";
	public static final String POST_BOUNDS = "(\\b|(?= ))";
	public final static String BAD_CHAR = "[^\\x20-\\x7E]+";

	public static final int PATTERN_BITMASK = Pattern.CASE_INSENSITIVE + Pattern.DOTALL;

	protected static String buildQuotedRegExpr(final String param1, final String[] param2, final String param3) {

		StringBuffer param2Builer = new StringBuffer();

		if (param2 != null && param2.length > 0) {
			for (int i = 0; i < param2.length; i++) {
				param2Builer.append("(");
				param2Builer.append(".?" + Pattern.quote(param2[i]));
				if (i + 1 != param2.length)
					param2Builer.append("(-)?");
				param2Builer.append(")?");
			}
		}

		return Pattern.quote(param1) + "(" + (param2Builer.length() == 0 ? "" : param2Builer.toString()) + ")?"
				+ (param3 == null || param3.isEmpty() ? "" : "(.?" + Pattern.quote(param3) + ")?");
	}

	protected static String buildQuotedRegExpr(final String param1, final String param2, final String[] param3,
			final String param4) {

		StringBuffer param3Builer = new StringBuffer();

		if (param3 != null && param3.length > 0) {
			for (int i = 0; i < param3.length - 1; i++) {
				param3Builer.append(".?" + Pattern.quote(param3[i]));
				param3Builer.append("|");
			}
			param3Builer.append(".?" + Pattern.quote(param3[param3.length - 1]));
		}

		return "(" + Pattern.quote(param1) + "(.?" + Pattern.quote(param2) + ")?|" + Pattern.quote(param2) + ")("
				+ (param3Builer.length() == 0 ? "" : "(" + param3Builer.toString() + ")?")
				+ (param4 == null || param4.isEmpty() ? "" : "(.?" + Pattern.quote(param4) + ")?") + ")?";
	}

	protected static String buildRegExpr(final String param1, final String[] param2, final String param3) {

		StringBuffer param2Builer = new StringBuffer();

		if (param2 != null && param2.length > 0) {
			for (int i = 0; i < param2.length; i++) {
				param2Builer.append("(");
				param2Builer.append(".?" + param2[i]);
				if (i + 1 != param2.length)
					param2Builer.append("(-)?");
				param2Builer.append(")?");
			}
		}

		return param1 + "(" + (param2Builer.length() == 0 ? "" : param2Builer.toString()) + ")?"
				+ (param3 == null || param3.isEmpty() ? "" : "(.?" + param3 + ")?");
	}

	protected static String buildRegExpr(final String param1, final String param2, final String[] param3,
			final String param4) {

		StringBuffer param3Builer = new StringBuffer();

		if (param3 != null && param3.length > 0) {
			for (int i = 0; i < param3.length - 1; i++) {
				param3Builer.append(".?" + param3[i]);
				param3Builer.append("|");
			}
			param3Builer.append(".?" + param3[param3.length - 1]);
		}

		return "(" + param1 + "(.?" + param2 + ")?|" + param2 + ")("
				+ (param3Builer.length() == 0 ? "" : "(" + param3Builer.toString() + ")?")
				+ (param4 == null || param4.isEmpty() ? "" : "(.?" + param4 + ")?") + ")?";
	}

	public Pattern toPattern(EntityType entityType) {

		if (entityType.isLiteral)
			return null;

		final List<String> names = new ArrayList<>();

		for (String w : entityType.name.split(CAMEL_CASE_SPLIT_PATTERN)) {

			w = w.replaceAll(SPECIAL_CHARS, "");
			if (STOP_WORDS.contains(w.toLowerCase()) || getStopWords().contains(w.toLowerCase()))
				continue;

			if (w.length() < getMinTokenlength())
				continue;

			names.add(w);

		}

		if (names.isEmpty())
			return null;

		if (names.size() == 1) {
			return Pattern.compile(PRE_BOUNDS + buildQuotedRegExpr(names.get(0), null, null) + POST_BOUNDS,
					PATTERN_BITMASK);
		} else if (names.size() == 2) {
			return Pattern.compile(
					PRE_BOUNDS + buildQuotedRegExpr(names.get(0), new String[] { names.get(1) }, null) + POST_BOUNDS,
					PATTERN_BITMASK);
		} else if (names.size() == 3) {
			return Pattern.compile(PRE_BOUNDS
					+ buildQuotedRegExpr(names.get(0), names.get(1), new String[] { names.get(2) }, null) + POST_BOUNDS,
					PATTERN_BITMASK);
		} else if (names.size() == 4) {
			return Pattern.compile(PRE_BOUNDS
					+ buildQuotedRegExpr(names.get(0), names.get(1), new String[] { names.get(2) }, names.get(3))
					+ POST_BOUNDS, PATTERN_BITMASK);
		} else if (names.size() == 5) {
			return Pattern
					.compile(
							PRE_BOUNDS + buildQuotedRegExpr(names.get(0), names.get(1),
									new String[] { names.get(2), names.get(3) }, names.get(4)) + POST_BOUNDS,
							PATTERN_BITMASK);
		} else {
			return Pattern
					.compile(
							PRE_BOUNDS + buildQuotedRegExpr(names.get(0), names.get(1),
									new String[] { names.get(2), names.get(3) }, names.get(4)) + POST_BOUNDS,
							PATTERN_BITMASK);
		}

	}

	public abstract int getMinTokenlength();

	public abstract Map<EntityType, Set<Pattern>> getHandMadePattern();

}

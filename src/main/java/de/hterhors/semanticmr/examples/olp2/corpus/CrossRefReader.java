package de.hterhors.semanticmr.examples.olp2.corpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Easy and (very) dirty cross ref reader!
 * 
 * @author hterhors
 *
 */
public class CrossRefReader {
	public static void main(String[] args) throws IOException {
		
		for (int i = 90; i <= 120; i++) {
			System.out.println(i+"\tfalse");
		}
		
		CrossRefReader crr = new CrossRefReader();
		TextReader tr = new TextReader();

		x: for (Entry<String, CrossRef> string : crr.crossRefMap.entrySet()) {
			System.out.println(string.getKey());
			for (Entry<String, Set<String>> string2 : string.getValue().de_crossRefMap.entrySet()) {

				if (string2.getValue().size() == 1)
					continue;

				System.out.println(string2.getKey());
				System.out.println(string2.getValue());

				for (String text : string2.getValue()) {
					System.out.println(tr.de_textMap.get(text));
				}
//				break x;
			}
			System.out.println("");
		}

//		crr.de_crossRefMap.entrySet().stream().limit(10).forEach(System.out::println);
//		crr.en_crossRefMap.entrySet().forEach(System.out::println);
	}

	private final Pattern textPattern = Pattern.compile("<Text id=\"(.*?)\" />");
	private final Pattern semiStructPattern = Pattern.compile("<SemiStruct id=\"(.*?)\" />");

	public static class CrossRef {

		public final String crossRefID;

		public final Map<String, Set<String>> de_crossRefMap = new HashMap<>();
		public final Map<String, Set<String>> en_crossRefMap = new HashMap<>();

		public CrossRef(String crossrefID) {
			this.crossRefID = crossrefID;
		}

	}

	public final Map<String, CrossRef> crossRefMap = new HashMap<>();

	public CrossRefReader() throws IOException {

		File crossRefDir = new File("data/Crossref/");

		for (File crossRefFile : crossRefDir.listFiles()) {

			String doc = Files.readAllLines(crossRefFile.toPath()).stream().reduce(String::concat).get();

			if (doc.contains("Text")) {

				CrossRef cr = new CrossRef(crossRefFile.getName());
				crossRefMap.put(cr.crossRefID, cr);

				Matcher mT = textPattern.matcher(doc);

				while (mT.find()) {

					Matcher mS = semiStructPattern.matcher(doc);
					while (mS.find()) {

						final String t = mT.group(1);
						final String s = mS.group(1);

						if (t.startsWith("de_") && s.startsWith("de_")) {
							crossRefMap.get(cr.crossRefID).de_crossRefMap.putIfAbsent(s, new HashSet<>());
							crossRefMap.get(cr.crossRefID).de_crossRefMap.get(s).add(t);
						} else if (t.startsWith("en_") && s.startsWith("en_")) {
							crossRefMap.get(cr.crossRefID).en_crossRefMap.put(s, new HashSet<>());
							crossRefMap.get(cr.crossRefID).en_crossRefMap.get(s).add(t);
						}
					}
				}
			}
		}
	}

}

package de.hterhors.semanticmr.examples.olp2.corpus.preprocessing;

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

	private final Pattern textPattern = Pattern.compile("<Text id=\"(.*?)\" />");
	private final Pattern semiStructPattern = Pattern.compile("<SemiStruct id=\"(.*?)\" />");

	public static class CrossRef {

		public final String crossRefID;
		public String structID = null;

		public final Set<String> crossRefMap = new HashSet<>();

		public CrossRef(String crossrefID) {
			this.crossRefID = crossrefID;
		}

		public void setStructID(String structID) {
			if (this.structID != null && (!structID.equals(this.structID) || this.structID == null))
				throw new IllegalStateException(
						"Multiple structs identified in:" + crossRefID + ": " + structID + "!=" + this.structID);
			this.structID = structID;
		}

	}

	public final Map<String, CrossRef> de_crossRefMap = new HashMap<>();
	public final Map<String, CrossRef> en_crossRefMap = new HashMap<>();

	public CrossRefReader(File crossRefDir) throws IOException {

		for (File crossRefFile : crossRefDir.listFiles()) {

			String doc = Files.readAllLines(crossRefFile.toPath()).stream().reduce(String::concat).get();

			if (doc.contains("Text")) {

				Matcher mT = textPattern.matcher(doc);

				while (mT.find()) {

					Matcher mS = semiStructPattern.matcher(doc);
					while (mS.find()) {

						final String t = mT.group(1);
						final String s = mS.group(1);

						Map<String, CrossRef> crossRefMap;
						if (t.startsWith("de_") && s.startsWith("de_")) {
							crossRefMap = de_crossRefMap;
						} else if (t.startsWith("en_") && s.startsWith("en_")) {
							crossRefMap = en_crossRefMap;
						} else {
							crossRefMap = null;
						}

						if (crossRefMap == null)
							continue;

						if (!crossRefMap.containsKey(crossRefFile.getName())) {
							CrossRef cr = new CrossRef(crossRefFile.getName());
							crossRefMap.putIfAbsent(crossRefFile.getName(), cr);
						}
						crossRefMap.get(crossRefFile.getName()).setStructID(s);
						crossRefMap.get(crossRefFile.getName()).crossRefMap.add(t);
					}
				}
			}
		}
	}

}

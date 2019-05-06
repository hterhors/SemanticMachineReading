package de.hterhors.semanticmr.projects.olp2.corpus.preprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Easy and (very) dirty text reader!
 * 
 * @author hterhors
 *
 */
public class TextReader {

	private final Pattern textPattern = Pattern.compile("<bodyelem id=\"(.+?)\">(.*?)</bodyelem>");
	private final Pattern idPattern = Pattern.compile("<document name=\"(.*?)\">");
	private final Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");

	public final Map<String, String> de_textMap = new HashMap<>();
	public final Map<String, String> en_textMap = new HashMap<>();

	public TextReader(File crossRefDir) throws IOException {

		for (File crossRefFile : crossRefDir.listFiles()) {

			String doc = Files.readAllLines(crossRefFile.toPath()).stream().reduce(String::concat).get();

			Matcher idp = idPattern.matcher(doc);
			Matcher titp = titlePattern.matcher(doc);

			if (idp.find() && titp.find()) {
				final String id = idp.group(1);

				Matcher mT = textPattern.matcher(doc);
				String text = titp.group(1).trim() + ". ";

				while (mT.find()) {

					text += mT.group(2).trim() + " ";

				}
				text = text.trim();
				if (id.startsWith("de_")) {
					de_textMap.put(id, text);
				} else if (id.startsWith("en_")) {
					en_textMap.put(id, text);
				}
			}
		}
	}

}

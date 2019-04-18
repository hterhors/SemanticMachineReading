package de.hterhors.semanticmr.santo.converter.reader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import com.opencsv.CSVReader;

import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.santo.container.RDFRelatedAnnotation;
import de.hterhors.semanticmr.santo.container.Triple;
import de.hterhors.semanticmr.santo.converter.SantoDocumentConverter;
import de.hterhors.semanticmr.santo.helper.PatternCollection;

public class TextualAnnotationsReader {

	private static final int ANNOTATION_ID_INDEX = 0;
	private static final int CLASS_TYPE_INDEX = 1;
	private static final int ONSET_INDEX = 2;
	private static final int OFFSET_INDEX = 3;
	private static final int TEXT_MENTION_INDEX = 4;
	private static final int META_INDEX = 5;
	private static final int RDF_LINK_INDEX = 6;

	final private File textualAnnotationsFile;
	final private Document document;

	private Map<Triple, RDFRelatedAnnotation> annotations;

	public TextualAnnotationsReader(File textualAnnotationsFile) {
		this.textualAnnotationsFile = textualAnnotationsFile;
		this.document = null;
	}

	public TextualAnnotationsReader(Document document, File textualAnnotationsFile) {
		this.textualAnnotationsFile = textualAnnotationsFile;
		this.document = document;
	}

	public Map<Triple, RDFRelatedAnnotation> getAnnotations() throws IOException {

		annotations = readAnnotations();

		validateTextualAnnotations();

		return annotations;
	}

	private void validateTextualAnnotations() {

		for (RDFRelatedAnnotation annotation : annotations.values()) {

			boolean found = false;
			for (DocumentToken documentToken : document.tokenList) {

				if (documentToken.docCharOffset == annotation.onset) {
					found = true;
					if (!annotation.textMention.startsWith(documentToken.text) && document.documentContent
							.substring(annotation.onset, annotation.offset).equals(annotation.textMention)) {
						System.out.println("WARN: Documents text does not match annotations text:" + documentToken.text
								+ " != " + annotation.textMention);
					}
					continue;
				}
			}
			if (!found) {
				System.out.println("WARN: offset does not match tokenization: " + annotation.onset);
			}
		}
	}

	private Map<Triple, RDFRelatedAnnotation> readAnnotations() throws IOException {
		CSVReader reader = null;

		final Map<Triple, RDFRelatedAnnotation> annotations = new HashMap<>();

		try {
			reader = new CSVReader(new FileReader(textualAnnotationsFile));
			String[] line;
			while ((line = reader.readNext()) != null) {

				if (line.length == 1 && line[0].isEmpty() || line[0].startsWith("#"))
					continue;

				if (!line[RDF_LINK_INDEX].trim().isEmpty()) {

					final Matcher m = PatternCollection.TRIPLE_EXTRACTOR_PATTERN.matcher(line[RDF_LINK_INDEX]);

					while (m.find()) {
						final String subject = m.group(1);
						final String predicate = m.group(2);
						final String object = m.group(5) == null ? m.group(4) : m.group(5);

						final Triple linkID = new Triple(subject, predicate, object);
						try {

							annotations.put(linkID,
									new RDFRelatedAnnotation(line[TEXT_MENTION_INDEX].trim(),
											Integer.parseInt(line[ONSET_INDEX].trim()),
											Integer.parseInt(line[OFFSET_INDEX].trim()),
											line[ANNOTATION_ID_INDEX].trim(), linkID));

						} catch (Exception e) {
							System.out.println(Arrays.toString(line));
							e.printStackTrace();
							throw e;
						}

					}
				} else {
					/*
					 * No RDF related annotation found.
					 */
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}

		return annotations;
	}

}

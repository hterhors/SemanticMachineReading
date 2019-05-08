package de.hterhors.semanticmr.tokenizer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.tokenizer.Tokenization.Token;

public class DefaultDocumentTokenizer {
	private static Logger log = LogManager.getFormatterLogger(DefaultDocumentTokenizer.class);

	public static List<DocumentToken> tokenizeDocumentsContent(final String content) {
		log.debug("Tokenize content...");

		List<DocumentToken> list = new ArrayList<>();

		List<String> sentences = SentenceSplitter.extractSentences(content);

		List<Tokenization> tokens = RegExTokenizer.tokenize(sentences);

		for (Tokenization tokenization : tokens) {

			int senTokenIndex = 0;

			for (Token token : tokenization.tokens) {

				int sentenceIndex = token.getSentenceIndex();
				int docTokenIndex = token.getIndex();
				int docCharOnset = token.getOnsetCharPosition();
				int senCharOnset = token.getFromSen();
				String text = token.getText();

				list.add(new DocumentToken(sentenceIndex, senTokenIndex, docTokenIndex, senCharOnset, docCharOnset,
						text));
				senTokenIndex++;
			}

		}
		log.debug("Number of tokens: " + list.size());

		return list;
	}

}

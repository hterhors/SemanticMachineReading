package de.hterhors.semanticmr.tokenizer;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.variables.DocumentToken;

public class StandardDocumentTokenizer {

	public static List<DocumentToken> tokenizeDocumentsContent(final String content) {

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

		return list;
	}

}

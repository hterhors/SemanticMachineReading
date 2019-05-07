package de.hterhors.semanticmr.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.tokenizer.Tokenization.Token;

public class RegExTokenizer {
	private static Logger log = LogManager.getFormatterLogger(RegExTokenizer.class);

//	private static Pattern pattern = Pattern.compile("[\\S]+");
	private static Pattern pattern = Pattern.compile("[a-zA-Z]+|\\d+|[^\\w\\s]");

	static {
		log.debug("Tokenization pattern = \"[a-zA-Z]+|\\d+|[^\\w\\s]\"");
	}

	public static List<Tokenization> tokenize(List<String> sentences) {
		log.debug("Tokenize number of sentences: " + sentences.size());
		List<Tokenization> tokenizations = new ArrayList<>();
		int accumulatedSentenceLength = 0;
		int sentenceIndex = 0;
		int index = 0;
		for (String sentence : sentences) {
			Matcher matcher = pattern.matcher(sentence);
			List<Token> tokens = new ArrayList<>();
			while (matcher.find()) {
				String text = matcher.group();
				int from = matcher.start();
				int to = matcher.end();
				tokens.add(new Token(sentenceIndex, index, accumulatedSentenceLength + from,
						accumulatedSentenceLength + to, text, from));
				index++;
			}
			sentenceIndex++;
			Tokenization tokenization = new Tokenization(tokens, sentence, accumulatedSentenceLength);
			tokenizations.add(tokenization);
			accumulatedSentenceLength += sentence.length();
		}
		return tokenizations;
	}

	public static Tokenization tokenize(String sentence) {
		int sentenceIndex = 0;
		int index = 0;
		Matcher matcher = pattern.matcher(sentence);
		List<Token> tokens = new ArrayList<>();
		while (matcher.find()) {
			String text = matcher.group();
			int from = matcher.start();
			int to = matcher.end();
			tokens.add(new Token(sentenceIndex, index, from, to, text, from));
			index++;
		}
		sentenceIndex++;
		Tokenization tokenization = new Tokenization(tokens, sentence, 0);
		return tokenization;
	}

}
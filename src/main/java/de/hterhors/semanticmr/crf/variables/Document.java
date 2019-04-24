package de.hterhors.semanticmr.crf.variables;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.FocusManager;

import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.exce.DuplicateDocumentException;

/**
 * The document class contains every necessary information of the textual
 * document. A document is part of an Instance.
 * 
 * @author hterhors
 * @see {@link Instance}
 *
 */
public class Document {

	final static public String TOKEN_SPLITTER = " ";

	final static private Map<String, Document> documents = new HashMap<>();

	static private Set<String> punctuationWords = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(",", ".", "-", "_", ";", ":", "#", "'", "+", "*", "~", "!", "\"", "§", "$", "%",
					"&", "/", "(", ")", "{", "}", "[", "]", "=", "?", "\\", "´", "`", "^", "°", "<", ">", "|")));

	static private Set<String> stopWords = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in",
					"into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
					"these", "they", "this", "to", "was", "will", "with", "his", "her", "from", "who", "whom")));

	final public String documentContent;

	final public String documentID;

	final public List<DocumentToken> tokenList;

	/**
	 * Tokens indexed by position.
	 */
	final private Map<Integer, DocumentToken> startOffsetCharPositionTokens = new HashMap<>();

	final private Map<Integer, DocumentToken> endOffsetCharPositionTokens = new HashMap<>();

	/**
	 * Create a new document without any textual content.
	 * 
	 * @param documentID
	 */
	public Document(String documentID) {
		this(documentID, Collections.emptyList());
	}

	/**
	 * Create a new document with textual content.
	 * 
	 * @param documentID
	 * @param tokenList
	 */
	public Document(String documentID, List<DocumentToken> tokenList) {
		this.documentID = documentID;

		if (Document.documents.containsKey(this.documentID))
			throw new DuplicateDocumentException("Document ID was already used: " + documentID);

		Document.documents.put(this.documentID, this);
		this.tokenList = Collections.unmodifiableList(tokenList);

		this.documentContent = builderDocumentContent(tokenList);

		for (DocumentToken token : tokenList) {
			startOffsetCharPositionTokens.put(new Integer(token.getDocCharOffset()), token);
			endOffsetCharPositionTokens.put(new Integer(token.getDocCharOffset() + token.getText().length()), token);
		}
		updateStopWords(this);
		updatePunctuationWords(this);
	}

	private String builderDocumentContent(List<DocumentToken> tokenList) {

		if (tokenList.isEmpty())
			return "";

		if (tokenList.size() == 1)
			return tokenList.get(1).getText();

		final StringBuilder documentContentBuilder = new StringBuilder();

		for (int i = 0; i < tokenList.size() - 1; i++) {
			documentContentBuilder.append(tokenList.get(i).getText());
			final int gap = tokenList.get(i + 1).getDocCharOffset()
					- (tokenList.get(i).getDocCharOffset() + tokenList.get(i).getText().length());

			for (int j = 0; j < gap; j++) {
				documentContentBuilder.append(TOKEN_SPLITTER);
			}
		}
		documentContentBuilder.append(tokenList.get(tokenList.size() - 1).getText());

		return documentContentBuilder.toString();
	}

	public static Set<String> getStopWords() {
		return stopWords;
	}

	public static void setStopWords(Set<String> stopWords) {
		Document.stopWords = Collections.unmodifiableSet(stopWords);

		for (Document doc : Document.documents.values()) {
			updateStopWords(doc);
		}
	}

	private static void updateStopWords(Document doc) {
		for (DocumentToken token : doc.tokenList) {
			token.setStopWord(Document.stopWords.contains(token.getText()));
		}
	}

	public static Set<String> getPunctuationWords() {
		return punctuationWords;
	}

	public static void setPunctuationWords(Set<String> punctuationWords) {
		Document.punctuationWords = Collections.unmodifiableSet(punctuationWords);

		for (Document doc : Document.documents.values()) {
			updatePunctuationWords(doc);
		}
	}

	private static void updatePunctuationWords(Document doc) {
		for (DocumentToken token : doc.tokenList) {
			token.setPunctuation(Document.punctuationWords.contains(token.getText()));
		}
	}

	@Override
	public String toString() {
		return "Document [documentID=" + documentID + "]";
	}

	public DocumentToken getTokenByCharOffset(Integer offset) throws DocumentLinkedAnnotationMismatchException {
		final DocumentToken token = startOffsetCharPositionTokens.getOrDefault(offset,
				endOffsetCharPositionTokens.get(offset));
		if (token == null)
			throw new DocumentLinkedAnnotationMismatchException(
					"Can not map charachter offset: " + offset + " to token in document: " + documentID);
		return token;
	}

	public String getContent(DocumentToken fromToken, DocumentToken toToken) {
		if (fromToken == toToken)
			return fromToken.getText();

		return this.documentContent.substring(fromToken.getDocCharOffset(),
				toToken.getDocCharOffset() + toToken.getText().length());
	}

}

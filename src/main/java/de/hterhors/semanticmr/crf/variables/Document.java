package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.helper.DefaultDocumentTokenizer;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

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
	 * TODO: to array? Tokens indexed by position.
	 */
	final private Map<Integer, DocumentToken> startOffsetCharPositionTokens = new HashMap<>();

	final private Map<Integer, DocumentToken> endOffsetCharPositionTokens = new HashMap<>();

	final private Map<Integer, List<DocumentToken>> sentencesByIndex = new HashMap<>();

	/**
	 * Create a new document without any textual content.
	 * 
	 * @param documentID
	 */
	public Document(String documentID) {
		this(documentID, Collections.emptyList());
	}

	/**
	 * Create a new document with textual content. The content is automatically
	 * tokenized using the {@link DefaultDocumentTokenizer}.
	 * 
	 * @param documentID
	 * @param tokenList
	 */
	public Document(String documentID, final String documentsContent) {
		this(documentID, DefaultDocumentTokenizer.tokenizeDocumentsContent(documentsContent));
	}

	/**
	 * Create a new document with textual content.
	 * 
	 * @param documentID
	 * @param tokenList
	 */
	public Document(String documentID, List<DocumentToken> tokenList) {
		this.documentID = documentID;

		Document.documents.put(this.documentID, this);

		this.tokenList = Collections.unmodifiableList(tokenList);

		this.documentContent = builderDocumentContent(tokenList);

		List<DocumentToken> sentence = new ArrayList<>();

		int prevSenIndex = 0;
		for (DocumentToken token : this.tokenList) {
			startOffsetCharPositionTokens.put(new Integer(token.getDocCharOffset()), token);
			endOffsetCharPositionTokens.put(new Integer(token.getDocCharOffset() + token.getText().length()), token);

			if (prevSenIndex != token.getSentenceIndex()) {
				sentencesByIndex.put(prevSenIndex, new ArrayList<>(sentence));
				sentence.clear();
				prevSenIndex = token.getSentenceIndex();
			}

			sentence.add(token);

		}
		sentencesByIndex.put(prevSenIndex, sentence);
		sentence = new ArrayList<>();

		updateStopWords(this);
		updatePunctuationWords(this);
	}

	private String builderDocumentContent(List<DocumentToken> tokenList) {

		if (tokenList.isEmpty())
			return "";

		if (tokenList.size() == 1)
			return tokenList.get(0).getText();

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
			token.setStopWord(Document.stopWords.contains(token.getText().toLowerCase()));
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

	public DocumentToken getTokenByCharStartOffset(Integer offset) throws DocumentLinkedAnnotationMismatchException {

		final DocumentToken token = startOffsetCharPositionTokens.get(offset);

		if (token == null) {
			throw new DocumentLinkedAnnotationMismatchException(
					"Can not map start charachter offset: " + offset + " to token in document: " + documentID);
		}
		return token;
	}

	public DocumentToken getTokenByCharEndOffset(Integer offset) throws DocumentLinkedAnnotationMismatchException {

		final DocumentToken token = endOffsetCharPositionTokens.get(offset);

		if (token == null) {
			throw new DocumentLinkedAnnotationMismatchException(
					"Can not map end charachter offset: " + offset + " to token in document: " + documentID);
		}
		return token;
	}

	public String getContent(DocumentToken fromToken, DocumentToken toToken) {
		if (fromToken == toToken)
			return fromToken.getText();

		return this.documentContent.substring(fromToken.getDocCharOffset(),
				toToken.getDocCharOffset() + toToken.getText().length());
	}

	public String getContentOfSentence(Integer sentenceIndex) {
		return builderDocumentContent(getSentenceByIndex(sentenceIndex));
	}

	public List<DocumentToken> getSentenceByIndex(int sentenceIndex) {
		return sentencesByIndex.get(sentenceIndex);
	}

	public int getNumberOfSentences() {
		return sentencesByIndex.size();
	}

	public Collection<List<DocumentToken>> getSentences() {
		return sentencesByIndex.values();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentID == null) ? 0 : documentID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (documentID == null) {
			if (other.documentID != null)
				return false;
		} else if (!documentID.equals(other.documentID))
			return false;
		return true;
	}

}

package de.hterhors.semanticmr.crf.variables;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	final static private Set<String> documentIDs = new HashSet<>();

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

		if (!documentIDs.add(this.documentID))
			throw new DuplicateDocumentException("Document ID was already used: " + documentID);

		this.tokenList = Collections.unmodifiableList(tokenList);

		this.documentContent = builderDocumentContent(tokenList);

		for (DocumentToken token : tokenList) {
			startOffsetCharPositionTokens.put(new Integer(token.docCharOffset), token);
			endOffsetCharPositionTokens.put(new Integer(token.docCharOffset + token.text.length()), token);
		}
	}

	private String builderDocumentContent(List<DocumentToken> tokenList) {

		if (tokenList.isEmpty())
			return "";

		if (tokenList.size() == 1)
			return tokenList.get(1).text;

		final StringBuilder documentContentBuilder = new StringBuilder();

		for (int i = 0; i < tokenList.size() - 1; i++) {
			documentContentBuilder.append(tokenList.get(i).text);
			final int gap = tokenList.get(i + 1).docCharOffset
					- (tokenList.get(i).docCharOffset + tokenList.get(i).text.length());

			for (int j = 0; j < gap; j++) {
				documentContentBuilder.append(" ");
			}
		}
		documentContentBuilder.append(tokenList.get(tokenList.size() - 1).text);

		return documentContentBuilder.toString();
	}

	@Override
	public String toString() {
		return "Document [documentID=" + documentID + "]";
	}

	public DocumentToken getTokenByCharOffset(Integer offset) throws IllegalArgumentException {
		final DocumentToken token = startOffsetCharPositionTokens.getOrDefault(offset,
				endOffsetCharPositionTokens.get(offset));
		if (token == null)
			throw new IllegalArgumentException(
					"Can not map charachter offset to token: " + offset + ", in document: " + documentID);
		return token;
	}

}

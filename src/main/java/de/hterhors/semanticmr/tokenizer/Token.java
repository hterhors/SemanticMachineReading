package de.hterhors.semanticmr.tokenizer;

import java.io.Serializable;

public class Token implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Position of this token in the list of tokens that make up the tokenized
	 * document.
	 */
	final private int documentIndex;
	/**
	 * Character offset position of this token in its sentence.
	 */
	final private int fromSen;

	/**
	 * Character position of this token in the original text.
	 */
	final private int from, to;
	/**
	 * Piece of text of the original document that the token offsets (from, to)
	 * correspond to.
	 */
	final private String text;

	/**
	 * The index of the sentence the token appears in.
	 */
	final private int sentenceIndex;

	public Token(int sentenceIndex, int index, int start, int stop, String text,int fromSen) {
		this.documentIndex = index;
		this.from = start;
		this.to = stop;
		this.text = text;
		this.sentenceIndex = sentenceIndex;
		this.fromSen = fromSen;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public int getOnsetCharPosition() {
		return from;
	}

	public int getOffsetCharPosition() {
		return to;
	}
public int getFromSen() {
	return fromSen;
}
	public String getText() {
		return text;
	}

	public int getIndex() {
		return documentIndex;
	}

	@Override
	public String toString() {
		return "Token [index=" + documentIndex + ", from=" + from + ", to=" + to + ", text=" + text + ", sentenceIndex="
				+ sentenceIndex + "]";
	}
}

package de.hterhors.semanticmr.crf.variables;

public class DocumentToken {

	final public int sentenceIndex;
	final public int senTokenIndex;
	final public int docTokenIndex;
	final public int senCharOnset;
	final public int docCharOnset;
	final public String text;

	public DocumentToken(int sentenceIndex, int senTokenIndex, int docTokenIndex, int senCharOnset, int docCharOnset,
			String text) {
		this.sentenceIndex = sentenceIndex;
		this.senTokenIndex = senTokenIndex;
		this.docTokenIndex = docTokenIndex;
		this.senCharOnset = senCharOnset;
		this.docCharOnset = docCharOnset;
		this.text = text;
	}

}

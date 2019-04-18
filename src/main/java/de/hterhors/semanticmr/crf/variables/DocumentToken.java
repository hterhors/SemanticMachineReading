package de.hterhors.semanticmr.crf.variables;

public class DocumentToken {

	final public int sentenceIndex;
	final public int senTokenIndex;
	final public int docTokenIndex;
	final public int senCharOffset;
	final public int docCharOffset;
	final public String text;

	public DocumentToken(int sentenceIndex, int senTokenIndex, int docTokenIndex, int senCharOnset, int docCharOnset,
			String text) {
		this.sentenceIndex = sentenceIndex;
		this.senTokenIndex = senTokenIndex;
		this.docTokenIndex = docTokenIndex;
		this.senCharOffset = senCharOnset;
		this.docCharOffset = docCharOnset;
		this.text = text;
	}

	@Override
	public String toString() {
		return "DocumentToken [sentenceIndex=" + sentenceIndex + ", senTokenIndex=" + senTokenIndex + ", docTokenIndex="
				+ docTokenIndex + ", senCharOffset=" + senCharOffset + ", docCharOffset=" + docCharOffset + ", text="
				+ text + "]";
	}

}

package de.hterhors.semanticmr.crf.variables;

public class DocumentToken {

	final private int sentenceIndex;
	final private int senTokenIndex;
	final private int docTokenIndex;
	final private int senCharOffset;
	final private int docCharOffset;
	final private String text;

	final private boolean isNumber;
	private boolean isStopWord = false;
	private boolean isPunctuation = false;

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public int getSenTokenIndex() {
		return senTokenIndex;
	}

	public int getDocTokenIndex() {
		return docTokenIndex;
	}

	public int getSenCharOffset() {
		return senCharOffset;
	}

	public int getDocCharOffset() {
		return docCharOffset;
	}

	public String getText() {
		return text;
	}

	public int getLength() {
		return text.length();
	}

	public DocumentToken(int sentenceIndex, int senTokenIndex, int docTokenIndex, int senCharOnset, int docCharOnset,
			String text) {
		this.sentenceIndex = sentenceIndex;
		this.senTokenIndex = senTokenIndex;
		this.docTokenIndex = docTokenIndex;
		this.senCharOffset = senCharOnset;
		this.docCharOffset = docCharOnset;
		this.text = text;
		this.isNumber = text.matches("\\d+");
	}

	@Override
	public String toString() {
		return "DocumentToken [sentenceIndex=" + sentenceIndex + ", senTokenIndex=" + senTokenIndex + ", docTokenIndex="
				+ docTokenIndex + ", senCharOffset=" + senCharOffset + ", docCharOffset=" + docCharOffset + ", text="
				+ text + "]";
	}

	public boolean isStopWord() {
		return isStopWord;
	}

	public boolean isNumber() {
		return isNumber;
	}

	public void setStopWord(boolean isStopWord) {
		this.isStopWord = isStopWord;
	}

	public boolean isPunctuation() {
		return isPunctuation;
	}

	public void setPunctuation(boolean isPunctuation) {
		this.isPunctuation = isPunctuation;
	}

}

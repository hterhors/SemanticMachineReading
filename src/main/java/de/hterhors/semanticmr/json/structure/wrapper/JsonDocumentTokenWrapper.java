package de.hterhors.semanticmr.json.structure.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonDocumentTokenWrapper {

	@SerializedName("si")
	private int sentenceIndex;
	@SerializedName("sti")
	private int senTokenIndex;
	@SerializedName("dti")
	private int docTokenIndex;
	@SerializedName("sco")
	private int senCharOffset;
	@SerializedName("dco")
	private int docCharOffset;
	@SerializedName("txt")
	private String text;

	public JsonDocumentTokenWrapper(int sentenceIndex, int senTokenIndex, int docTokenIndex, int senCharOnset,
			int docCharOnset, String text) {
		super();
		this.sentenceIndex = sentenceIndex;
		this.senTokenIndex = senTokenIndex;
		this.docTokenIndex = docTokenIndex;
		this.senCharOffset = senCharOnset;
		this.docCharOffset = docCharOnset;
		this.text = text;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	public int getSenTokenIndex() {
		return senTokenIndex;
	}

	public void setSenTokenIndex(int senTokenIndex) {
		this.senTokenIndex = senTokenIndex;
	}

	public int getDocTokenIndex() {
		return docTokenIndex;
	}

	@Override
	public String toString() {
		return "JsonDocumentTokenWrapper [sentenceIndex=" + sentenceIndex + ", senTokenIndex=" + senTokenIndex
				+ ", docTokenIndex=" + docTokenIndex + ", senCharOnset=" + senCharOffset + ", docCharOnset="
				+ docCharOffset + ", text=" + text + "]";
	}

	public void setDocTokenIndex(int docTokenIndex) {
		this.docTokenIndex = docTokenIndex;
	}

	public int getSenCharOnset() {
		return senCharOffset;
	}

	public void setSenCharOnset(int senCharOnset) {
		this.senCharOffset = senCharOnset;
	}

	public int getDocCharOnset() {
		return docCharOffset;
	}

	public void setDocCharOnset(int docCharOnset) {
		this.docCharOffset = docCharOnset;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + docCharOffset;
		result = prime * result + docTokenIndex;
		result = prime * result + senCharOffset;
		result = prime * result + senTokenIndex;
		result = prime * result + sentenceIndex;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		JsonDocumentTokenWrapper other = (JsonDocumentTokenWrapper) obj;
		if (docCharOffset != other.docCharOffset)
			return false;
		if (docTokenIndex != other.docTokenIndex)
			return false;
		if (senCharOffset != other.senCharOffset)
			return false;
		if (senTokenIndex != other.senTokenIndex)
			return false;
		if (sentenceIndex != other.sentenceIndex)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

}

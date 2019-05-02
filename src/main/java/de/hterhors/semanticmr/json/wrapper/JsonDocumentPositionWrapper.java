package de.hterhors.semanticmr.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonDocumentPositionWrapper {

	@SerializedName("off")
	private int charOffset;

	public JsonDocumentPositionWrapper(int charOffset) {
		super();
		this.charOffset = charOffset;
	}

	public int getCharOffset() {
		return charOffset;
	}

	public void setCharOffset(int charOffset) {
		this.charOffset = charOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + charOffset;
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
		JsonDocumentPositionWrapper other = (JsonDocumentPositionWrapper) obj;
		if (charOffset != other.charOffset)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonDocumentPositionWrapper [charOffset=" + charOffset + "]";
	}

}

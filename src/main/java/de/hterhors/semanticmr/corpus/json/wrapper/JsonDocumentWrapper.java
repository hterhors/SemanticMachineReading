package de.hterhors.semanticmr.corpus.json.wrapper;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class JsonDocumentWrapper {

	@SerializedName("id")
	private String documentID;

	@SerializedName("tl")
	private List<JsonDocumentTokenWrapper> tokenList;

	public JsonDocumentWrapper(String documentID, List<JsonDocumentTokenWrapper> tokenList) {
		super();
		this.documentID = documentID;
		this.tokenList = tokenList;
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public List<JsonDocumentTokenWrapper> getTokenList() {
		return tokenList;
	}

	@Override
	public String toString() {
		return "JsonDocumentWrapper [documentID=" + documentID + ", tokenList=" + tokenList + "]";
	}

	public void setTokenList(List<JsonDocumentTokenWrapper> tokenList) {
		this.tokenList = tokenList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentID == null) ? 0 : documentID.hashCode());
		result = prime * result + ((tokenList == null) ? 0 : tokenList.hashCode());
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
		JsonDocumentWrapper other = (JsonDocumentWrapper) obj;
		if (documentID == null) {
			if (other.documentID != null)
				return false;
		} else if (!documentID.equals(other.documentID))
			return false;
		if (tokenList == null) {
			if (other.tokenList != null)
				return false;
		} else if (!tokenList.equals(other.tokenList))
			return false;
		return true;
	}

}

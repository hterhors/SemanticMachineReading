package de.hterhors.semanticmr.corpus.json.wrapper;

public class JsonInstanceWrapper {

	/**
	 * The document.
	 */
	private JsonDocumentWrapper document;

	/**
	 * The corresponding gold annotation.
	 */
	private JsonAnnotationsWrapper goldAnnotations;

	public JsonInstanceWrapper(JsonDocumentWrapper document, JsonAnnotationsWrapper goldAnnotations) {
		super();
		this.document = document;
		this.goldAnnotations = goldAnnotations;
	}

	public JsonDocumentWrapper getDocument() {
		return document;
	}

	public void setDocument(JsonDocumentWrapper document) {
		this.document = document;
	}

	public JsonAnnotationsWrapper getGoldAnnotations() {
		return goldAnnotations;
	}

	public void setGoldAnnotations(JsonAnnotationsWrapper goldAnnotations) {
		this.goldAnnotations = goldAnnotations;
	}

	@Override
	public String toString() {
		return "JsonInstanceWrapper [document=" + document + ", goldAnnotations=" + goldAnnotations + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((document == null) ? 0 : document.hashCode());
		result = prime * result + ((goldAnnotations == null) ? 0 : goldAnnotations.hashCode());
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
		JsonInstanceWrapper other = (JsonInstanceWrapper) obj;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		if (goldAnnotations == null) {
			if (other.goldAnnotations != null)
				return false;
		} else if (!goldAnnotations.equals(other.goldAnnotations))
			return false;
		return true;
	}

}

package de.hterhors.semanticmr.json.wrapper;

import com.google.gson.annotations.SerializedName;

import de.hterhors.semanticmr.corpus.EInstanceContext;

public class JsonInstanceWrapper {

	/**
	 * The context to which this instance belongs one of TRAIN, DEVELOPMENT, TEST.
	 */
	@SerializedName("context")
	private EInstanceContext context;

	/**
	 * The document.
	 */
	@SerializedName("doc")
	private JsonDocumentWrapper document;

	/**
	 * The corresponding gold annotation.
	 */
	@SerializedName("ganns")
	private JsonAnnotationsWrapper goldAnnotations;

	public JsonInstanceWrapper(EInstanceContext context, JsonDocumentWrapper document,
			JsonAnnotationsWrapper goldAnnotations) {
		super();
		this.context = context;
		this.document = document;
		this.goldAnnotations = goldAnnotations;
	}

	public EInstanceContext getContext() {
		return context;
	}

	public void setContext(EInstanceContext context) {
		this.context = context;
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
		return "JsonInstanceWrapper [context=" + context + ", document=" + document + ", goldAnnotations="
				+ goldAnnotations + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
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
		if (context != other.context)
			return false;
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

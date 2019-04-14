package de.hterhors.semanticmr.corpus.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonRootAnnotationWrapper {

	@SerializedName("dla")
	private JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation;
	@SerializedName("la")
	private JsonLiteralAnnotationWrapper literalAnnotation;
	@SerializedName("ea")
	private JsonEntityTypeWrapper entityTypeAnnotation;

	public JsonDocumentLinkedAnnotationWrapper getDocLinkedAnnotation() {
		return docLinkedAnnotation;
	}

	public void setDocLinkedAnnotation(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation) {
		this.docLinkedAnnotation = docLinkedAnnotation;
	}

	public JsonLiteralAnnotationWrapper getLiteralAnnotation() {
		return literalAnnotation;
	}

	public void setLiteralAnnotation(JsonLiteralAnnotationWrapper literalAnnotation) {
		this.literalAnnotation = literalAnnotation;
	}

	public JsonEntityTypeWrapper getEntityTypeAnnotation() {
		return entityTypeAnnotation;
	}

	public void setEntityTypeAnnotation(JsonEntityTypeWrapper entityTypeAnnotation) {
		this.entityTypeAnnotation = entityTypeAnnotation;
	}

	public JsonRootAnnotationWrapper(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation,
			JsonLiteralAnnotationWrapper literalAnnotation, JsonEntityTypeWrapper entityTypeAnnotation) {
		super();
		this.docLinkedAnnotation = docLinkedAnnotation;
		this.literalAnnotation = literalAnnotation;
		this.entityTypeAnnotation = entityTypeAnnotation;
	}

	public JsonRootAnnotationWrapper(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation) {
		super();
		this.docLinkedAnnotation = docLinkedAnnotation;
		this.literalAnnotation = null;
		this.entityTypeAnnotation = null;
	}

	public JsonRootAnnotationWrapper(JsonLiteralAnnotationWrapper literalAnnotation) {
		super();
		this.docLinkedAnnotation = null;
		this.literalAnnotation = literalAnnotation;
		this.entityTypeAnnotation = null;
	}

	public JsonRootAnnotationWrapper(JsonEntityTypeWrapper entityTypeAnnotation) {
		super();
		this.docLinkedAnnotation = null;
		this.literalAnnotation = null;
		this.entityTypeAnnotation = entityTypeAnnotation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docLinkedAnnotation == null) ? 0 : docLinkedAnnotation.hashCode());
		result = prime * result + ((entityTypeAnnotation == null) ? 0 : entityTypeAnnotation.hashCode());
		result = prime * result + ((literalAnnotation == null) ? 0 : literalAnnotation.hashCode());
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
		JsonRootAnnotationWrapper other = (JsonRootAnnotationWrapper) obj;
		if (docLinkedAnnotation == null) {
			if (other.docLinkedAnnotation != null)
				return false;
		} else if (!docLinkedAnnotation.equals(other.docLinkedAnnotation))
			return false;
		if (entityTypeAnnotation == null) {
			if (other.entityTypeAnnotation != null)
				return false;
		} else if (!entityTypeAnnotation.equals(other.entityTypeAnnotation))
			return false;
		if (literalAnnotation == null) {
			if (other.literalAnnotation != null)
				return false;
		} else if (!literalAnnotation.equals(other.literalAnnotation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonRootAnnotationWrapper [docLinkedAnnotation=" + docLinkedAnnotation + ", literalAnnotation="
				+ literalAnnotation + ", entityTypeAnnotation=" + entityTypeAnnotation + "]";
	}

}

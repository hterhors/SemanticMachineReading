package de.hterhors.semanticmr.corpus.json.wrapper;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class JsonAnnotationsWrapper {

	@SerializedName("dla")
	private List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations;
	@SerializedName("la")
	private List<JsonLiteralAnnotationWrapper> literalAnnotations;
	@SerializedName("ea")
	private List<JsonEntityTypeWrapper> entityTypeAnnotations;
	@SerializedName("eta")
	private List<JsonEntityTemplateWrapper> entityTemplateAnnotations;

	public JsonAnnotationsWrapper(List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations,
			List<JsonLiteralAnnotationWrapper> literalAnnotations, List<JsonEntityTypeWrapper> entityTypeAnnotations,
			List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {
		this.docLinkedAnnotations = docLinkedAnnotations;
		this.literalAnnotations = literalAnnotations;
		this.entityTypeAnnotations = entityTypeAnnotations;
		this.entityTemplateAnnotations = entityTemplateAnnotations;
	}

	public List<JsonDocumentLinkedAnnotationWrapper> getDocLinkedAnnotations() {
		return docLinkedAnnotations;
	}

	public void setDocLinkedAnnotations(List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations) {
		this.docLinkedAnnotations = docLinkedAnnotations;
	}

	public List<JsonLiteralAnnotationWrapper> getLiteralAnnotations() {
		return literalAnnotations;
	}

	public void setLiteralAnnotations(List<JsonLiteralAnnotationWrapper> literalAnnotations) {
		this.literalAnnotations = literalAnnotations;
	}

	public List<JsonEntityTypeWrapper> getEntityTypeAnnotations() {
		return entityTypeAnnotations;
	}

	public void setEntityTypeAnnotations(List<JsonEntityTypeWrapper> entityTypeAnnotations) {
		this.entityTypeAnnotations = entityTypeAnnotations;
	}

	public List<JsonEntityTemplateWrapper> getEntityTemplateAnnotations() {
		return entityTemplateAnnotations;
	}

	public void setEntityTemplateAnnotations(List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {
		this.entityTemplateAnnotations = entityTemplateAnnotations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docLinkedAnnotations == null) ? 0 : docLinkedAnnotations.hashCode());
		result = prime * result + ((entityTypeAnnotations == null) ? 0 : entityTypeAnnotations.hashCode());
		result = prime * result + ((entityTemplateAnnotations == null) ? 0 : entityTemplateAnnotations.hashCode());
		result = prime * result + ((literalAnnotations == null) ? 0 : literalAnnotations.hashCode());
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
		JsonAnnotationsWrapper other = (JsonAnnotationsWrapper) obj;
		if (docLinkedAnnotations == null) {
			if (other.docLinkedAnnotations != null)
				return false;
		} else if (!docLinkedAnnotations.equals(other.docLinkedAnnotations))
			return false;
		if (entityTypeAnnotations == null) {
			if (other.entityTypeAnnotations != null)
				return false;
		} else if (!entityTypeAnnotations.equals(other.entityTypeAnnotations))
			return false;
		if (entityTemplateAnnotations == null) {
			if (other.entityTemplateAnnotations != null)
				return false;
		} else if (!entityTemplateAnnotations.equals(other.entityTemplateAnnotations))
			return false;
		if (literalAnnotations == null) {
			if (other.literalAnnotations != null)
				return false;
		} else if (!literalAnnotations.equals(other.literalAnnotations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonAnnotationsWrapper [docLinkedAnnotations=" + docLinkedAnnotations + ", literalAnnotations="
				+ literalAnnotations + ", entityTypeAnnotations=" + entityTypeAnnotations
				+ ", entityTemplateAnnotations=" + entityTemplateAnnotations + "]";
	}

}

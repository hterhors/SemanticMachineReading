package de.hterhors.semanticmr.corpus.json.wrapper;

import java.util.List;

public class JsonMultiFillerSlotWrapper {

	private List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations;
	private List<JsonLiteralAnnotationWrapper> literalAnnotations;
	private List<JsonEntityTypeWrapper> entityTypeAnnotations;
	private List<JsonEntityTemplateWrapper> entityTemplateAnnotations;

	public JsonMultiFillerSlotWrapper(List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations,
			List<JsonLiteralAnnotationWrapper> literalAnnotations, List<JsonEntityTypeWrapper> entityTypeAnnotations,
			List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {
		super();
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
		JsonMultiFillerSlotWrapper other = (JsonMultiFillerSlotWrapper) obj;
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
		return "JsonMultiFillerSlotWrapper [docLinkedAnnotations=" + docLinkedAnnotations + ", literalAnnotations="
				+ literalAnnotations + ", enittyTypeAnnotations=" + entityTypeAnnotations
				+ ", entityTemplateAnnotations=" + entityTemplateAnnotations + "]";
	}

}

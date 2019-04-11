package de.hterhors.semanticmr.corpus.json.wrapper;

import java.util.List;

public class JsonMultiFillerSlotWrapper {

	private List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations;
	private List<JsonLiteralAnnotationWrapper> literalAnnotations;
	private List<JsonEntityTypeWrapper> enittyTypeAnnotations;
	private List<JsonEntityTemplateWrapper> entityTemplateAnnotations;

	public JsonMultiFillerSlotWrapper(List<JsonDocumentLinkedAnnotationWrapper> docLinkedAnnotations,
			List<JsonLiteralAnnotationWrapper> literalAnnotations, List<JsonEntityTypeWrapper> enittyTypeAnnotations,
			List<JsonEntityTemplateWrapper> entityTemplateAnnotations) {
		super();
		this.docLinkedAnnotations = docLinkedAnnotations;
		this.literalAnnotations = literalAnnotations;
		this.enittyTypeAnnotations = enittyTypeAnnotations;
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

	public List<JsonEntityTypeWrapper> getEnittyTypeAnnotations() {
		return enittyTypeAnnotations;
	}

	public void setEnittyTypeAnnotations(List<JsonEntityTypeWrapper> enittyTypeAnnotations) {
		this.enittyTypeAnnotations = enittyTypeAnnotations;
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
		result = prime * result + ((enittyTypeAnnotations == null) ? 0 : enittyTypeAnnotations.hashCode());
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
		if (enittyTypeAnnotations == null) {
			if (other.enittyTypeAnnotations != null)
				return false;
		} else if (!enittyTypeAnnotations.equals(other.enittyTypeAnnotations))
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
				+ literalAnnotations + ", enittyTypeAnnotations=" + enittyTypeAnnotations
				+ ", entityTemplateAnnotations=" + entityTemplateAnnotations + "]";
	}

}

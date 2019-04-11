package de.hterhors.semanticmr.corpus.json.wrapper;

public class JsonSingleFillerSlotWrapper {

	private JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation;
	private JsonLiteralAnnotationWrapper literalAnnotation;
	private JsonEntityTypeWrapper entityTypeAnnotation;
	private JsonEntityTemplateWrapper entityTemplateAnnotation;

	public JsonSingleFillerSlotWrapper(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation,
			JsonLiteralAnnotationWrapper literalAnnotation, JsonEntityTypeWrapper enittyTypeAnnotation,
			JsonEntityTemplateWrapper entityTemplateAnnotation) {
		this.docLinkedAnnotation = docLinkedAnnotation;
		this.literalAnnotation = literalAnnotation;
		this.entityTypeAnnotation = enittyTypeAnnotation;
		this.entityTemplateAnnotation = entityTemplateAnnotation;
		checkCondition();
	}

	private void checkCondition() {
		int countNumberOfAnnotations = 0;
		countNumberOfAnnotations += this.docLinkedAnnotation == null ? 0 : 1;
		countNumberOfAnnotations += this.literalAnnotation == null ? 0 : 1;
		countNumberOfAnnotations += this.entityTypeAnnotation == null ? 0 : 1;
		countNumberOfAnnotations += this.entityTemplateAnnotation == null ? 0 : 1;
		if (countNumberOfAnnotations != 1)
			throw new IllegalStateException("Can not put more than one annotation into SingleFillerSlot object!");
	}

	public JsonSingleFillerSlotWrapper(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation) {
		this.docLinkedAnnotation = docLinkedAnnotation;
		this.literalAnnotation = null;
		this.entityTypeAnnotation = null;
		this.entityTemplateAnnotation = null;
	}

	public JsonSingleFillerSlotWrapper(JsonLiteralAnnotationWrapper literalAnnotation) {
		this.docLinkedAnnotation = null;
		this.literalAnnotation = literalAnnotation;
		this.entityTypeAnnotation = null;
		this.entityTemplateAnnotation = null;
	}

	public JsonSingleFillerSlotWrapper(JsonEntityTypeWrapper enittyTypeAnnotation) {
		this.docLinkedAnnotation = null;
		this.literalAnnotation = null;
		this.entityTypeAnnotation = enittyTypeAnnotation;
		this.entityTemplateAnnotation = null;
	}

	public JsonSingleFillerSlotWrapper(JsonEntityTemplateWrapper entityTemplateAnnotation) {
		this.docLinkedAnnotation = null;
		this.literalAnnotation = null;
		this.entityTypeAnnotation = null;
		this.entityTemplateAnnotation = entityTemplateAnnotation;
	}

	public JsonDocumentLinkedAnnotationWrapper getDocLinkedAnnotation() {
		return docLinkedAnnotation;
	}

	public void setDocLinkedAnnotation(JsonDocumentLinkedAnnotationWrapper docLinkedAnnotation) {
		this.docLinkedAnnotation = docLinkedAnnotation;
		checkCondition();
	}

	public JsonLiteralAnnotationWrapper getLiteralAnnotation() {
		return literalAnnotation;
	}

	public void setLiteralAnnotation(JsonLiteralAnnotationWrapper literalAnnotation) {
		this.literalAnnotation = literalAnnotation;
		checkCondition();
	}

	public JsonEntityTypeWrapper getEntityTypeAnnotation() {
		return entityTypeAnnotation;
	}

	public void setEntityTypeAnnotation(JsonEntityTypeWrapper entityTypeAnnotation) {
		this.entityTypeAnnotation = entityTypeAnnotation;
		checkCondition();
	}

	public JsonEntityTemplateWrapper getEntityTemplateAnnotation() {
		return entityTemplateAnnotation;
	}

	public void setEntityTemplateAnnotation(JsonEntityTemplateWrapper entityTemplateAnnotation) {
		this.entityTemplateAnnotation = entityTemplateAnnotation;
		checkCondition();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docLinkedAnnotation == null) ? 0 : docLinkedAnnotation.hashCode());
		result = prime * result + ((entityTypeAnnotation == null) ? 0 : entityTypeAnnotation.hashCode());
		result = prime * result + ((entityTemplateAnnotation == null) ? 0 : entityTemplateAnnotation.hashCode());
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
		JsonSingleFillerSlotWrapper other = (JsonSingleFillerSlotWrapper) obj;
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
		if (entityTemplateAnnotation == null) {
			if (other.entityTemplateAnnotation != null)
				return false;
		} else if (!entityTemplateAnnotation.equals(other.entityTemplateAnnotation))
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
		return "JsonSingleFillerSlotWrapper [docLinkedAnnotation=" + docLinkedAnnotation + ", literalAnnotation="
				+ literalAnnotation + ", enittyTypeAnnotation=" + entityTypeAnnotation + ", entityTemplateAnnotation="
				+ entityTemplateAnnotation + "]";
	}

}

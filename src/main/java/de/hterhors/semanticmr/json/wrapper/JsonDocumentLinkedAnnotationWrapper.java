package de.hterhors.semanticmr.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonDocumentLinkedAnnotationWrapper {
	/**
	 * Contains the textual content of this annotation.
	 */
	@SerializedName("txt")
	private JsonTextualContentWrapper textualContent;
	/**
	 * Defines the entity type of this annotation.
	 */
	@SerializedName("et")
	private JsonEntityTypeWrapper entityType;

	@SerializedName("dp")
	private JsonDocumentPositionWrapper documentPosition;

	public JsonDocumentLinkedAnnotationWrapper(JsonEntityTypeWrapper entityType,
			JsonTextualContentWrapper textualContent, JsonDocumentPositionWrapper documentPosition) {
		this.documentPosition = documentPosition;
		this.textualContent = textualContent;
		this.entityType = entityType;
	}

	public JsonDocumentPositionWrapper getDocumentPosition() {
		return documentPosition;
	}

	public void setDocumentPosition(JsonDocumentPositionWrapper documentPosition) {
		this.documentPosition = documentPosition;
	}

	public JsonTextualContentWrapper getTextualContent() {
		return textualContent;
	}

	public void setTextualContent(JsonTextualContentWrapper textualContent) {
		this.textualContent = textualContent;
	}

	public JsonEntityTypeWrapper getEntityType() {
		return entityType;
	}

	public void setEntityType(JsonEntityTypeWrapper entityType) {
		this.entityType = entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentPosition == null) ? 0 : documentPosition.hashCode());
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((textualContent == null) ? 0 : textualContent.hashCode());
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
		JsonDocumentLinkedAnnotationWrapper other = (JsonDocumentLinkedAnnotationWrapper) obj;
		if (documentPosition == null) {
			if (other.documentPosition != null)
				return false;
		} else if (!documentPosition.equals(other.documentPosition))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (textualContent == null) {
			if (other.textualContent != null)
				return false;
		} else if (!textualContent.equals(other.textualContent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonDocumentLinkedAnnotationWrapper [textualContent=" + textualContent + ", entityType=" + entityType
				+ ", documentPosition=" + documentPosition + "]";
	}

}

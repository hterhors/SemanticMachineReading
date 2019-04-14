package de.hterhors.semanticmr.corpus.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonLiteralAnnotationWrapper {

	/**
	 * Contains the textual content of this annotation.
	 */
	@SerializedName("txtc")
	private JsonTextualContentWrapper textualContent;
	/**
	 * Defines the entity type of this annotation.
	 */
	@SerializedName("et")
	private JsonEntityTypeWrapper entityType;

	public JsonLiteralAnnotationWrapper(JsonEntityTypeWrapper entityType, JsonTextualContentWrapper textualContent) {
		super();
		this.textualContent = textualContent;
		this.entityType = entityType;
	}

	public JsonTextualContentWrapper getTextualContent() {
		return textualContent;
	}

	public void setTextualContent(JsonTextualContentWrapper textualContent) {
		this.textualContent = textualContent;
	}

	@Override
	public String toString() {
		return "JsonLiteralAnnotationWrapper [textualContent=" + textualContent + ", entityType=" + entityType + "]";
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
		JsonLiteralAnnotationWrapper other = (JsonLiteralAnnotationWrapper) obj;
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

}

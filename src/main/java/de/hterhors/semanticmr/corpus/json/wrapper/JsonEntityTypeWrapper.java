package de.hterhors.semanticmr.corpus.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonEntityTypeWrapper {

	@SerializedName("etn")
	private String entityTypeName;

	public JsonEntityTypeWrapper(String entityTypeName) {
		super();
		this.entityTypeName = entityTypeName;
	}

	public String getEntityTypeName() {
		return entityTypeName;
	}

	public void setEntityTypeName(String entityTypeName) {
		this.entityTypeName = entityTypeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityTypeName == null) ? 0 : entityTypeName.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JsonEntityTypeWrapper [entityTypeName=" + entityTypeName + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonEntityTypeWrapper other = (JsonEntityTypeWrapper) obj;
		if (entityTypeName == null) {
			if (other.entityTypeName != null)
				return false;
		} else if (!entityTypeName.equals(other.entityTypeName))
			return false;
		return true;
	}

}

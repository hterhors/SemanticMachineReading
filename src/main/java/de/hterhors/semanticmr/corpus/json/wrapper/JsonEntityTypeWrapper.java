package de.hterhors.semanticmr.corpus.json.wrapper;

public class JsonEntityTypeWrapper   {

	private String entityType;

	public JsonEntityTypeWrapper(String entityType) {
		super();
		this.entityType = entityType;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JsonEntityTypeWrapper [entityType=" + entityType + "]";
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
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		return true;
	}

}

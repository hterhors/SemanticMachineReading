package de.hterhors.semanticmr.corpus.json.wrapper;

import java.util.Map;

public class JsonEntityTemplateWrapper {

	private JsonRootAnnotationWrapper rootAnnotation;

	/**
	 * An unmodifiable map of slots to fill.
	 */
	private Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots;

	/**
	 * An unmodifiable map of slots to fill.
	 */
	private Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots;

	public JsonEntityTemplateWrapper(JsonRootAnnotationWrapper entityType,
			Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots,
			Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots) {
		super();
		this.rootAnnotation = entityType;
		this.singleFillerSlots = singleFillerSlots;
		this.multiFillerSlots = multiFillerSlots;
	}

	@Override
	public String toString() {
		return "JsonEntityTemplateWrapper [entityType=" + rootAnnotation + ", singleFillerSlots=" + singleFillerSlots
				+ ", multiFillerSlots=" + multiFillerSlots + "]";
	}

	public JsonRootAnnotationWrapper getRootAnnotation() {
		return rootAnnotation;
	}

	public void setRootAnnotation(JsonRootAnnotationWrapper rootAnnotation) {
		this.rootAnnotation = rootAnnotation;
	}

	public Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> getSingleFillerSlots() {
		return singleFillerSlots;
	}

	public void setSingleFillerSlots(Map<JsonSlotTypeWrapper, JsonSingleFillerSlotWrapper> singleFillerSlots) {
		this.singleFillerSlots = singleFillerSlots;
	}

	public Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> getMultiFillerSlots() {
		return multiFillerSlots;
	}

	public void setMultiFillerSlots(Map<JsonSlotTypeWrapper, JsonMultiFillerSlotWrapper> multiFillerSlots) {
		this.multiFillerSlots = multiFillerSlots;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootAnnotation == null) ? 0 : rootAnnotation.hashCode());
		result = prime * result + ((multiFillerSlots == null) ? 0 : multiFillerSlots.hashCode());
		result = prime * result + ((singleFillerSlots == null) ? 0 : singleFillerSlots.hashCode());
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
		JsonEntityTemplateWrapper other = (JsonEntityTemplateWrapper) obj;
		if (rootAnnotation == null) {
			if (other.rootAnnotation != null)
				return false;
		} else if (!rootAnnotation.equals(other.rootAnnotation))
			return false;
		if (multiFillerSlots == null) {
			if (other.multiFillerSlots != null)
				return false;
		} else if (!multiFillerSlots.equals(other.multiFillerSlots))
			return false;
		if (singleFillerSlots == null) {
			if (other.singleFillerSlots != null)
				return false;
		} else if (!singleFillerSlots.equals(other.singleFillerSlots))
			return false;
		return true;
	}

}

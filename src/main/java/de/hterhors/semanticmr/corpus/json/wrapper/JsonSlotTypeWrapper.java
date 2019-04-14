package de.hterhors.semanticmr.corpus.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonSlotTypeWrapper {

	@SerializedName("stn")
	private String slotTypeName;

	public JsonSlotTypeWrapper(String slotTypeName) {
		super();
		this.slotTypeName = slotTypeName;
	}

	public String getSlotTypeName() {
		return slotTypeName;
	}

	public void setSlotTypeName(String slotTypeName) {
		this.slotTypeName = slotTypeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slotTypeName == null) ? 0 : slotTypeName.hashCode());
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
		JsonSlotTypeWrapper other = (JsonSlotTypeWrapper) obj;
		if (slotTypeName == null) {
			if (other.slotTypeName != null)
				return false;
		} else if (!slotTypeName.equals(other.slotTypeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonSlotTypeWrapper [slotTypeName=" + slotTypeName + "]";
	}

}

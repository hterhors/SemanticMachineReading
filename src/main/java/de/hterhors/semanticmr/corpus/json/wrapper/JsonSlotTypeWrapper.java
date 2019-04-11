package de.hterhors.semanticmr.corpus.json.wrapper;

public class JsonSlotTypeWrapper {

	private String slotType;

	public JsonSlotTypeWrapper(String slotType) {
		super();
		this.slotType = slotType;
	}

	public String getSlotType() {
		return slotType;
	}

	public void setSlotType(String slotType) {
		this.slotType = slotType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slotType == null) ? 0 : slotType.hashCode());
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
		if (slotType == null) {
			if (other.slotType != null)
				return false;
		} else if (!slotType.equals(other.slotType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonSlotTypeWrapper [slotType=" + slotType + "]";
	}

}

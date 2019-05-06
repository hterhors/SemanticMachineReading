package de.hterhors.semanticmr.init.reader.json.structures;

public class SpecsSlotTypeWrapper {

	private String entityName;
	private String slotName;
	private String slotFillerEntityName;
	private boolean isLiteral;
	private ESlotType slotType;
	private int maxNumberOfSlotFiller;

	public enum ESlotType {
		Single, Multi;
	}

	public SpecsSlotTypeWrapper(String entityName, String slotName, String slotFillerEntityName, boolean isLiteral,
			ESlotType slotType, int maxNumberOfSlotFiller) {
		super();
		this.entityName = entityName;
		this.slotName = slotName;
		this.slotFillerEntityName = slotFillerEntityName;
		this.isLiteral = isLiteral;
		this.slotType = slotType;
		this.maxNumberOfSlotFiller = maxNumberOfSlotFiller;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getSlotName() {
		return slotName;
	}

	public void setSlotName(String slotName) {
		this.slotName = slotName;
	}

	public String getSlotFillerEntityName() {
		return slotFillerEntityName;
	}

	public void setSlotFillerEntityName(String slotFillerEntityName) {
		this.slotFillerEntityName = slotFillerEntityName;
	}

	public boolean isLiteral() {
		return isLiteral;
	}

	public void setLiteral(boolean isLiteral) {
		this.isLiteral = isLiteral;
	}

	public ESlotType getSlotType() {
		return slotType;
	}

	public void setSlotType(ESlotType slotType) {
		this.slotType = slotType;
	}

	public int getMaxNumberOfSlotFiller() {
		return maxNumberOfSlotFiller;
	}

	public void setMaxNumberOfSlotFiller(int maxNumberOfSlotFiller) {
		this.maxNumberOfSlotFiller = maxNumberOfSlotFiller;
	}

}

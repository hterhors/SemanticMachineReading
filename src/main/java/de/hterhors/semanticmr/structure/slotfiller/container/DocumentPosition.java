package de.hterhors.semanticmr.structure.slotfiller.container;

/**
 * Contains information of the document position of an annotation.
 * 
 * @author hterhors
 *
 */
public class DocumentPosition {

	public static final DocumentPosition EMPTY_INSTANCE = new DocumentPosition(-1);

	/**
	 * The character offset position of its annotation.
	 */
	final public int charOffset;

	public DocumentPosition(int charOffset) {
		this.charOffset = charOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + charOffset;
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
		DocumentPosition other = (DocumentPosition) obj;
		if (charOffset != other.charOffset)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DocumentPosition [charOffset=" + charOffset + "]";
	}

	public DocumentPosition deepCopy() {
		if (this == EMPTY_INSTANCE)
			return EMPTY_INSTANCE;
		return new DocumentPosition(charOffset);
	}

	public String toPrettyString() {
		if (this == EMPTY_INSTANCE) {
			return "";
		}
		return String.valueOf(charOffset);
	}

}

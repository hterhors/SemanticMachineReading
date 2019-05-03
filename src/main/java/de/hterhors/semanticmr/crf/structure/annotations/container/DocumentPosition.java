package de.hterhors.semanticmr.crf.structure.annotations.container;

/**
 * Contains information of the document position of an annotation.
 * 
 * @author hterhors
 *
 */
public class DocumentPosition {

	/**
	 * The character offset position of its annotation.
	 */
	final public int docCharOffset;

	public DocumentPosition(int charOffset) {
		this.docCharOffset = charOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + docCharOffset;
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
		if (docCharOffset != other.docCharOffset)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DocumentPosition [charOffset=" + docCharOffset + "]";
	}

	public DocumentPosition deepCopy() {
		return new DocumentPosition(docCharOffset);
	}

	public String toPrettyString() {
		return String.valueOf(docCharOffset);
	}

}

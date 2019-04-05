package de.hterhors.semanticmr.structure.slotfiller;

import de.hterhors.semanticmr.structure.slotfiller.container.DocumentPosition;
import de.hterhors.semanticmr.structure.slotfiller.container.TextualContent;

/**
 * Annotation object for entity-type based slots that are linked to the
 * document.
 * 
 * @author hterhors
 *
 */
final public class DocumentLink extends Literal {

	/**
	 * Contains the document position of this annotation.
	 */
	public final DocumentPosition documentPosition;

	public DocumentLink(EntityType entityType, TextualContent textualContent, DocumentPosition documentPosition) {
		super(entityType, textualContent);
		this.documentPosition = documentPosition;
	}

	@Override
	public String toString() {
		return "DocumentLinkedSlotFiller [documentPosition=" + documentPosition + "]";
	}

	@Override
	public DocumentLink deepCopy() {
		return new DocumentLink(getEntityType().deepCopy(), textualContent.deepCopy(), documentPosition.deepCopy());
	}

	public String toPrettyString(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append(getEntityType().toPrettyString());
		sb.append("\t");
		sb.append(textualContent.toPrettyString());
		sb.append("\t");
		sb.append(documentPosition.toPrettyString());
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((documentPosition == null) ? 0 : documentPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentLink other = (DocumentLink) obj;
		if (documentPosition == null) {
			if (other.documentPosition != null)
				return false;
		} else if (!documentPosition.equals(other.documentPosition))
			return false;
		return true;
	}

	@Override
	public Score compare(Literal otherVal) {
		if (otherVal == null) {
			return Score.FN;
		} else if (equals(otherVal)) {
			return Score.CORRECT;
		} else {
			return Score.INCORRECT;
		}
	}
}

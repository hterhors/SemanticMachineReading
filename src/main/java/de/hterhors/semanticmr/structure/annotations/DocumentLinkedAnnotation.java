package de.hterhors.semanticmr.structure.annotations;

import de.hterhors.semanticmr.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.structure.annotations.container.TextualContent;

/**
 * Annotation object for entity-type based slots that are linked to the
 * document.
 * 
 * @author hterhors
 *
 */
final public class DocumentLinkedAnnotation extends LiteralAnnotation {

	/**
	 * Contains the document position of this annotation.
	 */
	public final DocumentPosition documentPosition;

	public DocumentLinkedAnnotation(EntityType entityType, TextualContent textualContent,
			DocumentPosition documentPosition) {
		super(entityType, textualContent);
		this.documentPosition = documentPosition;
	}

	@Override
	public String toString() {
		return "DocumentLinkedSlotFiller [documentPosition=" + documentPosition + "]";
	}

	@Override
	public DocumentLinkedAnnotation deepCopy() {
		return new DocumentLinkedAnnotation(getEntityType().deepCopy(), textualContent.deepCopy(),
				documentPosition.deepCopy());
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
		DocumentLinkedAnnotation other = (DocumentLinkedAnnotation) obj;
		if (documentPosition == null) {
			if (other.documentPosition != null)
				return false;
		} else if (!documentPosition.equals(other.documentPosition))
			return false;
		return true;
	}

	@Override
	public Score evaluate(LiteralAnnotation otherVal) {

		if (otherVal == null) {
			return Score.FN;
		} else if (equals(otherVal)) {
			return Score.TP;
		} else {
			return Score.FN_FP;
		}
	}
}

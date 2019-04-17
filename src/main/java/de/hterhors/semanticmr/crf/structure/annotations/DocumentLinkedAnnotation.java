package de.hterhors.semanticmr.crf.structure.annotations;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.eval.EEvaluationMode;

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
		return new DocumentLinkedAnnotation(entityType, textualContent.deepCopy(), documentPosition.deepCopy());
	}

	public String toPrettyString(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toPrettyString(depth));
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
		return equalsEval(obj);
	}

	final private boolean equalsEval(Object obj) {
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
	public Score evaluate(EEvaluationMode mode, EntityTypeAnnotation otherVal) {
		if (otherVal == null) {
			return Score.FN;
		} else {
			switch (mode) {
			case DOCUMENT_LINKED:
				if (equalsEval(otherVal)) {
					return Score.TP;
				} else {
					return Score.FN_FP;
				}
			case LITERAL:
			case ENTITY_TYPE:
				return super.evaluate(mode, otherVal);
			}
		}
		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + mode);
	}
}

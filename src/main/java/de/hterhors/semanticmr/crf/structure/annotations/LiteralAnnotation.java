package de.hterhors.semanticmr.crf.structure.annotations;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.eval.EEvaluationMode;

/**
 * Annotation object for literal based slots that are NOT linked to the
 * document.
 * 
 * @author hterhors
 *
 */
public class LiteralAnnotation extends EntityTypeAnnotation {

	/**
	 * Contains the textual content of this annotation.
	 */
	public final TextualContent textualContent;

	public LiteralAnnotation(EntityType entityType, TextualContent textualContent) {
		super(entityType);
		this.textualContent = textualContent;
		this.textualContent.normalize(entityType.getNormalizationFunction());
	}

	@Override
	public String toString() {
		return "LiteralSlotFiller [textualContent=" + textualContent + "]";
	}

	@Override
	public LiteralAnnotation deepCopy() {
		return new LiteralAnnotation(entityType, textualContent.deepCopy());
	}

	public String toPrettyString(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toPrettyString(depth));
		sb.append("\t");
		sb.append(textualContent.toPrettyString());
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((textualContent == null) ? 0 : textualContent.hashCode());
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
		LiteralAnnotation other = (LiteralAnnotation) obj;
		if (textualContent == null) {
			if (other.textualContent != null)
				return false;
		} else if (!textualContent.equals(other.textualContent))
			return false;
		return true;
	}

	final private boolean equalsEvalLA(Object obj) {
		if (this == obj)
			return true;
		if (!super.equalsEvalETA(obj))
			return false;
		if (!getClass().isAssignableFrom(obj.getClass()))
			return false;
		LiteralAnnotation other = (LiteralAnnotation) obj;
		if (textualContent == null) {
			if (other.textualContent != null)
				return false;
		} else if (!textualContent.equals(other.textualContent))
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
				if (equals(otherVal))
					return Score.TP;
				return Score.FN_FP;
			case LITERAL:
				if (getClass() == otherVal.getClass()) {
					if (getClass() != LiteralAnnotation.class)
						return equalsEvalLA(otherVal) ? Score.TP : Score.FN_FP;
					else
						return equals(otherVal) ? Score.TP : Score.FN_FP;
				} else {
					if ((this.getClass() == DocumentLinkedAnnotation.class))
						if (otherVal.getClass() == LiteralAnnotation.class)
							return ((LiteralAnnotation) otherVal).equalsEvalLA(this) ? Score.TP : Score.FN_FP;
						else
							return Score.FN_FP;

					else if ((otherVal.getClass() == DocumentLinkedAnnotation.class))
						if (getClass() == LiteralAnnotation.class)
							return ((LiteralAnnotation) this).equalsEvalLA(otherVal) ? Score.TP : Score.FN_FP;
						else
							return Score.FN_FP;
				}
				return Score.FN_FP;

			case ENTITY_TYPE:
				return super.evaluate(mode, otherVal);
			}
		}
		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + mode);
	}

	public String getSurfaceForm() {
		return textualContent.surfaceForm;
	}

	public String getNormalizedSurfaceForm() {
		return textualContent.normalizedSurfaceForm;
	}
}

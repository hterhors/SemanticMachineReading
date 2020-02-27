package de.hterhors.semanticmr.crf.structure.annotations;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

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
		return "LiteralAnnotation [textualContent=" + textualContent + ", toString()=" + super.toString() + "]";
	}

	@Override
	public LiteralAnnotation deepCopy() {
		return new LiteralAnnotation(entityType, textualContent.deepCopy());
	}

	@Override
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
	public Score evaluate(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		if (otherVal == null)
			return Score.FN;
		if (entityType.isLiteral || evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED
				|| evaluationDetail == EEvaluationDetail.LITERAL) {
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
		} else if (evaluationDetail == EEvaluationDetail.ENTITY_TYPE) {
			return super.evaluate(evaluationDetail, otherVal);
		}

		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + evaluationDetail);
	}

	final public String getCleanedSurfaceForm() {
		return textualContent.getCleanedSurfaceForm();
	}

	final public String getSurfaceForm() {
		return textualContent.surfaceForm;
	}

	final public String getNormalizedSurfaceForm() {
		return textualContent.getNormalizedSurfaceForm();
	}

	final public int getSurfaceFormAsInt() {
		return Integer.parseInt(textualContent.surfaceForm);
	}

	@Override
	public boolean evaluateEquals(EEvaluationDetail evaluationDetail, IEvaluatable otherVal) {
		return evaluate(evaluationDetail, otherVal).getF1() == 1.0D;
	}
}

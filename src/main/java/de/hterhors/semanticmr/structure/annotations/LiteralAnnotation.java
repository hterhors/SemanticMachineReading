package de.hterhors.semanticmr.structure.annotations;

import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.structure.annotations.container.TextualContent;

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

	@Override
	public Score evaluate(EntityTypeAnnotation otherVal) {
		if (otherVal == null) {
			return Score.FN;
		} else if (equals(otherVal)) {
			return Score.TP;
		} else {
			return Score.FN_FP;
		}
	}
}

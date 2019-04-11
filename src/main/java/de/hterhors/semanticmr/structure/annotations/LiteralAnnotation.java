package de.hterhors.semanticmr.structure.annotations;

import de.hterhors.semanticmr.structure.annotations.container.TextualContent;

/**
 * Annotation object for literal based slots that are NOT linked to the
 * document.
 * 
 * @author hterhors
 *
 */
public class LiteralAnnotation extends AbstractSlotFiller<LiteralAnnotation> {

	/**
	 * Contains the textual content of this annotation.
	 */
	public final TextualContent textualContent;
	/**
	 * Defines the entity type of this annotation.
	 */
	private final EntityType entityType;

	public LiteralAnnotation(EntityType entityType, TextualContent textualContent) {
		this.entityType = entityType;
		this.textualContent = textualContent;
		this.textualContent.normalize(entityType.getNormalizationFunction());
	}

	@Override
	public String toString() {
		return "LiteralSlotFiller [textualContent=" + textualContent + "]";
	}

	@Override
	public LiteralAnnotation deepCopy() {
		return new LiteralAnnotation(entityType.deepCopy(), textualContent.deepCopy());
	}

	public String toPrettyString(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append(entityType.toPrettyString());
		sb.append("\t");
		sb.append(textualContent.toPrettyString());
		return sb.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((textualContent == null) ? 0 : textualContent.hashCode());
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
		LiteralAnnotation other = (LiteralAnnotation) obj;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (entityType != other.entityType)
			return false;
		if (textualContent == null) {
			if (other.textualContent != null)
				return false;
		} else if (!textualContent.equals(other.textualContent))
			return false;
		return true;
	}

	@Override
	public EntityType getEntityType() {
		return entityType;
	}

	@Override
	public Score evaluate(LiteralAnnotation otherVal) {

		if (otherVal == null) {
			return Score.FN;
		} else if (equals(otherVal)) {
			return Score.CORRECT;
		} else {
			return Score.INCORRECT;
		}
	}

}

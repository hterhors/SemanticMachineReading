package de.hterhors.semanticmr.structure.slotfiller.container;

import de.hterhors.semanticmr.structure.slotfiller.normalization.INormalizationFunction;

/**
 * 
 * Contains information of the textual content of an annotation.
 * 
 * @author hterhors
 *
 */
public class TextualContent {

	public static final TextualContent EMPTY_INSTANCE = new TextualContent(null, null);

	/**
	 * The textual content of an annotation.
	 */
	final public String surfaceForm;

	/**
	 * Normalized literal.
	 */
	public String normalizedSurfaceForm;

	public TextualContent(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param surfaceForm
	 * @param normalizedSurfaceForm
	 */
	private TextualContent(String surfaceForm, String normalizedSurfaceForm) {
		this.surfaceForm = surfaceForm;
		this.normalizedSurfaceForm = normalizedSurfaceForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((normalizedSurfaceForm == null) ? 0 : normalizedSurfaceForm.hashCode());
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
		TextualContent other = (TextualContent) obj;
		if (normalizedSurfaceForm == null) {
			if (other.normalizedSurfaceForm != null)
				return false;
		} else if (!normalizedSurfaceForm.equals(other.normalizedSurfaceForm))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TextualContent [surfaceForm=" + surfaceForm + ", normalizedSurfaceForm=" + normalizedSurfaceForm + "]";
	}

	public void normalize(INormalizationFunction iNormalizationFunction) {
		this.normalizedSurfaceForm = iNormalizationFunction.normalize(this.surfaceForm);
	}

	public TextualContent deepCopy() {
		if (this == EMPTY_INSTANCE)
			return EMPTY_INSTANCE;
		return new TextualContent(surfaceForm, normalizedSurfaceForm);
	}

	public String toPrettyString() {
		if (this == EMPTY_INSTANCE) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("\"");
		sb.append(this.surfaceForm);
		sb.append("\"");
		sb.append("\t(\"").append(normalizedSurfaceForm).append("\")");
		return sb.toString().toString();
	}

}

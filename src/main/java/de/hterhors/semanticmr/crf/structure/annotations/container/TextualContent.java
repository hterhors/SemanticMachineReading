package de.hterhors.semanticmr.crf.structure.annotations.container;

import de.hterhors.semanticmr.crf.structure.annotations.normalization.INormalizationFunction;

/**
 * 
 * Contains information of the textual content of an annotation.
 * 
 * @author hterhors
 *
 */
public class TextualContent {

	/**
	 * The textual content of an annotation.
	 */
	final public String surfaceForm;

	/**
	 * Normalized literal.
	 */
	public String normalizedSurfaceForm;

	final public String cleanedSurfaceForm;

	public TextualContent(String surfaceForm) {
		this.surfaceForm = surfaceForm;
		this.cleanedSurfaceForm = cleanSurfaceForm(this.surfaceForm);
	}

	private String cleanSurfaceForm(String textMention) {
		return textMention.replaceAll("[0-9]", "#").replaceAll("[^\\x20-\\x7E]+", "§");
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param surfaceForm
	 * @param normalizedSurfaceForm
	 */
	private TextualContent(String surfaceForm, String normalizedSurfaceForm, String cleanedSurfaceForm) {
		this.surfaceForm = surfaceForm;
		this.normalizedSurfaceForm = normalizedSurfaceForm;
		this.cleanedSurfaceForm = cleanedSurfaceForm;
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
		return new TextualContent(surfaceForm, normalizedSurfaceForm, cleanedSurfaceForm);
	}

	public String toPrettyString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\"");
		sb.append(this.surfaceForm);
		sb.append("\"");
		sb.append("\t(\"").append(normalizedSurfaceForm).append("\")");
		return sb.toString().toString();
	}

}

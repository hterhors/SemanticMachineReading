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
	transient public String normalizedSurfaceForm;

	transient final public String cleanedSurfaceForm;

	transient private Boolean isNormalized = null;

	public TextualContent(String surfaceForm) {
		this.surfaceForm = surfaceForm;
		this.cleanedSurfaceForm = cleanSurfaceForm(this.surfaceForm);
	}

	/**
	 * Deep copy constructor.
	 * 
	 * @param surfaceForm
	 * @param normalizedSurfaceForm
	 */
	private TextualContent(String surfaceForm, String cleanedSurfaceForm, String normalizedSurfaceForm) {
		this.surfaceForm = surfaceForm;
		this.cleanedSurfaceForm = cleanedSurfaceForm;
		this.normalizedSurfaceForm = normalizedSurfaceForm;
	}

	private String cleanSurfaceForm(String textMention) {
		return textMention.replaceAll("[0-9]", "#").replaceAll("[^\\x20-\\x7E]+", "§");
	}

	public void normalize(String normalizedSurfaceForm) {
		this.normalizedSurfaceForm = normalizedSurfaceForm;
	}

	public TextualContent deepCopy() {
		return new TextualContent(surfaceForm, cleanedSurfaceForm, normalizedSurfaceForm);
	}

	public String toPrettyString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\"");
		sb.append(this.surfaceForm);
		sb.append("\"");
		if (normalizedIsDifferent()) {
			sb.append("\t(\"").append(getNormalizedSurfaceForm()).append("\")");
		}
		return sb.toString().toString();
	}

	private boolean normalizedIsDifferent() {
		if (isNormalized == null)
			isNormalized = new Boolean(normalizedSurfaceForm != this.surfaceForm);
		return isNormalized.booleanValue();
	}

	public String getNormalizedSurfaceForm() {
		return normalizedSurfaceForm;
	}

	@Override
	public int hashCode() {
		if (normalizedIsDifferent()) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((normalizedSurfaceForm == null) ? 0 : normalizedSurfaceForm.hashCode());
			return result;
		} else {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
			return result;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (normalizedIsDifferent()) {
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
		} else {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TextualContent other = (TextualContent) obj;
			if (surfaceForm == null) {
				if (other.surfaceForm != null)
					return false;
			} else if (!surfaceForm.equals(other.surfaceForm))
				return false;
			return true;
		}
	}

	public void normalize(INormalizationFunction normalizationFunction) {
		this.normalizedSurfaceForm = normalizationFunction.interprete(this.surfaceForm);
	}

}

package de.hterhors.semanticmr.corpus.json.wrapper;

import com.google.gson.annotations.SerializedName;

public class JsonTextualContentWrapper {
	/**
	 * The textual content of an annotation.
	 */
	@SerializedName("sff")
	private String surfaceForm;

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	public JsonTextualContentWrapper(String surfaceForm) {
		super();
		this.surfaceForm = surfaceForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
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
		JsonTextualContentWrapper other = (JsonTextualContentWrapper) obj;
		if (surfaceForm == null) {
			if (other.surfaceForm != null)
				return false;
		} else if (!surfaceForm.equals(other.surfaceForm))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JsonTextualContentWrapper [surfaceForm=" + surfaceForm + "]";
	}
}

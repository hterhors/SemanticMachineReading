package de.hterhors.semanticmr.crf.factor;

import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;

public abstract class FactorScope {

	protected final AbstractFactorTemplate template;

	public FactorScope(AbstractFactorTemplate template) {
		this.template = template;
	}

	public AbstractFactorTemplate getTemplate() {
		return template;
	}

	/**
	 * Reminder to proper implement hashCode function for all scopes!
	 * 
	 * @return
	 */
	public abstract int getHashCode();

	/**
	 * Reminder to proper implement equals function for all scopes!
	 */
	public abstract boolean getEquals(Object obj);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	/**
	 * NOTE: the template is compared by its class not by its feature weights
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactorScope other = (FactorScope) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (template.getClass() != other.template.getClass())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FactorScope [template=" + template + "]";
	}

}

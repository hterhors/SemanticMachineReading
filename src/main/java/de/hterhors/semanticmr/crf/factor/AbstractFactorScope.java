package de.hterhors.semanticmr.crf.factor;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;

/**
 * The factor scope is basically a list of variables in the context of a
 * specific template.
 * 
 * The scope is used to compute a feature vector. Both factor scope and feature
 * vector are stored in a factor.
 * 
 * <b>As factor scopes are used as keys in a hash map in the factor pool, it is
 * imperative to proper implement the hashCode and equals - methods!</b>
 * 
 * @author hterhors
 *
 * @see {@link Factor}
 */
public abstract class AbstractFactorScope<S extends AbstractFactorScope<S, A>, A extends AbstractAnnotation> {

	/**
	 * The template context of the list of variables.
	 */
	protected final AbstractFeatureTemplate<S, A> template;

	public AbstractFactorScope(AbstractFeatureTemplate<S, A> template) {
		this.template = template;
	}

	/**
	 * Getter for the template context.
	 * 
	 * @return the template
	 */
	public AbstractFeatureTemplate<S, A> getTemplate() {
		return template;
	}

	/**
	 * Reminder to proper implement hashCode function for all scopes!
	 * 
	 * @return
	 */
	public abstract int implementHashCode();

	/**
	 * Reminder to proper implement equals function for all scopes!
	 */
	public abstract boolean implementEquals(Object obj);

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
		AbstractFactorScope<S, A> other = (AbstractFactorScope<S, A>) obj;
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

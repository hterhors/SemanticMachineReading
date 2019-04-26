package de.hterhors.semanticmr.crf.templates.nerla;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.MorphologicalNerlaTemplate.MorphologicalNerlaScope;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class MorphologicalNerlaTemplate
		extends AbstractFeatureTemplate<MorphologicalNerlaScope, DocumentLinkedAnnotation> {

	static class MorphologicalNerlaScope
			extends AbstractFactorScope<MorphologicalNerlaScope, DocumentLinkedAnnotation> {

		final public EntityType type;
		final public String surfaceForm;

		public MorphologicalNerlaScope(
				AbstractFeatureTemplate<MorphologicalNerlaScope, DocumentLinkedAnnotation> template, EntityType type,
				String surfaceForm) {
			super(template);
			this.type = type;
			this.surfaceForm = surfaceForm;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			MorphologicalNerlaScope other = (MorphologicalNerlaScope) obj;
			if (surfaceForm == null) {
				if (other.surfaceForm != null)
					return false;
			} else if (!surfaceForm.equals(other.surfaceForm))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public int implementHashCode() {
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}

	}

	@Override
	public List<MorphologicalNerlaScope> generateFactorScopes(State state) {
		List<MorphologicalNerlaScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : getPredictedAnnotations(state)) {

			String surfaceForm = annotation.getSurfaceForm();
			EntityType type = annotation.getEntityType();

			factors.add(new MorphologicalNerlaScope(this, type, surfaceForm));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MorphologicalNerlaScope> factor) {

		String sf = factor.getFactorScope().surfaceForm;
		EntityType type = factor.getFactorScope().type;

		if (sf.isEmpty())
			return;

		factor.getFeatureVector().set("STARTS_WITH_CAPITAL", Character.isUpperCase(sf.charAt(0)));
		factor.getFeatureVector().set("STARTS_WITH_CAPITAL_FOR_TYPE_" + type.entityTypeName,
				Character.isUpperCase(sf.charAt(0)));

	}

}

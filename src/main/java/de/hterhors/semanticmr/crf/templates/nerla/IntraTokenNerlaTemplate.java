package de.hterhors.semanticmr.crf.templates.nerla;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.IntraTokenNerlaTemplate.IntraTokenNerlaScope;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class IntraTokenNerlaTemplate extends AbstractFeatureTemplate<IntraTokenNerlaScope> {

	/**
	 * 
	 */

	private static final String END_SIGN = "$";

	private static final String START_SIGN = "^";

	private static final int MIN_TOKEN_CHAR_LENGTH = 2;

	private static final String LEFT = "<";

	private static final String RIGHT = ">";

	class IntraTokenNerlaScope extends AbstractFactorScope<IntraTokenNerlaScope> {

		public EntityType entityType;
		public final String surfaceForm;

		public IntraTokenNerlaScope(AbstractFeatureTemplate<IntraTokenNerlaScope> template, EntityType entityType,
				String surfaceForm) {
			super(template);
			this.entityType = entityType;
			this.surfaceForm = surfaceForm;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
			result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
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
			IntraTokenNerlaScope other = (IntraTokenNerlaScope) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (!entityType.equals(other.entityType))
				return false;
			if (surfaceForm == null) {
				if (other.surfaceForm != null)
					return false;
			} else if (!surfaceForm.equals(other.surfaceForm))
				return false;
			return true;
		}

		@Override
		public int implementHashCode() {
			return hashCode();
		}

		@Override
		public boolean implementEquals(Object obj) {
			return equals(obj);
		}

		private IntraTokenNerlaTemplate getOuterType() {
			return IntraTokenNerlaTemplate.this;
		}

	}

	@Override
	public List<IntraTokenNerlaScope> generateFactorScopes(State state) {
		List<IntraTokenNerlaScope> factors = new ArrayList<>();

		for (LiteralAnnotation annotation : state.getCurrentPredictions().<LiteralAnnotation>getAnnotations()) {

			factors.add(new IntraTokenNerlaScope(this, annotation.getEntityType(), annotation.getSurfaceForm()));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<IntraTokenNerlaScope> factor) {

		getTokenNgrams(factor.getFeatureVector(), factor.getFactorScope().entityType.entityTypeName,
				factor.getFactorScope().surfaceForm);

	}

	private void getTokenNgrams(DoubleVector featureVector, String name, String surfaceForm) {

		final String cM = START_SIGN + Document.TOKEN_SPLITTER + surfaceForm + Document.TOKEN_SPLITTER + END_SIGN;

		final String[] tokens = cM.split(Document.TOKEN_SPLITTER);

		final int maxNgramSize = tokens.length;

		featureVector.set(LEFT + name + RIGHT + Document.TOKEN_SPLITTER + cM, true);

		for (int ngram = 1; ngram < maxNgramSize; ngram++) {
			for (int i = 0; i < maxNgramSize - 1; i++) {

				/*
				 * Do not include start symbol.
				 */
				if (i + ngram == 1)
					continue;

				/*
				 * Break if size exceeds token length
				 */
				if (i + ngram > maxNgramSize)
					break;

				final StringBuffer fBuffer = new StringBuffer();
				for (int t = i; t < i + ngram; t++) {

					if (tokens[t].isEmpty())
						continue;

					if (Document.getStopWords().contains(tokens[t].toLowerCase()))
						continue;

					fBuffer.append(tokens[t]).append(Document.TOKEN_SPLITTER);

				}

				final String featureName = fBuffer.toString().trim();

				if (featureName.length() < MIN_TOKEN_CHAR_LENGTH)
					continue;

				if (featureName.isEmpty())
					continue;

				featureVector.set(LEFT + name + RIGHT + Document.TOKEN_SPLITTER + featureName, true);

			}
		}

	}

}

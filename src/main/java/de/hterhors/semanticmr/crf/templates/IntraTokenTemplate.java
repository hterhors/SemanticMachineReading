package de.hterhors.semanticmr.crf.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.templates.IntraTokenTemplate.IntraTokenScope;
import de.hterhors.semanticmr.crf.templates.TokenContextTemplate.TokenContextScope;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class IntraTokenTemplate extends AbstractFeatureTemplate<IntraTokenScope> {

	/**
	 * 
	 */

	private static final String TOKEN_SPLITTER_SPACE = " ";

	private static final String END_SIGN = "$";

	private static final String START_SIGN = "^";

	private static final int MIN_TOKEN_LENGTH = 2;

	private static final String LEFT = "<";

	private static final String RIGHT = ">";

	public static final Set<String> STOP_WORDS = new HashSet<>(
			Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it",
					"no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they",
					"this", "to", "was", "will", "with", "his", "her", "from", "who", "whom"));

	class IntraTokenScope extends AbstractFactorScope<IntraTokenScope> {

		public EntityType entityType;
		public final String surfaceForm;

		public IntraTokenScope(AbstractFeatureTemplate<IntraTokenScope> template, EntityType entityType,
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
			IntraTokenScope other = (IntraTokenScope) obj;
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

		private IntraTokenTemplate getOuterType() {
			return IntraTokenTemplate.this;
		}

	}

	@Override
	public List<IntraTokenScope> generateFactorScopes(State state) {
		List<IntraTokenScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : state.getCurrentPredictions().<EntityTemplate>getAnnotations()) {

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().literalAnnoation().build();

			for (Entry<SlotType, Set<AbstractSlotFiller<?>>> slot : filter.getMergedAnnotations().entrySet()) {

				for (AbstractSlotFiller<?> slotFiller : slot.getValue()) {

					final LiteralAnnotation docLinkedAnnotation = slotFiller.asInstanceOfLiteralAnnotation();

					factors.add(new IntraTokenScope(this, docLinkedAnnotation.getEntityType(),
							docLinkedAnnotation.getSurfaceForm()));
				}
			}
		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<IntraTokenScope> factor) {

		getTokenNgrams(factor.getFeatureVector(), factor.getFactorScope().entityType.entityTypeName,
				factor.getFactorScope().surfaceForm);

	}

	private void getTokenNgrams(DoubleVector featureVector, String name, String surfaceForm) {

		final String cM = START_SIGN + TOKEN_SPLITTER_SPACE + surfaceForm + TOKEN_SPLITTER_SPACE + END_SIGN;

		final String[] tokens = cM.split(TOKEN_SPLITTER_SPACE);

		final int maxNgramSize = tokens.length;

		featureVector.set(LEFT + name + RIGHT + TOKEN_SPLITTER_SPACE + cM, true);

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

					if (STOP_WORDS.contains(tokens[t].toLowerCase()))
						continue;

					fBuffer.append(tokens[t]).append(TOKEN_SPLITTER_SPACE);

				}

				final String featureName = fBuffer.toString().trim();

				if (featureName.length() < MIN_TOKEN_LENGTH)
					continue;

				if (featureName.isEmpty())
					continue;

				featureVector.set(LEFT + name + RIGHT + TOKEN_SPLITTER_SPACE + featureName, true);

			}
		}

	}

}

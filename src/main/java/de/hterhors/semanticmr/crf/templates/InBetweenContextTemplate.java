package de.hterhors.semanticmr.crf.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.InBetweenContextTemplate.InBetweenContextScope;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * This template creates feature in form of an in between context. Each feature
 * contains the parent class annotations and its property slot annotation and
 * the text in between. Further we capture the
 * 
 * @author hterhors
 *
 * @date Jan 15, 2018
 */
public class InBetweenContextTemplate extends AbstractFeatureTemplate<InBetweenContextScope> {

	public static final Set<String> STOP_WORDS = new HashSet<>(
			Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it",
					"no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they",
					"this", "to", "was", "will", "with", "his", "her", "from", "who", "whom"));

	private static final String SPLITTER = " ";

	private static final String LEFT = "<";

	private static final String RIGHT = ">";

	private static final String END_DOLLAR = "$";

	private static final String START_CIRCUMFLEX = "^";

	private static final int MIN_TOKEN_LENGTH = 2;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int MAX_TOKEN_DIST = 10;

	private static final int MIN_TOKEN_DIST = 2;

	public InBetweenContextTemplate() {
	}

	static class PositionPairContainer {

		final String fromClassNameType;
		final int fromTokenIndex;

		final String toClassNameType;
		final int toTokenIndex;

		public PositionPairContainer(String fromClassNameType, int fromTokenIndex, String toClassNameType,
				int toTokenIndex) {
			this.fromClassNameType = fromClassNameType;
			this.fromTokenIndex = fromTokenIndex;
			this.toClassNameType = toClassNameType;
			this.toTokenIndex = toTokenIndex;
		}

	}

	class InBetweenContextScope extends AbstractFactorScope<InBetweenContextScope> {

		public final Instance instance;
		public final EntityType fromEntity;
		public final Integer fromEntityCharacterOnset;
		public final EntityType toEntity;
		public final Integer toEntityCharacterOnset;

		public InBetweenContextScope(AbstractFeatureTemplate<InBetweenContextScope> template, Instance instance,
				EntityType fromEntity, Integer fromEntityCharacterOnset, EntityType toEntity,
				Integer toEntityCharacterOnset) {
			super(template);
			this.instance = instance;
			this.fromEntity = fromEntity;
			this.fromEntityCharacterOnset = fromEntityCharacterOnset;
			this.toEntity = toEntity;
			this.toEntityCharacterOnset = toEntityCharacterOnset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((fromEntity == null) ? 0 : fromEntity.hashCode());
			result = prime * result + ((fromEntityCharacterOnset == null) ? 0 : fromEntityCharacterOnset.hashCode());
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			result = prime * result + ((toEntity == null) ? 0 : toEntity.hashCode());
			result = prime * result + ((toEntityCharacterOnset == null) ? 0 : toEntityCharacterOnset.hashCode());
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
			InBetweenContextScope other = (InBetweenContextScope) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (fromEntity == null) {
				if (other.fromEntity != null)
					return false;
			} else if (!fromEntity.equals(other.fromEntity))
				return false;
			if (fromEntityCharacterOnset == null) {
				if (other.fromEntityCharacterOnset != null)
					return false;
			} else if (!fromEntityCharacterOnset.equals(other.fromEntityCharacterOnset))
				return false;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (toEntity == null) {
				if (other.toEntity != null)
					return false;
			} else if (!toEntity.equals(other.toEntity))
				return false;
			if (toEntityCharacterOnset == null) {
				if (other.toEntityCharacterOnset != null)
					return false;
			} else if (!toEntityCharacterOnset.equals(other.toEntityCharacterOnset))
				return false;
			return true;
		}

		private InBetweenContextTemplate getOuterType() {
			return InBetweenContextTemplate.this;
		}

		@Override
		public int implementHashCode() {
			return hashCode();
		}

		@Override
		public boolean implementEquals(Object obj) {
			return equals(obj);
		}

	}

	@Override
	public List<InBetweenContextScope> generateFactorScopes(State state) {

		final List<InBetweenContextScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : state.getCurrentPredictions().<EntityTemplate>getAnnotations()) {

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().literalAnnoation().build();

			final List<AbstractSlotFiller<?>> slotFillers = filter.getMergedAnnotations().values().stream()
					.flatMap(s -> s.stream()).collect(Collectors.toList());

			for (int i = 0; i < slotFillers.size(); i++) {
				AbstractSlotFiller<?> fromSlotFiller = slotFillers.get(i);

				for (int j = i + 1; j < slotFillers.size(); j++) {
					AbstractSlotFiller<?> toSlotFiller = slotFillers.get(j);

					final Integer fromOffset = ((DocumentLinkedAnnotation) fromSlotFiller).getStartOffset();
					final Integer toOffset = ((DocumentLinkedAnnotation) toSlotFiller).getStartOffset();

					if (fromOffset.intValue() > toOffset.intValue()) {
						factors.add(new InBetweenContextScope(this, state.getInstance(), toSlotFiller.getEntityType(),
								toOffset, fromSlotFiller.getEntityType(), fromOffset));
					} else if (fromOffset.intValue() < toOffset.intValue()) {
						factors.add(new InBetweenContextScope(this, state.getInstance(), fromSlotFiller.getEntityType(),
								fromOffset, toSlotFiller.getEntityType(), toOffset));
					}

				}
			}
		}

		return factors;

	}

	@Override
	public void generateFeatureVector(Factor<InBetweenContextScope> factor) {
		try {

			DoubleVector featureVector = factor.getFeatureVector();

			final List<DocumentToken> tokens = factor.getFactorScope().instance.getDocument().tokenList;

			final String fromName = factor.getFactorScope().fromEntity.entityTypeName;
			final int fromTokenIndex = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().fromEntityCharacterOnset).docTokenIndex;

			final String toName = factor.getFactorScope().toEntity.entityTypeName;
			final int toTokenIndex = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().toEntityCharacterOnset).docTokenIndex;

			if (toTokenIndex - fromTokenIndex > MAX_TOKEN_DIST)
				return;

			if (toTokenIndex - fromTokenIndex < MIN_TOKEN_DIST)
				return;

			StringBuffer fullTokenFeature = new StringBuffer(LEFT).append(fromName).append(RIGHT).append(SPLITTER);

			final String[] inBetweenContext = new String[toTokenIndex - fromTokenIndex + 1];
			inBetweenContext[0] = START_CIRCUMFLEX;
			int index = 1;
			for (int i = fromTokenIndex + 1; i < toTokenIndex; i++) {
				inBetweenContext[index++] = tokens.get(i).text;

				/*
				 * Each token as single feature.
				 */
				final StringBuffer feature = new StringBuffer(LEFT).append(fromName).append(RIGHT).append(SPLITTER);
				feature.append(inBetweenContext[index]);
				feature.append(SPLITTER);
				feature.append(LEFT).append(toName).append(RIGHT);
				featureVector.set(feature.toString(), true);

				/*
				 * Collect all in between tokens.
				 */
				fullTokenFeature.append(tokens.get(i).text);
				fullTokenFeature.append(SPLITTER);
			}
			fullTokenFeature.append(LEFT).append(toName).append(RIGHT);
			featureVector.set(fullTokenFeature.toString(), true);

			inBetweenContext[index] = END_DOLLAR;
			if (inBetweenContext.length > 4)
				getTokenNgrams(featureVector, fromName, toName, inBetweenContext);
		} catch (Exception e) {
//			System.out.println("WARN! " + e.getMessage());
		}
	}

	private void getTokenNgrams(DoubleVector featureVector, String fromClassName, String toClassName, String[] tokens) {

		final int maxNgramSize = tokens.length;
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

					fBuffer.append(tokens[t]).append(SPLITTER);

				}

				final String featureName = fBuffer.toString().trim();

				if (featureName.length() < MIN_TOKEN_LENGTH)
					continue;

				if (featureName.isEmpty())
					continue;

				featureVector.set(
						LEFT + fromClassName + RIGHT + SPLITTER + featureName + SPLITTER + LEFT + toClassName + RIGHT,
						true);

			}
		}

	}

}
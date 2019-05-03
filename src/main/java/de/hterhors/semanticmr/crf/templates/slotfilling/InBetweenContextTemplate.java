package de.hterhors.semanticmr.crf.templates.slotfilling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.slotfilling.InBetweenContextTemplate.InBetweenContextScope;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

/**
 * This template creates feature in form of an in between context. Each feature
 * contains the parent class annotations and its property slot annotation and
 * the text in between. Further we capture the
 * 
 * @author hterhors
 *
 * @date Jan 15, 2018
 */
public class InBetweenContextTemplate extends AbstractFeatureTemplate<InBetweenContextScope, EntityTemplate> {

	private static final String LEFT = "<";

	private static final String RIGHT = ">";

	private static final String END_DOLLAR = "$";

	private static final String START_CIRCUMFLEX = "^";

	private static final int MIN_TOKEN_LENGTH = 2;

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

	class InBetweenContextScope extends AbstractFactorScope<InBetweenContextScope, EntityTemplate> {

		public final Instance instance;
		public final EntityType fromEntity;
		public final Integer fromEntityCharacterOnset;
		public final EntityType toEntity;
		public final Integer toEntityCharacterOnset;

		public InBetweenContextScope(AbstractFeatureTemplate<InBetweenContextScope, EntityTemplate> template,
				Instance instance, EntityType fromEntity, Integer fromEntityCharacterOnset, EntityType toEntity,
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

		for (EntityTemplate annotation : getPredictedAnnotations(state)) {

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().literalAnnoation().build();

			final List<AbstractAnnotation> slotFillers = filter.getMergedAnnotations().values().stream()
					.flatMap(s -> s.stream()).collect(Collectors.toList());

			for (int i = 0; i < slotFillers.size(); i++) {
				AbstractAnnotation fromSlotFiller = slotFillers.get(i);

				for (int j = i + 1; j < slotFillers.size(); j++) {
					AbstractAnnotation toSlotFiller = slotFillers.get(j);

					final Integer fromOffset = ((DocumentLinkedAnnotation) fromSlotFiller).getStartDocCharOffset();
					final Integer toOffset = ((DocumentLinkedAnnotation) toSlotFiller).getStartDocCharOffset();

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

			final String fromEntityType = factor.getFactorScope().fromEntity.entityName;
			final int fromTokenIndex = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().fromEntityCharacterOnset).getDocTokenIndex();

			final String toEntityType = factor.getFactorScope().toEntity.entityName;
			final int toTokenIndex = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().toEntityCharacterOnset).getDocTokenIndex();

			if (toTokenIndex - fromTokenIndex > MAX_TOKEN_DIST)
				return;

			if (toTokenIndex - fromTokenIndex < MIN_TOKEN_DIST)
				return;

			final List<DocumentToken> tokens = factor.getFactorScope().instance.getDocument().tokenList
					.subList(fromTokenIndex + 1, toTokenIndex); // exclude start token.

			if (tokens.size() > 2)
				getTokenNgrams(featureVector, fromEntityType, toEntityType, tokens);
		} catch (DocumentLinkedAnnotationMismatchException e) {
			System.out.println("WARN! " + e.getMessage());

		}
	}

	private void getTokenNgrams(DoubleVector featureVector, String fromClassName, String toClassName,
			List<DocumentToken> tokens) {

		final int maxNgramSize = tokens.size() + 2;
		for (int ngram = 1; ngram < maxNgramSize; ngram++) {
			for (int i = 0; i < maxNgramSize - 1; i++) {

				/*
				 * Break if size exceeds token length
				 */
				if (i + ngram > maxNgramSize)
					break;

				final StringBuffer fBuffer = new StringBuffer();
				for (int t = i; t < i + ngram; t++) {

					final String text;
					if (t == 0)
						text = START_CIRCUMFLEX;
					else if (t == tokens.size() + 1)
						text = END_DOLLAR;
					else {

						final DocumentToken token = tokens.get(t - 1);

						if (token.getText().isEmpty())
							continue;

						if (token.isStopWord())
							continue;
						text = token.getText();
					}

					fBuffer.append(text);
					fBuffer.append(Document.TOKEN_SPLITTER);

				}

				final String featureName = fBuffer.toString().trim();

				if (featureName.length() < MIN_TOKEN_LENGTH)
					continue;

				if (featureName.isEmpty())
					continue;

				featureVector.set(LEFT + fromClassName + RIGHT + Document.TOKEN_SPLITTER + featureName
						+ Document.TOKEN_SPLITTER + LEFT + toClassName + RIGHT, true);

			}
		}

	}

}
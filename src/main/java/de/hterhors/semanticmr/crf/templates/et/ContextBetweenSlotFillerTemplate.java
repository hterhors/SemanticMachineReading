package de.hterhors.semanticmr.crf.templates.et;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.et.ContextBetweenSlotFillerTemplate.ContextBetweenScope;
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
public class ContextBetweenSlotFillerTemplate extends AbstractFeatureTemplate<ContextBetweenScope> {

	private static final String LEFT = "<";

	private static final String RIGHT = ">";

	private static final String END_DOLLAR = "$";

	private static final String START_CIRCUMFLEX = "^";

	private static final int MIN_TOKEN_LENGTH = 2;

	private static final int MAX_TOKEN_DIST = 10;

	private static final int MIN_TOKEN_DIST = 2;

	private static final String PREFIX = "CBSFT\t";

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

	static class ContextBetweenScope extends AbstractFactorScope {

		public final Instance instance;
		public final EntityType fromEntity;
		public final Integer fromEntityCharacterOnset;
		public final EntityType toEntity;
		public final Integer toEntityCharacterOnset;

		public ContextBetweenScope(AbstractFeatureTemplate<ContextBetweenScope> template, Instance instance,
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
			ContextBetweenScope other = (ContextBetweenScope) obj;
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

		@Override
		public int implementHashCode() {
			return hashCode();
		}

		@Override
		public boolean implementEquals(Object obj) {
			return equals(obj);
		}

		@Override
		public String toString() {
			return "ContextBetweenScope [instance=" + instance + ", fromEntity=" + fromEntity
					+ ", fromEntityCharacterOnset=" + fromEntityCharacterOnset + ", toEntity=" + toEntity
					+ ", toEntityCharacterOnset=" + toEntityCharacterOnset + "]";
		}

	}

	@Override
	public List<ContextBetweenScope> generateFactorScopes(State state) {

		final List<ContextBetweenScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : super.<EntityTemplate>getPredictedAnnotations(state)) {

			/**
			 * TODO: get token index directly!
			 */
			Integer rootOffset = null;
			if (annotation.asInstanceOfEntityTemplate().getRootAnnotation().isInstanceOfDocumentLinkedAnnotation()) {
				rootOffset = annotation.getRootAnnotation().asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0)
						.getDocTokenIndex();
//				rootOffset = annotation.getRootAnnotation().asInstanceOfDocumentLinkedAnnotation()
//						.getStartDocCharOffset();
			}

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().docLinkedAnnoation().build();

			final List<AbstractAnnotation> slotFillers = filter.getMergedAnnotations().values().stream()
					.flatMap(s -> s.stream()).collect(Collectors.toList());

			for (int i = 0; i < slotFillers.size(); i++) {
				final AbstractAnnotation fromSlotFiller = slotFillers.get(i);

				final Integer fromOffset = fromSlotFiller.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0)
						.getDocTokenIndex();
//				final Integer fromOffset = fromSlotFiller.asInstanceOfDocumentLinkedAnnotation()
//						.getStartDocCharOffset();

				if (annotation.asInstanceOfEntityTemplate().getRootAnnotation()
						.isInstanceOfDocumentLinkedAnnotation()) {
					if (Math.abs(rootOffset - fromOffset) > MAX_TOKEN_DIST)
						continue;

					if (Math.abs(rootOffset - fromOffset) < MIN_TOKEN_DIST)
						continue;
					if (rootOffset.intValue() > fromOffset.intValue()) {
						factors.add(new ContextBetweenScope(this, state.getInstance(), fromSlotFiller.getEntityType(),
								fromOffset, annotation.getEntityType(), rootOffset));
					} else if (rootOffset.intValue() < fromOffset.intValue()) {
						factors.add(new ContextBetweenScope(this, state.getInstance(), annotation.getEntityType(),
								rootOffset, fromSlotFiller.getEntityType(), fromOffset));
					}
				}

				for (int j = i + 1; j < slotFillers.size(); j++) {
					final AbstractAnnotation toSlotFiller = slotFillers.get(j);

					final Integer toOffset = toSlotFiller.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0)
							.getDocTokenIndex();
//					final Integer toOffset = toSlotFiller.asInstanceOfDocumentLinkedAnnotation()
//							.getStartDocCharOffset();

					if (Math.abs(toOffset - fromOffset) > MAX_TOKEN_DIST)
						continue;

					if (Math.abs(toOffset - fromOffset) < MIN_TOKEN_DIST)
						continue;

					if (fromOffset.intValue() > toOffset.intValue()) {

						factors.add(new ContextBetweenScope(this, state.getInstance(), toSlotFiller.getEntityType(),
								toOffset, fromSlotFiller.getEntityType(), fromOffset));
					} else if (fromOffset.intValue() < toOffset.intValue()) {
						factors.add(new ContextBetweenScope(this, state.getInstance(), fromSlotFiller.getEntityType(),
								fromOffset, toSlotFiller.getEntityType(), toOffset));
					}

				}
			}
		}

		return factors;

	}

	@Override
	public void generateFeatureVector(Factor<ContextBetweenScope> factor) {

		EntityType fromEntity = factor.getFactorScope().fromEntity;
		EntityType toEntity = factor.getFactorScope().toEntity;
//		try {

		DoubleVector featureVector = factor.getFeatureVector();

		final int fromTokenIndex = factor.getFactorScope().fromEntityCharacterOnset;
//			final int fromTokenIndex = factor.getFactorScope().instance.getDocument()
//					.getTokenByCharStartOffset(factor.getFactorScope().fromEntityCharacterOnset).getDocTokenIndex();

//			final int toTokenIndex = factor.getFactorScope().instance.getDocument()
//					.getTokenByCharStartOffset(factor.getFactorScope().toEntityCharacterOnset).getDocTokenIndex();
		final int toTokenIndex = factor.getFactorScope().toEntityCharacterOnset;

		final List<DocumentToken> tokens = factor.getFactorScope().instance.getDocument().tokenList
				.subList(fromTokenIndex + 1, toTokenIndex); // exclude start token.

		if (tokens.size() > 2) {

			Set<EntityType> fromEntityTypes = new HashSet<>();
			fromEntityTypes.add(fromEntity);
			fromEntityTypes.addAll(fromEntity.getTransitiveClosureSuperEntityTypes());

			Set<EntityType> toEntityTypes = new HashSet<>();
			toEntityTypes.add(toEntity);
			toEntityTypes.addAll(toEntity.getTransitiveClosureSuperEntityTypes());

			getTokenNgrams(featureVector, fromEntityTypes, toEntityTypes, tokens);

		}
//		} catch (DocumentLinkedAnnotationMismatchException e) {
//			e.printStackTrace();
//			System.out.println("WARN! " + e.getMessage());
//
//		}
	}

	private void getTokenNgrams(DoubleVector featureVector, Set<EntityType> fromEntityTypes,
			Set<EntityType> toEntityTypes, List<DocumentToken> tokens) {

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
				for (EntityType fromEntityType : fromEntityTypes) {
					for (EntityType toEntityType : toEntityTypes) {
						featureVector.set(PREFIX + LEFT + fromEntityType.name + RIGHT + Document.TOKEN_SPLITTER
								+ featureName + Document.TOKEN_SPLITTER + LEFT + toEntityType.name + RIGHT, true);
					}
				}

			}
		}

	}

}
package de.hterhors.semanticmr.crf.templates.slotfilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.slotfilling.TokenContextTemplate.TokenContextScope;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class TokenContextTemplate extends AbstractFeatureTemplate<TokenContextScope, EntityTemplate> {

	private static final char SPLITTER = ' ';
	private static final char RIGHT = '>';
	private static final char LEFT = '<';

	class TokenContextScope extends AbstractFactorScope<TokenContextScope, EntityTemplate> {

		public final Instance instance;

		public final EntityType entityType;

		public final int startOffset;

		public final int endOffset;

		public TokenContextScope(AbstractFeatureTemplate<TokenContextScope, EntityTemplate> template, Instance instance,
				EntityType entityType, int startOffset, int endOffset) {
			super(template);
			this.instance = instance;
			this.entityType = entityType;
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + endOffset;
			result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			result = prime * result + startOffset;
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
			TokenContextScope other = (TokenContextScope) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (endOffset != other.endOffset)
				return false;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (!entityType.equals(other.entityType))
				return false;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (startOffset != other.startOffset)
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

		private TokenContextTemplate getOuterType() {
			return TokenContextTemplate.this;
		}

	}

	@Override
	public List<TokenContextScope> generateFactorScopes(State state) {
		List<TokenContextScope> factors = new ArrayList<>();

		for (EntityTemplate annotation : getPredictedAnnotations(state)) {

			final EntityTemplateAnnotationFilter filter = annotation.filter().singleSlots().multiSlots().merge()
					.nonEmpty().docLinkedAnnoation().build();

			for (Entry<SlotType, Set<AbstractSlotFiller<? extends AbstractSlotFiller<?>>>> slot : filter
					.getMergedAnnotations().entrySet()) {

				for (AbstractSlotFiller<?> slotFiller : slot.getValue()) {

					final DocumentLinkedAnnotation docLinkedAnnotation = slotFiller
							.asInstanceOfDocumentLinkedAnnotation();

					factors.add(new TokenContextScope(this, state.getInstance(), docLinkedAnnotation.getEntityType(),
							docLinkedAnnotation.getStartDocCharOffset(), docLinkedAnnotation.getEndDocCharOffset()));
				}
			}

		}

		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<TokenContextScope> factor) {
		DoubleVector featureVector = factor.getFeatureVector();

		try {

			DocumentToken beginToken = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().startOffset);
			DocumentToken endToken = factor.getFactorScope().instance.getDocument()
					.getTokenByCharOffset(factor.getFactorScope().endOffset);

			addContextFeatures(featureVector, factor.getFactorScope().instance.getDocument().tokenList,
					factor.getFactorScope().entityType.entityTypeName, beginToken.getDocTokenIndex(),
					endToken.getDocTokenIndex());
		} catch (Exception e) {
//			System.out.println("WARN! " + e.getMessage());
		}
	}

	private void addContextFeatures(DoubleVector featureVector, List<DocumentToken> tokens, String className,
			int beginTokenIndex, int endTokenIndex) {

		final String[] leftContext = extractLeftContext(tokens, beginTokenIndex);

		final String[] rightContext = extractRightContext(tokens, endTokenIndex);

		getContextFeatures(featureVector, className, leftContext, rightContext);
	}

	private String[] extractLeftContext(List<DocumentToken> tokens, int beginTokenIndex) {
		final String[] leftContext = new String[3];

		for (int i = 1; i < 4; i++) {
			if (beginTokenIndex - i >= 0) {
				leftContext[i - 1] = tokens.get(beginTokenIndex - i).getText();
			} else {
				break;
			}
		}
		return leftContext;
	}

	private String[] extractRightContext(List<DocumentToken> tokens, int endTokenIndex) {
		final String[] rightContext = new String[3];

		for (int i = 1; i < 4; i++) {
			if (endTokenIndex + i < tokens.size()) {
				rightContext[i - 1] = tokens.get(endTokenIndex + i).getText();
			} else {
				break;
			}
		}
		return rightContext;

	}

	private void getContextFeatures(DoubleVector featureVector, final String contextClass, final String[] leftContext,
			final String[] rightContext) {

		final StringBuffer lCs = new StringBuffer();
		final StringBuffer rCs = new StringBuffer();
		for (int i = 0; i < leftContext.length; i++) {
			rCs.setLength(0);
			lCs.insert(0, leftContext[i] + SPLITTER);
			featureVector.set(
					new StringBuffer(lCs).append(LEFT).append(contextClass).append(RIGHT).append(rCs).toString().trim(),
					true);

			for (int j = 0; j < rightContext.length; j++) {
				rCs.append(SPLITTER).append(rightContext[j]);
				featureVector.set(new StringBuffer(lCs).append(LEFT).append(contextClass).append(RIGHT).append(rCs)
						.toString().trim(), true);

			}
		}

		rCs.setLength(0);
		lCs.setLength(0);

		for (int i = 0; i < rightContext.length; i++) {
			lCs.setLength(0);
			rCs.append(SPLITTER).append(rightContext[i]);
			featureVector.set(
					new StringBuffer(lCs).append(LEFT).append(contextClass).append(RIGHT).append(rCs).toString().trim(),
					true);

			for (int j = 0; j < leftContext.length; j++) {
				lCs.insert(0, leftContext[j] + SPLITTER);
				featureVector.set(new StringBuffer(lCs).append(LEFT).append(contextClass).append(RIGHT).append(rCs)
						.toString().trim(), true);

			}

		}
	}
}

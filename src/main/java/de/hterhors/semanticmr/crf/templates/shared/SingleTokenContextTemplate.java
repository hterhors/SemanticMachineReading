package de.hterhors.semanticmr.crf.templates.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.structure.annotations.filter.EntityTemplateAnnotationFilter;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.shared.SingleTokenContextTemplate.SingleTokenContextScope;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class SingleTokenContextTemplate extends AbstractFeatureTemplate<SingleTokenContextScope> {

	private static final int DEFAULT_MAX_TOKEN_CONTEXT_LEFT = 10;
	private static final int DEFAULT_MAX_TOKEN_CONTEXT_RIGHT = 10;
	private static final String BOF = "BOF";
	private static final String EOF = "EOF";
	private static final String PREFIX = "STCT\t";

	public SingleTokenContextTemplate() {
		super(false);
	}

	static class SingleTokenContextScope extends AbstractFactorScope {

		public final Instance instance;

		public final EntityType entityType;

		public final DocumentToken firstToken;

		public final DocumentToken lastToken;

		public SingleTokenContextScope(AbstractFeatureTemplate<SingleTokenContextScope> template, Instance instance,
				EntityType entityType, DocumentToken firstToken, DocumentToken lastToken) {
			super(template);
			this.instance = instance;
			this.entityType = entityType;
			this.firstToken = firstToken;
			this.lastToken = lastToken;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SingleTokenContextScope other = (SingleTokenContextScope) obj;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (!entityType.equals(other.entityType))
				return false;
			if (firstToken == null) {
				if (other.firstToken != null)
					return false;
			} else if (!firstToken.equals(other.firstToken))
				return false;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (lastToken == null) {
				if (other.lastToken != null)
					return false;
			} else if (!lastToken.equals(other.lastToken))
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

	}

	@Override
	public List<SingleTokenContextScope> generateFactorScopes(State state) {
		List<SingleTokenContextScope> factors = new ArrayList<>();

		for (AbstractAnnotation annotation : getPredictedAnnotations(state)) {

			if (annotation.isInstanceOfDocumentLinkedAnnotation()) {

				factors.add(new SingleTokenContextScope(this, state.getInstance(), annotation.getEntityType(),
						annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0),
						annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens
								.get(annotation.asInstanceOfDocumentLinkedAnnotation().relatedTokens.size() - 1)));

			} else if (annotation.isInstanceOfEntityTemplate()) {

				if (annotation.asInstanceOfEntityTemplate().getRootAnnotation()
						.isInstanceOfDocumentLinkedAnnotation()) {

					final DocumentLinkedAnnotation rootAnn = annotation.asInstanceOfEntityTemplate().getRootAnnotation()
							.asInstanceOfDocumentLinkedAnnotation();

					factors.add(new SingleTokenContextScope(this, state.getInstance(), rootAnn.getEntityType(),
							rootAnn.relatedTokens.get(0), rootAnn.relatedTokens.get(rootAnn.relatedTokens.size() - 1)));
				}

				final EntityTemplateAnnotationFilter filter = annotation.asInstanceOfEntityTemplate().filter()
						.singleSlots().multiSlots().merge().nonEmpty().docLinkedAnnoation().build();

				for (Entry<SlotType, Set<AbstractAnnotation>> slot : filter.getMergedAnnotations().entrySet()) {

					for (AbstractAnnotation slotFiller : slot.getValue()) {

						final DocumentLinkedAnnotation docLinkedAnnotation = slotFiller
								.asInstanceOfDocumentLinkedAnnotation();

						factors.add(new SingleTokenContextScope(this, state.getInstance(),
								docLinkedAnnotation.getEntityType(), docLinkedAnnotation.relatedTokens.get(0),
								docLinkedAnnotation.relatedTokens.get(docLinkedAnnotation.relatedTokens.size() - 1)));
					}
				}
			}

		}

		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<SingleTokenContextScope> factor) {
		DoubleVector featureVector = factor.getFeatureVector();

		DocumentToken beginToken = factor.getFactorScope().firstToken;
		DocumentToken endToken = factor.getFactorScope().lastToken;

		Set<EntityType> entityTypes = new HashSet<>();
		entityTypes.add(factor.getFactorScope().entityType);
		entityTypes.addAll(factor.getFactorScope().entityType.getTransitiveClosureSuperEntityTypes());

		int distance = 0;
		for (int i = Math.max(0, beginToken.getDocTokenIndex() - DEFAULT_MAX_TOKEN_CONTEXT_LEFT) - 1; i < beginToken
				.getDocTokenIndex(); i++) {

			if (i < 0)
				for (EntityType entityType : entityTypes) {
					featureVector.set(PREFIX + BOF + " " + entityType.name, true);
				}
			else {
				final String text = factor.getFactorScope().instance.getDocument().tokenList.get(i).getText();
				if (Document.getStopWords().contains(text))
					continue;
				for (EntityType entityType : entityTypes) {
					featureVector.set(PREFIX + text + " " + entityType.name, true);
					featureVector.set(PREFIX + text + "... " + entityType.name, true);
					featureVector.set(PREFIX + text + "... -" + distance + "... " + entityType.name, true);
				}
			}
			distance++;
		}
		distance = 0;
		for (int i = beginToken.getDocTokenIndex(); i <= Math.min(
				factor.getFactorScope().instance.getDocument().tokenList.size(),
				endToken.getDocTokenIndex() + DEFAULT_MAX_TOKEN_CONTEXT_RIGHT); i++) {

			if (i == factor.getFactorScope().instance.getDocument().tokenList.size())
				for (EntityType entityType : entityTypes) {
					featureVector.set(PREFIX + EOF + " " + entityType.name, true);
				}
			else {
				final String text = factor.getFactorScope().instance.getDocument().tokenList.get(i).getText();
				if (Document.getStopWords().contains(text))
					continue;
				for (EntityType entityType : entityTypes) {
					featureVector.set(PREFIX + text + " " + entityType.name, true);
					featureVector.set(PREFIX + entityType.name + "... " + text, true);
					featureVector.set(PREFIX + entityType.name + "... +" + distance + "... " + text, true);
				}
			}
			distance++;
		}

	}

}

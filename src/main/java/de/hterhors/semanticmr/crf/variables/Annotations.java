package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.EvaluationHelper;

public class Annotations implements IEvaluatable<Annotations> {

//	private boolean unmodifiable = false;

	private List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations;

//	private final Instance instance;

	private Set<DocumentToken> tokensWithAnnotations = new HashSet<>();

	public Annotations() {
		this(Collections.emptyList());
	}

	public Annotations(AbstractSlotFiller<? extends AbstractSlotFiller<?>> annotation) {
		this(Arrays.asList(annotation));
	}

	public Annotations(List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
		Objects.requireNonNull(annotations);
		this.annotations = annotations;
//		this.instance = instance;
		for (AbstractSlotFiller<? extends AbstractSlotFiller<?>> abstractSlotFiller : this.annotations) {
			if (abstractSlotFiller instanceof DocumentLinkedAnnotation) {
				tokensWithAnnotations.addAll(((DocumentLinkedAnnotation) abstractSlotFiller).relatedTokens);
			}
		}
	}

	public void unmodifiable() {
		annotations = Collections.unmodifiableList(annotations);
//		unmodifiable = true;
	}

	@SuppressWarnings("unchecked")
	public <Annotation extends AbstractSlotFiller<?>> List<Annotation> getAnnotations() {
		return (List<Annotation>) annotations;
	}

//	public void setAnnotations(List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations) {
//		if (unmodifiable)
//			throw new IllegalStateException("Annotations are unmodifiabled. Can not override them.");
//
//		this.annotations = annotations;
//	}

	@Override
	public Score evaluate(EEvaluationDetail evaluationMode, Annotations otherVal) {

		if (this.annotations.size() == 0 || otherVal.annotations.size() == 0 || otherVal == null)
			return Score.ZERO;

		if (this.annotations.size() == 1 && otherVal.annotations.size() == 1)
			return EvaluationHelper.scoreSingle(evaluationMode, this.annotations.get(0), otherVal.annotations.get(0));

		return EvaluationHelper.scoreMax(evaluationMode, this.annotations, otherVal.annotations);

	}

	public Annotations deepUpdateCopy(int annotationIndex,
			AbstractSlotFiller<? extends AbstractSlotFiller<?>> newCurrentPrediction) {

		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> updatedList = new ArrayList<>(
				annotations.size());
		for (int index = 0; index < annotations.size(); index++) {

			if (index == annotationIndex) {
				updatedList.add(newCurrentPrediction);
				continue;
			}
			updatedList.add(annotations.get(index).deepCopy());
		}

		return new Annotations(updatedList);
	}

	public Annotations deepRemoveCopy(int annotationIndex) {
		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> updatedList = new ArrayList<>(
				annotations.size());
		for (int index = 0; index < annotations.size(); index++) {

			if (index == annotationIndex)
				continue;

			updatedList.add(annotations.get(index).deepCopy());
		}

		return new Annotations(updatedList);
	}

	public Annotations deepAddCopy(AbstractSlotFiller<? extends AbstractSlotFiller<?>> newCurrentPrediction) {

		final List<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> updatedList = new ArrayList<>(
				annotations.size());
		for (int index = 0; index < annotations.size(); index++) {
			updatedList.add(annotations.get(index).deepCopy());
		}
		updatedList.add(newCurrentPrediction);

		return new Annotations(updatedList);
	}

	@Override
	public String toString() {
		return "Annotations [annotations="
				+ annotations.stream().map(p -> p.toPrettyString()).reduce("", String::concat) + "]";
	}

	public boolean containsAnnotationOnTokens(DocumentToken... tokens) {
		if (tokensWithAnnotations.isEmpty())
			return false;
		for (DocumentToken documentToken : tokens) {
			if (tokensWithAnnotations.contains(documentToken))
				return true;
		}
		return false;
	}

}

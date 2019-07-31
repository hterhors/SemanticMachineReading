package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

public class Annotations implements IEvaluatable {

	private List<AbstractAnnotation> annotations;

	private Set<DocumentToken> tokensWithAnnotations = new HashSet<>();

	public Annotations() {
		this(Collections.emptyList());
	}

	public Annotations(AbstractAnnotation... annotation) {
		this(Arrays.asList(annotation));
	}

	public Annotations(AbstractAnnotation annotation) {
		this(Arrays.asList(annotation));
	}

	public Annotations(List<AbstractAnnotation> annotations) {
		Objects.requireNonNull(annotations);
		this.annotations = annotations;
		for (AbstractAnnotation abstractSlotFiller : this.annotations) {
			if (abstractSlotFiller instanceof DocumentLinkedAnnotation) {
				tokensWithAnnotations.addAll(((DocumentLinkedAnnotation) abstractSlotFiller).relatedTokens);
			}
		}
	}

	public void unmodifiable() {
		annotations = Collections.unmodifiableList(annotations);
	}

	@SuppressWarnings("unchecked")
	public <Annotation extends AbstractAnnotation> List<Annotation> getAnnotations() {
		return (List<Annotation>) annotations;
	}

	@Override
	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable otherVal) {

		if (!(otherVal instanceof Annotations))
			return Score.ZERO;

		final Annotations otherAnnotations = (Annotations) otherVal;

		if (this.annotations.size() == 0 || otherAnnotations.annotations.size() == 0 || otherVal == null)
			return Score.ZERO;

		if (this.annotations.size() == 1 && otherAnnotations.annotations.size() == 1)
			return evaluator.scoreSingle(this.annotations.get(0), otherAnnotations.annotations.get(0));

		return evaluator.scoreMultiValues(this.annotations, otherAnnotations.annotations);

	}

	public Annotations deepUpdateCopy(int annotationIndex, AbstractAnnotation newCurrentPrediction) {

		final List<AbstractAnnotation> updatedList = new ArrayList<>(annotations.size());
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
		final List<AbstractAnnotation> updatedList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {

			if (index == annotationIndex)
				continue;

			updatedList.add(annotations.get(index).deepCopy());
		}

		return new Annotations(updatedList);
	}

	public Annotations deepAddCopy(AbstractAnnotation newCurrentPrediction) {

		final List<AbstractAnnotation> updatedList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {
			updatedList.add(annotations.get(index).deepCopy());
		}
		updatedList.add(newCurrentPrediction);

		return new Annotations(updatedList);
	}

	@Override
	public String toString() {
		return "Annotations [annotations="
				+ annotations.stream().map(p -> "\n" + p.toPrettyString()).reduce("", String::concat) + "]";
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((tokensWithAnnotations == null) ? 0 : tokensWithAnnotations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotations other = (Annotations) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (tokensWithAnnotations == null) {
			if (other.tokensWithAnnotations != null)
				return false;
		} else if (!tokensWithAnnotations.equals(other.tokensWithAnnotations))
			return false;
		return true;
	}

}

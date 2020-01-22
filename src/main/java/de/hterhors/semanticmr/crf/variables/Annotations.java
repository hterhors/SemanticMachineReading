package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance.GoldModificationRule;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public class Annotations {

	private List<AbstractAnnotation> predictions;

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
		this.predictions = annotations;
		for (AbstractAnnotation abstractSlotFiller : this.predictions) {
			if (abstractSlotFiller instanceof DocumentLinkedAnnotation) {
				tokensWithAnnotations.addAll(((DocumentLinkedAnnotation) abstractSlotFiller).relatedTokens);
			}
		}
	}

	public Annotations(Annotations goldAnnotations, Collection<GoldModificationRule> modifyRules) {
		this(applyModifications(goldAnnotations, modifyRules));
	}

	private static List<AbstractAnnotation> applyModifications(Annotations goldAnnotations,
			Collection<GoldModificationRule> modifyRules) {
		final List<AbstractAnnotation> modifiedAnnotations = new ArrayList<>();
		for (AbstractAnnotation goldAnnotation : goldAnnotations.getAbstractAnnotations()) {
			for (GoldModificationRule modifyGoldRule : modifyRules) {
				if (goldAnnotation != null)
					goldAnnotation = modifyGoldRule.modify(goldAnnotation);
			}
			if (goldAnnotation != null)
				modifiedAnnotations.add(goldAnnotation);
		}
		return modifiedAnnotations;
	}

	public Annotations unmodifiable() {
		predictions = Collections.unmodifiableList(predictions);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <Annotation extends AbstractAnnotation> List<Annotation> getAnnotations() {
		return (List<Annotation>) predictions;
	}

	public List<AbstractAnnotation> getAbstractAnnotations() {
		return predictions;
	}

	public Score evaluate(AbstractEvaluator evaluator, Annotations otherVal) {

		if (!(otherVal instanceof Annotations))
			return Score.ZERO;

		final Annotations otherAnnotations = (Annotations) otherVal;

//		if (this.predictions.isEmpty() && otherAnnotations.predictions.size() == 1) {
//			AbstractAnnotation e = otherAnnotations.predictions.iterator().next();
//			if (e.getEntityType() == EntityType.get("VertebralArea") && e.asInstanceOfEntityTemplate().isEmpty())
//				return Score.TP;
//		}


		
		if (this.predictions.size() == 0 || otherAnnotations.predictions.size() == 0 || otherVal == null)
			return Score.ZERO;

		if (this.predictions.size() == 1 && otherAnnotations.predictions.size() == 1)
			return evaluator.scoreSingle(this.predictions.get(0), otherAnnotations.predictions.get(0));

		return evaluator.scoreMultiValues(this.predictions, otherAnnotations.predictions);

	}

	public Annotations deepUpdateCopy(int annotationIndex, AbstractAnnotation newCurrentPrediction) {

		final List<AbstractAnnotation> updatedList = new ArrayList<>(predictions.size());
		for (int index = 0; index < predictions.size(); index++) {

			if (index == annotationIndex) {
				updatedList.add(newCurrentPrediction);
				continue;
			}
			updatedList.add(predictions.get(index).deepCopy());
		}

		return new Annotations(updatedList);
	}

	public Annotations deepRemoveCopy(int annotationIndex) {
		final List<AbstractAnnotation> updatedList = new ArrayList<>(predictions.size());
		for (int index = 0; index < predictions.size(); index++) {

			if (index == annotationIndex)
				continue;

			updatedList.add(predictions.get(index).deepCopy());
		}

		return new Annotations(updatedList);
	}

	public Annotations deepAddCopy(AbstractAnnotation newCurrentPrediction) {

		final List<AbstractAnnotation> updatedList = new ArrayList<>(predictions.size());
		for (int index = 0; index < predictions.size(); index++) {
			updatedList.add(predictions.get(index).deepCopy());
		}
		updatedList.add(newCurrentPrediction);

		return new Annotations(updatedList);
	}

	public Annotations deepCopy() {

		final List<AbstractAnnotation> deepCopyList = new ArrayList<>(predictions.size());
		for (int index = 0; index < predictions.size(); index++) {
			deepCopyList.add(predictions.get(index).deepCopy());
		}

		return new Annotations(deepCopyList);
	}

	@Override
	public String toString() {
		return "Annotations [annotations="
				+ predictions.stream().map(p -> "\n" + p.toPrettyString()).reduce("", String::concat) + "]";
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
		result = prime * result + ((predictions == null) ? 0 : predictions.hashCode());
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
		if (predictions == null) {
			if (other.predictions != null)
				return false;
		} else if (!predictions.equals(other.predictions))
			return false;
		if (tokensWithAnnotations == null) {
			if (other.tokensWithAnnotations != null)
				return false;
		} else if (!tokensWithAnnotations.equals(other.tokensWithAnnotations))
			return false;
		return true;
	}

}

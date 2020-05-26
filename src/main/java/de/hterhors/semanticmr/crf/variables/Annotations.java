package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance.DuplicationRule;
import de.hterhors.semanticmr.crf.variables.Instance.GoldModificationRule;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public class Annotations {

	private List<AbstractAnnotation> annotations;

	private Set<DocumentToken> tokensWithAnnotations = new HashSet<>();

	/**
	 * The new annotation that was added from the previous state.
	 */
	private AbstractAnnotation newAddAnnotation;
	private AbstractAnnotation newRemoveAnnotation;

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

	public Annotations(Annotations goldAnnotations, Collection<GoldModificationRule> modifyRules,
			DuplicationRule duplicationRule) {
		this(applyModifications(goldAnnotations, modifyRules, duplicationRule));
	}

	private static List<AbstractAnnotation> applyModifications(Annotations goldAnnotations,
			Collection<GoldModificationRule> modifyRules, DuplicationRule duplicationRule) {

		final List<AbstractAnnotation> modifiedAnnotations = new ArrayList<>();
		for (AbstractAnnotation goldAnnotation : goldAnnotations.getAbstractAnnotations()) {
			for (GoldModificationRule modifyGoldRule : modifyRules) {
				if (goldAnnotation != null)
					goldAnnotation = modifyGoldRule.modify(goldAnnotation);
			}
			if (goldAnnotation != null)
				modifiedAnnotations.add(goldAnnotation);
		}

		List<AbstractAnnotation> distinctAnnotations = new ArrayList<>();

		l_1: for (AbstractAnnotation abstractAnnotation : modifiedAnnotations) {

			for (AbstractAnnotation abstractAnnotation2 : distinctAnnotations) {

				if (duplicationRule.isDuplicate(abstractAnnotation, abstractAnnotation2)) {
					continue l_1;
				}

			}

			distinctAnnotations.add(abstractAnnotation);
		}

		return distinctAnnotations;
	}

	public Annotations unmodifiable() {
		annotations = Collections.unmodifiableList(annotations);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <Annotation extends AbstractAnnotation> List<Annotation> getAnnotations() {
		return (List<Annotation>) annotations;
	}

	public List<AbstractAnnotation> getAbstractAnnotations() {
		return annotations;
	}

	private Score prevScore = new Score();

	public Score evaluate(AbstractEvaluator evaluator, Annotations otherVal, EScoreType scoreType) {

		/**
		 * Speed up evaluation for NER. Only compare the change to the previous state
		 * with gold and combine prev score with the change.
		 */
		Score score = Score.getZero(scoreType);

		if (!(otherVal instanceof Annotations)) {
			score = Score.getZero(scoreType);
		} else {
			if (this.annotations.size() == 1 && otherVal.annotations.size() == 1)
				score = evaluator.scoreSingle(this.annotations.get(0), otherVal.annotations.get(0));
			else {
				if (evaluator.evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED && scoreType == EScoreType.MICRO
						&& (otherVal.newAddAnnotation != null || otherVal.newRemoveAnnotation != null)) {
					Score tmp = new Score();
					if (otherVal.newAddAnnotation != null) {
						Score partiallyScore = evaluator.scoreMultiValues(this.annotations,
								Arrays.asList(otherVal.newAddAnnotation), scoreType);

						/**
						 * if it was added correct add +1 tp and -1 fn
						 * 
						 * if it was added wrong +1 fp
						 * 
						 */
						tmp.add(new Score(otherVal.prevScore.getTp() + partiallyScore.getTp(),
								otherVal.prevScore.getFp() + partiallyScore.getFp(),
								otherVal.prevScore.getFn() - partiallyScore.getTp()));
						otherVal.newAddAnnotation = null;
					}
					if (otherVal.newRemoveAnnotation != null) {
						Score partiallyScore = evaluator.scoreMultiValues(this.annotations,
								Arrays.asList(otherVal.newRemoveAnnotation), scoreType);
						/**
						 * if a correct was removed -1 tp and + fn
						 * 
						 * if a wrong was removed -1 fp
						 */
						tmp.add(new Score(otherVal.prevScore.getTp() - partiallyScore.getTp(),
								otherVal.prevScore.getFp() - partiallyScore.getFp(),
								otherVal.prevScore.getFn() + partiallyScore.getTp()));
						otherVal.newRemoveAnnotation = null;
					}
					score = tmp;
				} else {
					score = evaluator.scoreMultiValues(this.annotations, otherVal.annotations, scoreType);
				}
			}
		}

		if (scoreType == EScoreType.MACRO)
			score.toMacro();
		else
			otherVal.prevScore = score;

		return score;
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

		final Annotations newAnnotations = new Annotations(updatedList);

		if (newCurrentPrediction.isInstanceOfEntityTypeAnnotation()) {
			newAnnotations.newRemoveAnnotation = annotations.get(annotationIndex);
			newAnnotations.newAddAnnotation = newCurrentPrediction;
			newAnnotations.prevScore = new Score(this.prevScore);
		}

		return newAnnotations;
	}

	public Annotations deepRemoveCopy(int removeAnnotationIndex) {
		final List<AbstractAnnotation> updatedList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {

			if (index == removeAnnotationIndex)
				continue;

			updatedList.add(annotations.get(index).deepCopy());
		}

		Annotations newAnnotations = new Annotations(updatedList);

		if (annotations.get(removeAnnotationIndex).isInstanceOfEntityTypeAnnotation()) {
			newAnnotations.prevScore = new Score(this.prevScore);
			newAnnotations.newRemoveAnnotation = annotations.get(removeAnnotationIndex);
		}
		return newAnnotations;
	}

	public Annotations deepAddCopy(AbstractAnnotation newCurrentPrediction) {

		final List<AbstractAnnotation> updatedList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {
			updatedList.add(annotations.get(index).deepCopy());
		}
		updatedList.add(newCurrentPrediction);

		Annotations newAnnotations = new Annotations(updatedList);

		if (newCurrentPrediction.isInstanceOfEntityTypeAnnotation()) {
			newAnnotations.newAddAnnotation = newCurrentPrediction;
			newAnnotations.prevScore = new Score(this.prevScore);
		}
		return newAnnotations;
	}

	public Annotations deepCopy() {

		final List<AbstractAnnotation> deepCopyList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {
			deepCopyList.add(annotations.get(index).deepCopy());
		}

		Annotations annotations = new Annotations(deepCopyList);
		annotations.newAddAnnotation = this.newAddAnnotation;
		annotations.newRemoveAnnotation = this.newRemoveAnnotation;
		annotations.prevScore = new Score(this.prevScore);
		return annotations;
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
		result = prime * result + ((newAddAnnotation == null) ? 0 : newAddAnnotation.hashCode());
		result = prime * result + ((newRemoveAnnotation == null) ? 0 : newRemoveAnnotation.hashCode());
		result = prime * result + ((prevScore == null) ? 0 : prevScore.hashCode());
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
		if (newAddAnnotation == null) {
			if (other.newAddAnnotation != null)
				return false;
		} else if (!newAddAnnotation.equals(other.newAddAnnotation))
			return false;
		if (newRemoveAnnotation == null) {
			if (other.newRemoveAnnotation != null)
				return false;
		} else if (!newRemoveAnnotation.equals(other.newRemoveAnnotation))
			return false;
		if (prevScore == null) {
			if (other.prevScore != null)
				return false;
		} else if (!prevScore.equals(other.prevScore))
			return false;
		if (tokensWithAnnotations == null) {
			if (other.tokensWithAnnotations != null)
				return false;
		} else if (!tokensWithAnnotations.equals(other.tokensWithAnnotations))
			return false;
		return true;
	}

}

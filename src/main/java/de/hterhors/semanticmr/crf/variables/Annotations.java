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
import de.hterhors.semanticmr.crf.variables.Instance.GoldModificationRule;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public class Annotations {

	public static boolean REMOVE_DUPLICATES = false;

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

		List<AbstractAnnotation> distinctAnnotations = new ArrayList<>();

		l_1: for (AbstractAnnotation abstractAnnotation : modifiedAnnotations) {

			if (REMOVE_DUPLICATES)
				for (AbstractAnnotation abstractAnnotation2 : distinctAnnotations) {

					if (abstractAnnotation2.evaluate(evaluator, abstractAnnotation).getF1() == 1.0D) {
						continue l_1;
					}

				}

			distinctAnnotations.add(abstractAnnotation);
		}

		return distinctAnnotations;
	}

	public final static CartesianEvaluator evaluator = new CartesianEvaluator(EEvaluationDetail.ENTITY_TYPE);

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

	public Score evaluate(AbstractEvaluator evaluator, Annotations otherVal, EScoreType scoreType) {

		Score score;

		if (!(otherVal instanceof Annotations)) {
			score = Score.ZERO;
		} else {
			final Annotations otherAnnotations = (Annotations) otherVal;

			if (this.annotations.size() == 1 && otherAnnotations.annotations.size() == 1)
				score = evaluator.scoreSingle(this.annotations.get(0), otherAnnotations.annotations.get(0));
			else
				score = evaluator.scoreMultiValues(this.annotations, otherAnnotations.annotations, scoreType);
		}
		
		
		if (scoreType == EScoreType.MACRO)
			score.toMacro();

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

	public Annotations deepCopy() {

		final List<AbstractAnnotation> deepCopyList = new ArrayList<>(annotations.size());
		for (int index = 0; index < annotations.size(); index++) {
			deepCopyList.add(annotations.get(index).deepCopy());
		}

		return new Annotations(deepCopyList);
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

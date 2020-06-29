package de.hterhors.semanticmr.eval;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;

public class NerlaEvaluator extends AbstractEvaluator {

	public NerlaEvaluator(EEvaluationDetail evaluationDetail) {
		super(evaluationDetail);
	}

	public Score prf1(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return prf1(evaluationDetail, annotations, otherAnnotations);
	}

	public Score prf1(EEvaluationDetail evaluationDetail, Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {

		int tp = 0;
		int fp = 0;
		int fn = 0;

		outer: for (AbstractAnnotation a : annotations) {
			for (AbstractAnnotation oa : otherAnnotations) {
				/*
				 * 
				 * If two annotations that are compared to each other of type document linked
				 * and of type literal then always compare at least the literal as well as the
				 * entity type. If the evaluation detail is DocLinked then also compare the
				 * position in the text!
				 * 
				 */

				if (a.isInstanceOfDocumentLinkedAnnotation() && oa.isInstanceOfDocumentLinkedAnnotation()
						&& a.getEntityType().isLiteral && oa.getEntityType().isLiteral) {

					boolean equals = true;
					if (equals)
						equals &= oa.getEntityType() == a.getEntityType();
					if (equals)
						equals &= oa.asInstanceOfLiteralAnnotation().getNormalizedSurfaceForm()
								.equals(a.asInstanceOfLiteralAnnotation().getSurfaceForm());
					if (equals && evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED)
						equals &= oa.asInstanceOfDocumentLinkedAnnotation().getStartDocCharOffset() == a
								.asInstanceOfDocumentLinkedAnnotation().getStartDocCharOffset();

					if (equals) {
						tp++;
						continue outer;
					}

				} else {
					if (oa.evaluateEquals(this, a)) {
						tp++;
						continue outer;
					}
				}

			}

			fn++;
		}

		fp = Math.max(otherAnnotations.size() - tp, 0);

		return new Score(tp, fp, fn);

	}

	public Score prf1(EntityTypeAnnotation gold, EntityTypeAnnotation predictions) {
		return prf1(Arrays.asList(gold), Arrays.asList(predictions));
	}

	@Override
	protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoretype) {
		Score score = prf1(annotations, otherAnnotations);

		if (scoretype == EScoreType.MACRO)
			score.toMacro();
		return score;
	}

	@Override
	protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {

		int tp = 0;

		outer: for (AbstractAnnotation a : annotations) {
			for (AbstractAnnotation oa : otherAnnotations) {
				if (oa.evaluateEquals(this, a)) {
					tp++;
					continue outer;
				}
			}

			return false;
		}

		return Math.max(otherAnnotations.size() - tp, 0) == 0;

	}

	@Override
	public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {
		throw new NotImplementedException("Not impl.");

	}

}

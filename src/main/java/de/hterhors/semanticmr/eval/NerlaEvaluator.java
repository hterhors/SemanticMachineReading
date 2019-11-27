package de.hterhors.semanticmr.eval;

import java.util.Arrays;
import java.util.Collection;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
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
//		System.out.println("annotations.size(): " + annotations.size());
//		System.out.println(annotations);
//		System.out.println("otherAnnotations.size(): " + otherAnnotations.size());
//		System.out.println(otherAnnotations);
		int tp = 0;
		int fp = 0;
		int fn = 0;

		outer: for (AbstractAnnotation a : annotations) {
			for (AbstractAnnotation oa : otherAnnotations) {
				if (oa.evaluate(this, a).getF1() == 1.0) {
					tp++;
					continue outer;
				}
			}

			fn++;
		}

		outer: for (AbstractAnnotation a : otherAnnotations) {
			for (AbstractAnnotation oa : annotations) {
				if (oa.evaluate(this, a).getF1() == 1.0) {
					continue outer;
				}
			}
			fp++;
		}
//		System.out.println(new Score(tp, fp, fn));
//		System.out.println();
		return new Score(tp, fp, fn);

	}

	public Score prf1(EntityTypeAnnotation gold, EntityTypeAnnotation predictions) {
		return prf1(Arrays.asList(gold), Arrays.asList(predictions));
	}

	@Override
	protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return prf1(annotations, otherAnnotations);
	}

}

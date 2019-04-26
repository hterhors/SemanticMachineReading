package de.hterhors.semanticmr.eval;

import java.util.Collection;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public interface IEvaluator {

	public Score scoreMax(EEvaluationDetail evaluationMode,
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> annotations,
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> otherAnnotations);

}

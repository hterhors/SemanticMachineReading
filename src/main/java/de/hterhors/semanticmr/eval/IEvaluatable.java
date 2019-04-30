package de.hterhors.semanticmr.eval;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.variables.Annotations;

public interface IEvaluatable {

	public Score evaluate(EEvaluationDetail evaluationMode, Annotations goldAnnotations,
			Annotations currentPredictions);

}
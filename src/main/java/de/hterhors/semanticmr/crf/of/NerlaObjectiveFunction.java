package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

public class NerlaObjectiveFunction implements IObjectiveFunction {

	final private EEvaluationDetail evaluationMode;

	final private NerlaEvaluator evaluator = new NerlaEvaluator();

	public NerlaObjectiveFunction(EEvaluationDetail evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public NerlaObjectiveFunction() {
		this.evaluationMode = EEvaluationDetail.DOCUMENT_LINKED;
	}

	@Override
	public void score(State state) {

		/**
		 * Implement details.
		 */
		state.setObjectiveScore(state.score(evaluator, evaluationMode).getF1());
	}

	@Override
	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

}

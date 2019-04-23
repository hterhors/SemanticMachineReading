package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

public class SlotFillingObjectiveFunction implements IObjectiveFunction {

	final private EEvaluationDetail evaluationMode;

	NerlaEvaluator x = new NerlaEvaluator();

	public SlotFillingObjectiveFunction(EEvaluationDetail evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public SlotFillingObjectiveFunction() {
		this.evaluationMode = EEvaluationDetail.DOCUMENT_LINKED;
	}

	public void score(State state) {
		state.setObjectiveScore(state.score(evaluationMode).getF1());
	}

	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

}

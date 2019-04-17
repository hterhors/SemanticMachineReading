package de.hterhors.semanticmr.crf;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationMode;

public class ObjectiveFunction {

	final private EEvaluationMode evaluationMode;

	public ObjectiveFunction(EEvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public ObjectiveFunction() {
		this.evaluationMode = EEvaluationMode.DOCUMENT_LINKED;
	}

	public void score(State state) {
		state.setObjectiveScore(state.score(evaluationMode).getF1());
	}

	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

}

package de.hterhors.semanticmr.crf;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

public class ObjectiveFunction {

	public void score(State state) {
		state.setObjectiveScore(state.goldEntityTemplate.compare(state.currentPredictedEntityTemplate).getF1());
	}

	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

}

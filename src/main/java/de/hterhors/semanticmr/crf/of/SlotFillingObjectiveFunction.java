package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

public class SlotFillingObjectiveFunction implements IObjectiveFunction {

	final private AbstractEvaluator evaluator;

	public SlotFillingObjectiveFunction(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public void score(State state) {
		state.setObjectiveScore(state.score(evaluator).getF1());
	}

	@Override
	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

}

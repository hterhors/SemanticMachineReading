package de.hterhors.semanticmr.crf.of;

import java.util.List;

import org.apache.jena.sparql.function.library.eval;

import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;

public class SlotFillingObjectiveFunction implements IObjectiveFunction {

	final private AbstractEvaluator evaluator;

	public SlotFillingObjectiveFunction(AbstractEvaluator evaluator) {
		if (evaluator instanceof CartesianEvaluator) {
			SlotFillingExplorer.MAX_NUMBER_OF_ANNOTATIONS = CartesianEvaluator.MAXIMUM_PERMUTATION_SIZE;
		}

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

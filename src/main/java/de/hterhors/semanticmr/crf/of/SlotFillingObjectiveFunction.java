package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.exploration.RootTemplateCardinalityExplorer;
import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.CartesianEvaluator;

public class SlotFillingObjectiveFunction implements IObjectiveFunction {

	final private AbstractEvaluator evaluator;
	final private EScoreType scoreType;

	public SlotFillingObjectiveFunction(EScoreType scoreType, AbstractEvaluator evaluator) {
		if (evaluator instanceof CartesianEvaluator) {
			SlotFillingExplorer.MAX_NUMBER_OF_ANNOTATIONS = CartesianEvaluator.MAXIMUM_PERMUTATION_SIZE;
			RootTemplateCardinalityExplorer.MAX_NUMBER_OF_ANNOTATIONS = CartesianEvaluator.MAXIMUM_PERMUTATION_SIZE;
		} else {
			SlotFillingExplorer.MAX_NUMBER_OF_ANNOTATIONS = 100;
			RootTemplateCardinalityExplorer.MAX_NUMBER_OF_ANNOTATIONS = 100;
		}
		this.scoreType = scoreType;
		this.evaluator = evaluator;
	}

	@Override
	public void score(State state) {
		state.setObjectiveScore(state.score(evaluator, scoreType).getF1());
		state.score(evaluator, EScoreType.MACRO);
		state.score(evaluator, EScoreType.MICRO);
	}

	@Override
	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

	@Override
	public AbstractEvaluator getEvaluator() {
		return evaluator;
	}

}

package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

public class NerlaObjectiveFunction implements IObjectiveFunction {

	final private NerlaEvaluator evaluator;

	public NerlaObjectiveFunction(EEvaluationDetail evaluationDetail) {
		this.evaluator = new NerlaEvaluator(evaluationDetail);
	}

	public NerlaObjectiveFunction() {
		this(EEvaluationDetail.DOCUMENT_LINKED);
	}

	@Override
	public void score(State state) {
		state.setObjectiveScore(state.score(evaluator).getMicroScore().getF1());
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

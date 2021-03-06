package de.hterhors.semanticmr.crf.of;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

public interface IObjectiveFunction {

	public void score(State currentState);

	public void score(List<State> proposalStates);

	public AbstractEvaluator getEvaluator();
}

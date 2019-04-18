package de.hterhors.semanticmr.crf.exploration;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

public interface IExplorationStrategy {

	public List<State> explore(State currentState);

}

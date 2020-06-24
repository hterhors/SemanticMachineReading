package de.hterhors.semanticmr.crf.exploration;

import java.util.Iterator;
import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

public interface IExplorationStrategy extends Iterator<State> {

	public List<State> explore(State currentState);

	public void set(int sentenceIndex);
}

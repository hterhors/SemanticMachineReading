package de.hterhors.semanticmr.crf;

import de.hterhors.semanticmr.crf.variables.State;

public class StatePair {

	public final State currentState;
	public final State candidateState;

	public StatePair(State currentState, State candidateState) {
		this.currentState = currentState;
		this.candidateState = candidateState;
	}

}

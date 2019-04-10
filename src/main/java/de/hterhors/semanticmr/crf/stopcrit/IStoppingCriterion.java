package de.hterhors.semanticmr.crf.stopcrit;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

public interface IStoppingCriterion {

	public boolean checkCondition(final List<State> producedStateChain);

}

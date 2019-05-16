package de.hterhors.semanticmr.crf.sampling.stopcrit;

import java.util.Collection;

import de.hterhors.semanticmr.crf.variables.State;

public interface ITrainingStoppingCriterion {

	public boolean meetsCondition(Collection<State> producedStateChain);

}

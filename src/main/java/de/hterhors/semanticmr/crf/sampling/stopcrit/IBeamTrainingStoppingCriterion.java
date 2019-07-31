package de.hterhors.semanticmr.crf.sampling.stopcrit;

import java.util.Collection;
import java.util.List;

import de.hterhors.semanticmr.crf.variables.State;

public interface IBeamTrainingStoppingCriterion {

	public boolean meetsCondition(Collection<List<State>> collection);

}

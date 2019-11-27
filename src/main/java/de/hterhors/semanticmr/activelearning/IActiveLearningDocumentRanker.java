package de.hterhors.semanticmr.activelearning;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.Instance;

public interface IActiveLearningDocumentRanker {

	List<Instance> rank(List<Instance> remainingInstances);

}

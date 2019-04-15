package de.hterhors.semanticmr.corpus.distributor;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.Instance;


public abstract class AbstractCorpusDistributor implements IInstanceDistributor {
	protected static final float DEFAULT_CORPUS_SIZE_FRACTION = 1F;

	final public float corpusSizeFraction;

	public AbstractCorpusDistributor(final float corpusSizeFraction) {
		if (corpusSizeFraction > 0) {
			this.corpusSizeFraction = corpusSizeFraction;
		} else {
			this.corpusSizeFraction = 1F;
		}
	}

	public static interface IDistributorStrategy {

		IDistributorStrategy distributeTrainingInstances(List<Instance> trainingInstances);

		IDistributorStrategy distributeDevelopmentInstances(List<Instance> developmentInstances);

		IDistributorStrategy distributeTestInstances(List<Instance> testInstances);
	}

}

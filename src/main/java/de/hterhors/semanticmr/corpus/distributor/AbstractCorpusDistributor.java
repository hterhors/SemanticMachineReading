package de.hterhors.semanticmr.corpus.distributor;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * Abstract class for a corpus distributor.
 * 
 * @author hterhors
 *
 */
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

	/**
	 * 
	 * @author hterhors
	 *
	 * @param <B>
	 */
	static public abstract class AbstractCorpusDistributorConfigBuilder<B extends AbstractCorpusDistributorConfigBuilder<B>> {

		float corpusSizeFraction = DEFAULT_CORPUS_SIZE_FRACTION;

		public abstract AbstractCorpusDistributor build();

		public float getCorpusSizeFraction() {
			return corpusSizeFraction;
		}

		public B setCorpusSizeFraction(float corpusSizeFraction) {
			this.corpusSizeFraction = corpusSizeFraction;
			return getDistributor();
		}

		protected abstract B getDistributor();

	}

}

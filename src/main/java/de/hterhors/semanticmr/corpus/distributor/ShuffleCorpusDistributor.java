package de.hterhors.semanticmr.corpus.distributor;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * Merges training, development and test data shuffles them and redistributes
 * the data according to the specification in the setting.
 * 
 * @author hterhors
 *
 * @param <T>
 * @date Oct 13, 2017
 */
public class ShuffleCorpusDistributor extends AbstractCorpusDistributor {

	/**
	 * The proportion of the training data.
	 */
	public final int trainingProportion;

	/**
	 * The proportion of the development data.
	 */
	public final int developmentProportion;

	/**
	 * The proportion of the test data.
	 */
	public final int testProportion;

	/**
	 * The random to shuffle the documents.
	 */
	public final Random rnd;

	/**
	 * The seed that was used to initialize the random.
	 */
	public final long seed;

	private ShuffleCorpusDistributor(float corpusSizeFraction, int trainingProportion, int developmentProportion,
			int testProportion, long seed) {
		super(corpusSizeFraction);

		this.trainingProportion = trainingProportion;
		this.developmentProportion = developmentProportion;
		this.testProportion = testProportion;
		this.seed = seed;
		this.rnd = new Random(seed);
	}

	private int proportionSum() {
		return trainingProportion + developmentProportion + testProportion;
	}

	public int numberOfTrainingData(final int totalNumberOfDocuments) {
		return Math.round(
				corpusSizeFraction * (((float) trainingProportion / (float) proportionSum()) * totalNumberOfDocuments));
	}

	public int numberOfDevelopmentData(final int totalNumberOfDocuments) {
		return Math.round(corpusSizeFraction
				* (((float) developmentProportion / (float) proportionSum()) * totalNumberOfDocuments));
	}

	public int numberOfTestData(final int totalNumberOfDocuments) {
		return Math.round(
				corpusSizeFraction * (((float) testProportion / (float) proportionSum()) * totalNumberOfDocuments));
	}

	@Override
	public String toString() {
		return "ShuffleCorpusConfig [trainingProportion=" + trainingProportion + ", developmentProportion="
				+ developmentProportion + ", testProportion=" + testProportion + ", rnd=" + rnd + ", seed=" + seed
				+ "]";
	}

	public static class Builder extends AbstractCorpusDistributorConfigBuilder<Builder> {

		/**
		 * The proportion of the training data.
		 */
		int trainingProportion = 0;

		/**
		 * The proportion of the development data.
		 */
		int developmentProportion = 0;

		/**
		 * The proportion of the test data.
		 */
		int testProportion = 0;

		/**
		 * The seed that was used to initialize the random.
		 */
		long seed = new Random().nextLong();

		public Builder setTrainingProportion(int trainingProportion) {
			this.trainingProportion = trainingProportion;
			return this;

		}

		public Builder setDevelopmentProportion(int developmentProportion) {
			this.developmentProportion = developmentProportion;
			return this;

		}

		public Builder setTestProportion(int testProportion) {
			this.testProportion = testProportion;
			return this;

		}

		public Builder setSeed(long seed) {
			this.seed = seed;
			return this;

		}

		public int getTrainingProportion() {
			return trainingProportion;
		}

		public int getDevelopmentProportion() {
			return developmentProportion;
		}

		public int getTestProportion() {
			return testProportion;
		}

		public long getSeed() {
			return seed;
		}

		@Override
		public ShuffleCorpusDistributor build() {
			return new ShuffleCorpusDistributor(corpusSizeFraction, trainingProportion, developmentProportion,
					testProportion, seed);
		}

		@Override
		protected Builder getDistributor() {
			return this;
		}

	}

	/**
	 * 
	 * Builds a new corpus for training development and test based on the input
	 * documents. All documents are shuffled and redistributed according to the
	 * configuration.
	 * 
	 * @param config
	 * @param trainingDocuments
	 * @param developmentDocuments
	 * @param testDocuments
	 * @param investigationRestriction
	 */
	@Override
	public IDistributorStrategy distributeInstances(InstanceProvider corpusProvider) {

		Collections.sort(corpusProvider.getInstances());
		Collections.shuffle(corpusProvider.getInstances(), rnd);

		final int totalNumberOfDocuments = corpusProvider.getInstances().size();

		final int numberForTraining = numberOfTrainingData(totalNumberOfDocuments);
		final int numberForDevelopment = numberOfDevelopmentData(totalNumberOfDocuments);
		final int numberForTest = numberOfTestData(totalNumberOfDocuments);

		return new IDistributorStrategy() {

			@Override
			public IDistributorStrategy distributeTrainingInstances(List<Instance> trainingDocuments) {
				trainingDocuments.addAll(corpusProvider.getInstances().subList(0, numberForTraining));
				return this;
			}

			@Override
			public IDistributorStrategy distributeDevelopmentInstances(List<Instance> developmentDocuments) {
				developmentDocuments.addAll(corpusProvider.getInstances().subList(numberForTraining,
						numberForTraining + numberForDevelopment));
				return this;
			}

			@Override
			public IDistributorStrategy distributeTestInstances(List<Instance> testDocuments) {
				testDocuments.addAll(corpusProvider.getInstances().subList(numberForTraining + numberForDevelopment,
						numberForTraining + numberForDevelopment + numberForTest));
				return this;
			}
		};
	}

	@Override
	public String getDistributorID() {
		return "Shuffle";
	}

}

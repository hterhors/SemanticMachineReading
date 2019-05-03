package de.hterhors.semanticmr.corpus.distributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * Takes the original distribution of documents into training, development and
 * testing.
 * 
 * @author hterhors
 *
 * @param <T>
 * @date Oct 13, 2017
 */
public class OriginalCorpusDistributor extends AbstractCorpusDistributor {

	private OriginalCorpusDistributor(final float corpusSizeFraction) {
		super(corpusSizeFraction);
	}

	public static class Builder extends AbstractCorpusDistributorConfigBuilder<Builder> {

		@Override
		public OriginalCorpusDistributor build() {
			return new OriginalCorpusDistributor(corpusSizeFraction);
		}

		@Override
		protected Builder getDistributor() {
			return this;
		}
	}

	/**
	 * Builds the original distributed corpus from the raw data. Keeps the training,
	 * develop and test instances.
	 * 
	 * 
	 * @param config
	 * @param trainingInstances        to fill
	 * @param developmentInstances     to fill
	 * @param testInstances            to fill
	 * @param investigationRestriction
	 */
	@Override
	public IDistributorStrategy distributeInstances(InstanceProvider corpusProvider) {

		/**
		 * TODO: Not very efficient! Convert interalINstances to Map with name as key.
		 */
		return new IDistributorStrategy() {

			private void sortAndShuffleIf(List<Instance> l) {
				if (corpusSizeFraction != 1.0F) {

					/*
					 * Ensure same order on shuffle, if fraction size is not equals 1;
					 */
					Collections.sort(l);

					/*
					 * Shuffle to not always get the first same elements based on the name.
					 */
					Collections.shuffle(l, new Random(987654321L));
				}
			}

			@Override
			public IDistributorStrategy distributeTrainingInstances(List<Instance> trainingDocuments) {

				List<Instance> l = new ArrayList<>(corpusProvider.getOriginalTrainingInstances());

				sortAndShuffleIf(l);

				for (Instance instance : l) {
					final float fraction = (float) trainingDocuments.size()
							/ corpusProvider.getOriginalTrainingInstances().size();

					if (fraction >= corpusSizeFraction)
						break;

					/*
					 * As we work with the real distribution given the raw corpus,
					 * allExistingInternalInstances may not contain the document if it violates
					 * previous restrictions.
					 */
					trainingDocuments.add(instance);
				}
				return this;
			}

			@Override
			public IDistributorStrategy distributeDevelopmentInstances(List<Instance> developmentDocuments) {

				List<Instance> l = new ArrayList<>(corpusProvider.getOriginalDevelopInstances());

				sortAndShuffleIf(l);

				for (Instance instance : l) {
					final float fraction = (float) developmentDocuments.size()
							/ corpusProvider.getOriginalDevelopInstances().size();

					if (fraction >= corpusSizeFraction)
						break;

					/*
					 * As we work with the real distribution given the raw corpus,
					 * allExistingInternalInstances may not contain the document if it violates
					 * previous restrictions.
					 */
					developmentDocuments.add(instance);
				}
				return this;
			}

			@Override
			public IDistributorStrategy distributeTestInstances(List<Instance> testDocuments) {

				List<Instance> l = new ArrayList<>(corpusProvider.getOriginalTestInstances());

				sortAndShuffleIf(l);

				for (Instance instance : l) {
					final float fraction = (float) testDocuments.size()
							/ corpusProvider.getOriginalTestInstances().size();

					if (fraction >= corpusSizeFraction)
						break;

					/*
					 * As we work with the real distribution given the raw corpus,
					 * allExistingInternalInstances may not contain the document if it violates
					 * previous restrictions.
					 */
					testDocuments.add(instance);
				}
				return this;
			}
		};
	}

	@Override
	public String getDistributorID() {
		return "Origin";
	}

}

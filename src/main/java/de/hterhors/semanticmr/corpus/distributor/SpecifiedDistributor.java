package de.hterhors.semanticmr.corpus.distributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
public class SpecifiedDistributor extends AbstractCorpusDistributor {

	private final List<String> trainingInstanceNames;
	private final List<String> developInstanceNames;
	private final List<String> testInstanceNames;

	private SpecifiedDistributor(float corpusSizeFraction, List<String> trainingInstanceNames,
			List<String> developInstanceNames, List<String> testInstanceNames) {
		super(corpusSizeFraction);
		this.trainingInstanceNames = trainingInstanceNames;
		this.developInstanceNames = developInstanceNames;
		this.testInstanceNames = testInstanceNames;
	}

	public static class Builder extends AbstractCorpusDistributorConfigBuilder<Builder> {

		private List<String> trainingInstanceNames = new ArrayList<>();
		private List<String> developInstanceNames = new ArrayList<>();
		private List<String> testInstanceNames = new ArrayList<>();

		public List<String> getTrainingInstanceNames() {
			return trainingInstanceNames;
		}

		public Builder setTrainingInstanceNames(List<String> trainingInstanceNames) {
			this.trainingInstanceNames = trainingInstanceNames;
			return this;
		}

		public List<String> getDevelopInstanceNames() {
			return developInstanceNames;
		}

		public Builder setDevelopInstanceNames(List<String> developInstanceNames) {
			this.developInstanceNames = developInstanceNames;
			return this;
		}

		public List<String> getTestInstanceNames() {
			return testInstanceNames;
		}

		public Builder setTestInstanceNames(List<String> testInstanceNames) {
			this.testInstanceNames = testInstanceNames;
			return this;
		}

		@Override
		public SpecifiedDistributor build() {
			return new SpecifiedDistributor(1F, trainingInstanceNames, developInstanceNames, testInstanceNames);
		}

		@Override
		public Builder setCorpusSizeFraction(float corpusSizeFraction) {
			throw new UnsupportedOperationException("Can not set corpus size fraction in this distributor.");
		}

		@Override
		protected Builder getDistributor() {
			return this;
		}
	}

	@Override
	public IDistributorStrategy distributeInstances(InstanceProvider corpusProvider) {

		return new IDistributorStrategy() {

			@Override
			public IDistributorStrategy distributeTrainingInstances(List<Instance> trainingDocuments) {

				trainingDocuments.addAll(corpusProvider.getInstances().stream()
						.filter(i -> trainingInstanceNames.contains(i.getName())).collect(Collectors.toList()));

				return this;
			}

			@Override
			public IDistributorStrategy distributeDevelopmentInstances(List<Instance> developmentDocuments) {

				developmentDocuments.addAll(corpusProvider.getInstances().stream()
						.filter(i -> developInstanceNames.contains(i.getName())).collect(Collectors.toList()));
				return this;
			}

			@Override
			public IDistributorStrategy distributeTestInstances(List<Instance> testDocuments) {

				testDocuments.addAll(corpusProvider.getInstances().stream()
						.filter(i -> testInstanceNames.contains(i.getName())).collect(Collectors.toList()));
				return this;
			}
		};
	}

	@Override
	public String getDistributorID() {
		return "Specified";
	}

}

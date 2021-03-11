package de.hterhors.semanticmr.corpus.distributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public final Set<String> instanceNames = new HashSet<>();
	public final boolean filter;

	private SpecifiedDistributor(boolean filter, float corpusSizeFraction, List<String> trainingInstanceNames,
			List<String> developInstanceNames, List<String> testInstanceNames) {
		super(corpusSizeFraction);
		this.trainingInstanceNames = trainingInstanceNames;
		this.developInstanceNames = developInstanceNames;
		this.testInstanceNames = testInstanceNames;
		this.instanceNames.addAll(trainingInstanceNames);
		this.instanceNames.addAll(developInstanceNames);
		this.instanceNames.addAll(testInstanceNames);
		this.filter = filter;
	}

	public static class Builder extends AbstractCorpusDistributorConfigBuilder<Builder> {

		private List<String> trainingInstanceNames = new ArrayList<>();
		private List<String> developInstanceNames = new ArrayList<>();
		private List<String> testInstanceNames = new ArrayList<>();
		private boolean filter = false;

		public boolean isFilter() {
			return filter;
		}

		public Builder setFilter(boolean filter) {
			this.filter = filter;
			return this;
		}

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
			return new SpecifiedDistributor(filter, 1F, trainingInstanceNames, developInstanceNames, testInstanceNames);
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
	public IDistributorStrategy distributeInstances(List<Instance> instancesToRedistribute) {

		return new IDistributorStrategy() {

			@Override
			public IDistributorStrategy distributeTrainingInstances(List<Instance> trainingDocuments) {

				for (String name : trainingInstanceNames) {
					for (Instance instance : instancesToRedistribute) {
						if (instance.getName().equals(name))
							trainingDocuments.add(instance);
					}

				}

				return this;
			}

			@Override
			public IDistributorStrategy distributeDevelopmentInstances(List<Instance> developmentDocuments) {

				for (String name : developInstanceNames) {
					for (Instance instance : instancesToRedistribute) {
						if (instance.getName().equals(name))
							developmentDocuments.add(instance);
					}

				}

				return this;
			}

			@Override
			public IDistributorStrategy distributeTestInstances(List<Instance> testDocuments) {
				for (String name : testInstanceNames) {
					for (Instance instance : instancesToRedistribute) {
						if (instance.getName().equals(name))
							testDocuments.add(instance);
					}

				}
				return this;
			}

		};
	}

	@Override
	public String getDistributorID() {
		return "Specified";
	}

}

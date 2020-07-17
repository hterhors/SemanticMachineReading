package de.hterhors.semanticmr.crf;

import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public interface ISemanticParsingCRF {
	static final String COVERAGE_CONTEXT = "===========COVERAGE============\n";
	static final String TRAIN_CONTEXT = "===========TRAIN============\n";
	static final String TEST_CONTEXT = "===========TEST============\n";

	static class CRFStatistics {
		private final String context;

		long startTrainingTime;
		long endTrainingTime;

		public CRFStatistics(String context) {
			this.context = context;
		}

		long getTotalDuration() {
			return endTrainingTime - startTrainingTime;
		}

		@Override
		public String toString() {
			return "CRFStatistics [context=" + context + ", getTotalDuration()=" + getTotalDuration() + "]";
		}

	}

	public void setInitializer(IStateInitializer initializer);

	public Map<Instance, State> train(AdvancedLearner newLearner, List<Instance> trainingInstances, int numberOfEpochs,
			ISamplingStoppingCriterion[] sampleStoppingCrits);

	public Map<Instance, State> predict(List<Instance> testInstances, ISamplingStoppingCriterion... maxStepCrit);

	public CRFStatistics getTestStatistics();

	public CRFStatistics getTrainingStatistics();

	public Map<Instance, State> predictHighRecall(List<Instance> collect, int n, ISamplingStoppingCriterion ... samplingCrits);

}

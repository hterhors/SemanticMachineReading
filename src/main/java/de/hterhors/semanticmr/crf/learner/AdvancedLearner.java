package de.hterhors.semanticmr.crf.learner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.learner.optimizer.Optimizer;
import de.hterhors.semanticmr.crf.learner.regularizer.Regularizer;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.VectorUtil;

/**
 * This learner implements a margin rank learning scheme with modular parameter
 * optimization and regularization.
 * 
 */
public class AdvancedLearner {

	private double margin = 0.1;

	private Optimizer optimizer;
	private Regularizer regularizer;

	/**
	 * This implementation of the learner implements the SampleRank learning scheme.
	 * Very generally speaking, given a pair of states, the learner changes the
	 * weights of the model such that the scores of the model are aligned with the
	 * preference of the objective function. As a slight modification, this
	 * implementation allows mini-batch updates.
	 * 
	 * @param model
	 * @param alpha
	 */
	public AdvancedLearner(Optimizer optimizer) {
		this.optimizer = optimizer;
	}

	public AdvancedLearner(Optimizer optimizer, Regularizer regularizer) {
		this.optimizer = optimizer;
		this.regularizer = regularizer;
	}

	public AdvancedLearner(Optimizer optimizer, Regularizer regularizer, double margin) {
		this.optimizer = optimizer;
		this.regularizer = regularizer;
		this.margin = margin;
	}

	/**
	 * Performs a model update according to a learning scheme (currently
	 * SampleRank). The update step is scaled with the provided alpha value.
	 * 
	 * @param currentState
	 * @param possibleNextState
	 */
	public void updateWeights(List<AbstractFactorTemplate> factorTemplates, State currentState,
			State possibleNextState) {

		collectMarginRankGradientsAndApply(factorTemplates, currentState, possibleNextState, 1);

	}

	/**
	 * Updates the weights of each template according to the Margin Rank scheme.
	 * 
	 * @param t2
	 * @param currentState
	 * 
	 * @param currentState
	 * @param possibleNextState
	 * @param batchGradients
	 * @param i
	 */
	private void collectMarginRankGradientsAndApply(List<AbstractFactorTemplate> factorTemplates,
			final State currentState, final State possibleNextState, int sampleWeight) {

		double linearScore = 0;
		/*
		 * Collect differences of features for both states and remember respective
		 * template
		 */
		final Map<AbstractFactorTemplate, DoubleVector> featureDifferencesPerTemplate = new HashMap<>(
				factorTemplates.size());

		final State posState;
		final State negState;

		if (preference(possibleNextState, currentState)) {
			// possibleNextState is POS
			// currentState is NEG
			posState = possibleNextState;
			negState = currentState;
		} else {
			// currentState is POS
			// possibleNextState is NEG
			posState = currentState;
			negState = possibleNextState;
		}

		for (AbstractFactorTemplate t : factorTemplates) {
			DoubleVector differences = VectorUtil.getFeatureDifferences(t, negState, posState);
			featureDifferencesPerTemplate.put(t, differences);
			linearScore += differences.dotProduct(t.getWeights());
			if (regularizer != null) {
				linearScore -= regularizer.penalize(t.getWeights());
			}
		}

		if (linearScore + margin >= 0) {
			/*
			 * gradient for weight w[i] is simply featureDifference[i].
			 */
			for (AbstractFactorTemplate t : factorTemplates) {
				final DoubleVector weightGradient;

				if (sampleWeight == 0) {
					weightGradient = new DoubleVector();
				} else {
					weightGradient = featureDifferencesPerTemplate.get(t);

					if (regularizer != null) {
						regularizer.regularize(weightGradient, t.getWeights());
					}

					if (sampleWeight != 1) {
						weightGradient.mul(sampleWeight);
					}
				}
//				batchGradients.get(t).add(weightGradient);
				/**
				 * Applies the previously collected weight updates in one step.
				 */
				optimizer.applyUpdates(t.getWeights(), weightGradient);

			}
		}
	}

	/**
	 * Compares the objective scores of the state1 and state2 using the precomputed
	 * objective scores to decide if state1 is preferred over state2. Note: The
	 * objective scores are merely accessed but not recomputed. This step needs to
	 * be done before.
	 * 
	 * @param state1
	 * @param state2
	 * @param goldState
	 * @return
	 */
	private boolean preference(State state1, State state2) {
		double O1 = state1.getObjectiveScore();
		double O2 = state2.getObjectiveScore();
		return O1 > O2;
	}

}

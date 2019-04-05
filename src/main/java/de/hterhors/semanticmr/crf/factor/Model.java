package de.hterhors.semanticmr.crf.factor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.crf.variables.Vector;

public class Model {

	final private static FactorPool FACTOR_POOL_INSTANCE = FactorPool.getInstance();

	final private List<AbstractFactorTemplate> factorTemplates;

	final private AdvancedLearner learner;

	public Model(List<AbstractFactorTemplate> factorTemplates, AdvancedLearner learner) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
		this.learner = learner;
	}

	public void score(List<State> states) {

		final Set<FactorScope> cachedFactorScopes = FACTOR_POOL_INSTANCE.getCachedFactorScopes();

		for (AbstractFactorTemplate template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			states.parallelStream()
					.forEach(state -> state.getFactorGraph().addFactorScopes(template.generateFactorScopes(state)));

			/*
			 * Compute all selected factors in parallel.
			 */
			states.stream().flatMap(state -> state.getFactorGraph().getFactorScopes().stream()).parallel()
					.filter(fs -> !cachedFactorScopes.contains(fs)).map(remainingFactorScope -> {
						Factor f = new Factor(remainingFactorScope);
						template.computeFeatureVector(f);
						return f;
					}).sequential().forEach(factor -> FACTOR_POOL_INSTANCE.addFactor(factor));
		}

		states.parallelStream()
				.forEach(state -> state.setModelScore(computeScore(state.getFactorGraph().getFactors())));

	}

	/**
	 * Computes the score of this state according to the trained model. The computed
	 * score is returned but also updated in the state objects <i>score</i> field.
	 * 
	 * @param list
	 * @return
	 */

	protected double computeScore(List<Factor> factors) {

		if (factors.size() == 0)
			return 0;

		double score = 1;
		for (Factor factor : factors) {
			Vector featureVector = factor.getFeatureVector();
			Vector weights = factor.getTemplate().getWeights();
			double dotProduct = featureVector.dotProduct(weights);
			double factorScore = Math.exp(dotProduct);
			score *= factorScore;
		}

		return score;

	}

	public void updateWeights(final State currentState, final State candidateState) {
		this.learner.updateWeights(this.factorTemplates, currentState, candidateState);
	}

	@Override
	public String toString() {
		factorTemplates.get(0).getWeights().getFeatures().entrySet().forEach(System.out::println);
		return "";
	}

}

package de.hterhors.semanticmr.crf.factor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;

public class Model {

	final private static FactorPool FACTOR_POOL_INSTANCE = FactorPool.getInstance();

	/**
	 * converts a feature name to its index.
	 */
	private final static Map<String, Integer> featureNameIndex = new ConcurrentHashMap<>();

	/**
	 * converts an index to its feature name.
	 */
	private final static Map<Integer, String> indexFeatureName = new ConcurrentHashMap<>();

	public static Integer getIndexForFeatureName(String feature) {
		Integer index;

		if ((index = featureNameIndex.get(feature)) != null) {
			return index;
		}

		index = new Integer(featureNameIndex.size());
		featureNameIndex.put(feature, index);
		indexFeatureName.put(index, feature);

		return index;
	}

	public static String getFeatureForIndex(Integer feature) {
		return indexFeatureName.get(feature);
	}

	final private List<AbstractFactorTemplate> factorTemplates;

	final private AdvancedLearner learner;

	public Model(List<AbstractFactorTemplate> factorTemplates, AdvancedLearner learner) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
		this.learner = learner;
	}

	public void score(List<State> states) {

		final Set<FactorScope> cachedFactorScopes = FACTOR_POOL_INSTANCE.getCachedFactorScopes();
		/**
		 * TODO: measure efficiency
		 */
		for (AbstractFactorTemplate template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			states.parallelStream().forEach(
					state -> state.getFactorGraph(template).addFactorScopes(template.generateFactorScopes(state)));

			/*
			 * Compute all selected factors in parallel.
			 */
			states.stream().flatMap(state -> state.getFactorGraphs().stream())
					.flatMap(l -> l.getFactorScopes().stream()).parallel()
					.filter(fs -> !cachedFactorScopes.contains(fs)).map(remainingFactorScope -> {
						Factor f = new Factor(remainingFactorScope);
						template.computeFeatureVector(f);
						return f;
					}).sequential().forEach(factor -> FACTOR_POOL_INSTANCE.addFactor(factor));
		}

		states.parallelStream().forEach(state -> state.setModelScore(computeScore(state)));

	}

	/**
	 * Computes the score of this state according to the trained model. The computed
	 * score is returned but also updated in the state objects <i>score</i> field.
	 * 
	 * @param list
	 * @return
	 */

	private double computeScore(State state) {

		double score = 1;
		for (FactorGraph abstractFactorTemplate : state.getFactorGraphs()) {
			final List<Factor> factors = abstractFactorTemplate.getFactors();

			if (factors.size() == 0)
				return 0;

			for (Factor factor : factors) {
				DoubleVector featureVector = factor.getFeatureVector();
				DoubleVector weights = factor.getTemplate().getWeights();
				double dotProduct = featureVector.dotProduct(weights);
				double factorScore = Math.exp(dotProduct);
				score *= factorScore;
			}

		}
		return score;

	}

//	protected double computeScore(List<Factor> factors) {
//
//		if (factors.size() == 0)
//			return 0;
//
//		double score = 1;
//		for (Factor factor : factors) {
//			DoubleVector featureVector = factor.getFeatureVector();
//			DoubleVector weights = factor.getTemplate().getWeights();
//			double dotProduct = featureVector.dotProduct(weights);
//			double factorScore = Math.exp(dotProduct);
//			score *= factorScore;
//		}
//
//		return score;
//
//	}

	public void updateWeights(final State currentState, final State candidateState) {
		this.learner.updateWeights(this.factorTemplates, currentState, candidateState);
	}

	@Override
	public String toString() {
		factorTemplates.get(0).getWeights().getFeatures().entrySet().forEach(System.out::println);
		return "";
	}

}

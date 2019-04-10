package de.hterhors.semanticmr.crf.factor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

public class Model {

	final private static FactorPool FACTOR_POOL_INSTANCE = FactorPool.getInstance();

	/**
	 * Converts a feature name to its index.
	 */
	private final static Map<String, Integer> featureNameIndex = new ConcurrentHashMap<>();

	/**
	 * Converts an index to its feature name.
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

	final private List<AbstractFeatureTemplate<?>> factorTemplates;

	final private AdvancedLearner learner;

	public Model(List<AbstractFeatureTemplate<?>> factorTemplates, AdvancedLearner learner) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
		this.learner = learner;
	}

	public void score(State state) {

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			collectFactorScopesForState(template, state);

			/*
			 * Compute all selected factors in parallel.
			 */
			computeRemainingFactors(template,
					state.getFactorGraphs().stream().flatMap(l -> l.getFactorScopes().stream()));
		}

		/*
		 * Compute and set model score
		 */
		computeAndSetModelScore(state);

	}

	public void score(List<State> states) {

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			states.parallelStream().forEach(state -> collectFactorScopesForState(template, state));

			/*
			 * Compute all selected factors in parallel.
			 */
			computeRemainingFactors(template, states.stream().flatMap(state -> state.getFactorGraphs().stream())
					.flatMap(l -> l.getFactorScopes().stream()));
		}
		/*
		 * Compute and set model score
		 */
		states.parallelStream().forEach(state -> computeAndSetModelScore(state));

	}

	private void computeAndSetModelScore(State state) {
		state.setModelScore(computeScore(state));
	}

	@SuppressWarnings("unchecked")
	private void computeRemainingFactors(AbstractFeatureTemplate<?> template, Stream<AbstractFactorScope> stream) {
		stream.parallel().filter(fs -> !FACTOR_POOL_INSTANCE.containsFactorScope(fs)).map(remainingFactorScope -> {
			@SuppressWarnings({ "rawtypes" })
			Factor f = new Factor(remainingFactorScope);
			template.generateFeatureVector(f);
			return f;
		}).sequential().forEach(factor -> FACTOR_POOL_INSTANCE.addFactor(factor));
	}

	private void collectFactorScopesForState(AbstractFeatureTemplate<?> template, State state) {
		state.getFactorGraph(template).addFactorScopes(template.generateFactorScopes(state));
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
		boolean factorsAvailable = false;
		for (FactorGraph abstractFactorTemplate : state.getFactorGraphs()) {

			final List<Factor<?>> factors = abstractFactorTemplate.getFactors();

			factorsAvailable |= factors.size() != 0;

			for (Factor<?> factor : factors) {
				score *= factor.computeScalarScore();
			}

		}
		if (factorsAvailable)
			return score;

		return 0;

	}

	public void updateWeights(final State currentState, final State candidateState) {
		this.learner.updateWeights(this.factorTemplates, currentState, candidateState);
	}

	@Override
	public String toString() {
		factorTemplates.get(0).getWeights().getFeatures().entrySet()
				.forEach(f -> System.out.println(indexFeatureName.get(f.getKey()) + ":" + f.getValue()));
		return "";
	}

}

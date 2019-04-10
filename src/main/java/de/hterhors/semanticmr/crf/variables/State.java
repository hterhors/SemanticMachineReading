package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.factor.FactorGraph;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public class State {

	private static final double DEFAULT_OBJECTIVE_SCORE = 0;

	private static final double DEFAULT_MODEL_SCORE = 1.0;

	final public EntityTemplate goldEntityTemplate;

	final public EntityTemplate currentPredictedEntityTemplate;

	final private Map<AbstractFactorTemplate, FactorGraph> factorGraph;

	private double modelScore = DEFAULT_MODEL_SCORE;

	private double objectiveScore = DEFAULT_OBJECTIVE_SCORE;

	/**
	 * Creates a new deep copy of the state except of the factor graph and the new
	 * prediction.
	 * 
	 * @param goldEntityTemplate
	 * @param currentPredictedEntityTemplate
	 * @param modelScore
	 * @param objectiveScore
	 */
	private State(EntityTemplate goldEntityTemplate, EntityTemplate currentPredictedEntityTemplate, double modelScore,
			double objectiveScore) {
		this.goldEntityTemplate = goldEntityTemplate;
		this.currentPredictedEntityTemplate = currentPredictedEntityTemplate;
		this.modelScore = modelScore;
		this.objectiveScore = objectiveScore;
		this.factorGraph = new HashMap<>();
	}

	public State(EntityTemplate goldEntityTemplate, EntityTemplate currentPredictedEntityTemplate) {
		this.goldEntityTemplate = goldEntityTemplate;
		this.currentPredictedEntityTemplate = currentPredictedEntityTemplate;
		this.factorGraph = new HashMap<>();
	}

	public State deepUpdateCopy(EntityTemplate newCurrentPrediction) {
		return new State(goldEntityTemplate, newCurrentPrediction, DEFAULT_MODEL_SCORE, DEFAULT_OBJECTIVE_SCORE);
	}

	public FactorGraph getFactorGraph(final AbstractFactorTemplate template) {
		FactorGraph fg;
		if ((fg = factorGraph.get(template)) == null) {
			fg = new FactorGraph();
			factorGraph.put(template, fg);
		}

		return fg;
	}

	public double getModelScore() {
		return modelScore;
	}

	public double getObjectiveScore() {
		return objectiveScore;
	}

	public void setModelScore(double modelScore) {
		this.modelScore = modelScore;
	}

	public void setObjectiveScore(double objectiveScore) {
		this.objectiveScore = objectiveScore;
	}

	@Override
	public String toString() {
		return "State [currentPredictedEntityTemplate=" + currentPredictedEntityTemplate.toPrettyString()
				+ ", modelScore=" + modelScore + ", objectiveScore=" + objectiveScore + "]";
	}

	public Collection<FactorGraph> getFactorGraphs() {
		return factorGraph.values();
	}

}

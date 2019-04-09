package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.factor.FactorGraph;
import de.hterhors.semanticmr.crf.templates.AbstractFactorTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public class State {

	final public EntityTemplate goldEntityTemplate;

	final public EntityTemplate currentPredictedEntityTemplate;

	final private Map<AbstractFactorTemplate, FactorGraph> factorGraph = new HashMap<>();

	private double modelScore = 0;

	private double objectiveScore = 0;

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
	}

	public State(EntityTemplate goldEntityTemplate, EntityTemplate currentPredictedEntityTemplate) {
		this.goldEntityTemplate = goldEntityTemplate;
		this.currentPredictedEntityTemplate = currentPredictedEntityTemplate;
	}

	public State deepUpdateCopy(EntityTemplate newCurrentPrediction) {
		return new State(goldEntityTemplate, newCurrentPrediction, modelScore, objectiveScore);
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
				+ ", factorGraph=" + factorGraph + "]";
	}

	public Collection<FactorGraph> getFactorGraphs() {
		return factorGraph.values();
	}

}

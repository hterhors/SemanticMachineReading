package de.hterhors.semanticmr.crf.variables;

import de.hterhors.semanticmr.crf.factor.FactorGraph;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public class State {

	final public EntityTemplate goldEntityTemplate;

	final public EntityTemplate currentPredictedEntityTemplate;

	final private FactorGraph factorGraph = new FactorGraph();

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

	public FactorGraph getFactorGraph() {
		return factorGraph;
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

}

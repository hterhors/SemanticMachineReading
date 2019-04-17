package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.factor.FactorGraph;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.eval.EEvaluationMode;

public class State {

	private final Instance instance;

	private static final double DEFAULT_OBJECTIVE_SCORE = 0.0D;

	private static final double DEFAULT_MODEL_SCORE = 1.0D;

	final private Annotations currentPredictions;

	public Annotations getCurrentPredictions() {
		return currentPredictions;
	}

	final private Map<AbstractFeatureTemplate<?>, FactorGraph> factorGraphs;

	private double modelScore;

	private double objectiveScore;

	private Score score;

	public State(Instance instance, Annotations currentPredictions) {
		this.instance = instance;
		this.currentPredictions = currentPredictions;
		this.factorGraphs = new HashMap<>();
		this.modelScore = DEFAULT_MODEL_SCORE;
		this.objectiveScore = DEFAULT_OBJECTIVE_SCORE;
	}

	/**
	 * Creates a new State instance copying all of its properties except the model
	 * score, objective score and the current prediction. this is directly updated
	 * to the provided one.
	 * 
	 * @param newCurrentPrediction
	 * @return a new State instance
	 */
	public State deepUpdateCopy(final int annotationIndex,
			AbstractSlotFiller<? extends AbstractSlotFiller<?>> newCurrentPrediction) {
		return new State(this.instance, currentPredictions.deepUpdateCopy(annotationIndex, newCurrentPrediction));
	}

	public FactorGraph getFactorGraph(final AbstractFeatureTemplate<?> template) {
		FactorGraph fg;
		if ((fg = factorGraphs.get(template)) == null) {
			fg = new FactorGraph();
			factorGraphs.put(template, fg);
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
		return "State [modelScore=" + modelScore + ", objectiveScore=" + objectiveScore + ", goldAnnotation="
				+ instance.getGoldAnnotations() + ", currentPredictions=" + currentPredictions + ", instance="
				+ instance.getDocument().documentID + "]";
	}

	public Collection<FactorGraph> getFactorGraphs() {
		return factorGraphs.values();
	}

	public Annotations getGoldAnnotations() {
		return instance.getGoldAnnotations();
	}

	public Score score(EEvaluationMode evaluationMode) {
		this.score = instance.getGoldAnnotations().evaluate(evaluationMode, currentPredictions);
		return score;
	}

	public Score getScore() {
		return score;
	}

	public Instance getInstance() {
		return instance;
	}

}

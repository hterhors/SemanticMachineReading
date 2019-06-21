package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.factor.FactorGraph;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

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

	public State deepAddCopy(AbstractAnnotation newCurrentPrediction) {
		return new State(this.instance, currentPredictions.deepAddCopy(newCurrentPrediction));
	}

	/**
	 * Creates a new State instance copying all of its properties except the model
	 * score, objective score and the current prediction. this is directly updated
	 * to the provided one.
	 * 
	 * @param newCurrentPrediction
	 * @return a new State instance
	 */
	public State deepUpdateCopy(final int annotationIndex, AbstractAnnotation newCurrentPrediction) {
		return new State(this.instance, currentPredictions.deepUpdateCopy(annotationIndex, newCurrentPrediction));
	}

	public State deepRemoveCopy(int annotationIndex) {
		return new State(this.instance, currentPredictions.deepRemoveCopy(annotationIndex));
	}

	public FactorGraph getFactorGraph(final AbstractFeatureTemplate<?> template) {
		FactorGraph fg;
		if ((fg = factorGraphs.get(template)) == null) {
			fg = new FactorGraph(template);
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
		return "State [instance=" + instance.getDocument().documentID + ", modelScore=" + modelScore
				+ ", objectiveScore=" + objectiveScore + ", goldAnnotation=" + instance.getGoldAnnotations()
				+ ", currentPredictions=" + currentPredictions + "]";
	}

	public Collection<FactorGraph> getFactorGraphs() {
		return factorGraphs.values();
	}

	public Annotations getGoldAnnotations() {
		return instance.getGoldAnnotations();
	}

	public Score score(AbstractEvaluator evaluator) {
		this.score = instance.getGoldAnnotations().evaluate(evaluator, currentPredictions);
		return score;
	}

//	public Score score(IEvaluatable evaluator, EEvaluationDetail evaluationMode) {
//		this.score = evaluator.evaluate(evaluationMode, instance.getGoldAnnotations(), currentPredictions);
//		return score;
//	}

	public Score getScore() {
		return score;
	}

	public Instance getInstance() {
		return instance;
	}

	public boolean containsAnnotationOnTokens(DocumentToken... tokens) {
		return currentPredictions.containsAnnotationOnTokens(tokens);
	}

}

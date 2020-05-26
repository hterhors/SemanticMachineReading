package de.hterhors.semanticmr.crf.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.model.FactorGraph;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
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

	private Score microScore;
	private Score macroScore;

	public State(Instance instance, Annotations currentPredictions) {
		this.instance = instance;
		this.currentPredictions = currentPredictions;
		this.factorGraphs = new HashMap<>();
		this.modelScore = DEFAULT_MODEL_SCORE;
		this.objectiveScore = DEFAULT_OBJECTIVE_SCORE;
	}

	public State(State value) {
		this.instance = value.instance;
		this.currentPredictions = value.currentPredictions.deepCopy();
		this.factorGraphs = value.factorGraphs;
		this.modelScore = value.modelScore;
		this.objectiveScore = value.objectiveScore;
		this.microScore= value.microScore; 
		this.macroScore= value.macroScore; 
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

	public void setFactorGraph(final AbstractFeatureTemplate<?> template, final FactorGraph factorGraph) {
		factorGraphs.put(template, factorGraph);
	}

	public FactorGraph getFactorGraph(final AbstractFeatureTemplate<?> template) {
		return factorGraphs.get(template);
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

	private boolean isMicroScored = false;
	private boolean isMacroScored = false;

	public boolean isMacroScored() {
		return isMacroScored;
	}
	
	public boolean isMicroScored() {
		return isMicroScored;
	
	}
	public Score getMicroScore() {
		if (!isMicroScored)
			getMicroScore();
//			throw new IllegalStateException("State is not scored for micro score.");
		return microScore;
	}

	public Score getMicroScore(AbstractEvaluator evaluator) {
//		if (!isMicroScored) {
			this.microScore = instance.getGoldAnnotations().evaluate(evaluator, currentPredictions, EScoreType.MICRO);
			isMicroScored = true;
//		}
		return microScore;
	}

	public Score getMacroScore() {
		if (!isMacroScored)
			getMacroScore();
//			throw new IllegalStateException("State is not scored for macro score.");
		return macroScore;
	}

	public Score getMacroScore(AbstractEvaluator evaluator) {
//		if (!isMacroScored) {
			this.macroScore = instance.getGoldAnnotations().evaluate(evaluator, currentPredictions, EScoreType.MACRO);
			isMacroScored = true;
//		}
		return macroScore;
	}

	public Instance getInstance() {
		return instance;
	}

	public boolean containsAnnotationOnTokens(DocumentToken... tokens) {
		return currentPredictions.containsAnnotationOnTokens(tokens);
	}

	public Score score(AbstractEvaluator evaluator, EScoreType scoreType) {
		return scoreType == EScoreType.MICRO ? getMicroScore(evaluator) : getMacroScore(evaluator);
	}

}

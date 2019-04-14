package de.hterhors.semanticmr.crf.sampling.impl;

import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;

public class AcceptStrategies {
	/**
	 * Returns an accept function that accepts the candidate state if its model
	 * score is GREATER THAN the model score of the current state. In this case the
	 * returned accept function returns true.
	 * 
	 * @return
	 */
	public static AcceptStrategy strictModelAccept() {
		return (candidate, current) -> candidate.getModelScore() > current.getModelScore();
	};

	public static AcceptStrategy modelAccept() {
		return (candidate, current) -> candidate.getModelScore() >= current.getModelScore();
	};

	/**
	 * Returns an accept function that accepts the candidate state if its objective
	 * score is GREATER THAN the objective score of the current state. In this case
	 * the returned accept function returns true.
	 * 
	 * @return
	 */
	public static AcceptStrategy strictObjectiveAccept() {
		return (candidate, current) -> candidate.getObjectiveScore() > current.getObjectiveScore();
	}

	public static AcceptStrategy objectiveAccept() {
		return (candidate, current) -> candidate.getObjectiveScore() >= current.getObjectiveScore();
	};

}

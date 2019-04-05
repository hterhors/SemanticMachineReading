package de.hterhors.semanticmr.crf.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import de.hterhors.semanticmr.crf.variables.State;

public class SamplingUtils {

	private static Random rand = new Random(43921422342L);

	/**
	 * Selects a state from the given list according to the probability distribution
	 * defined by the states' (model/objective) scores. Each score is divided by the
	 * total sum of all scores, in order to create a probability distribution across
	 * states. If "softmax" is true, the probability distribution is computed using
	 * the softmax formula.
	 * 
	 * @param nextStates
	 * @param useModelDistribution
	 * @param softmax
	 * @return
	 */
	public static State drawFromDistribution(List<State> nextStates, boolean useModelDistribution) {
		Function<State, Double> toScore = null;
		if (useModelDistribution) {
			toScore = s -> s.getModelScore();
		} else {
			toScore = s -> s.getObjectiveScore();
		}
		Function<Double, Double> toProbability = d -> d;
		// compute total sum of scores
		double totalSum = 0;
		for (State s : nextStates) {
			double prob = 0;
			if (useModelDistribution) {
				prob = toProbability.apply(toScore.apply(s));
			} else {
				prob = toProbability.apply(toScore.apply(s));
			}
			totalSum += prob;
		}
		double randomIndex = rand.nextDouble() * totalSum;
		double sum = 0;
		int i = 0;
		while (sum < randomIndex) {
			if (useModelDistribution) {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			} else {
				sum += toProbability.apply(toScore.apply(nextStates.get(i++)));
			}
		}
		State state = nextStates.get(Math.max(0, i - 1));
		return state;

	}

	/**
	 * This function decides if the currentState should be replaced with the
	 * selectedNextState. The decision is based on the scores (model or objective)
	 * of both states. Currently, it implements the accept function from the
	 * Metropolis Hastings algorithm.
	 * 
	 * @param candidateState
	 * @param currentState
	 * @param useModelDistribution
	 * @return
	 */
	public static boolean accept(State candidateState, State currentState, boolean useModelDistribution) {
		double pCurrent = 0;
		double pCandidate = 0;
		if (useModelDistribution) {
			pCurrent = currentState.getModelScore();
			pCandidate = candidateState.getModelScore();
		} else {
			pCurrent = currentState.getObjectiveScore();
			pCandidate = candidateState.getObjectiveScore();
		}
		return pCandidate > pCurrent;
	}

	/**
	 * Accepts the candidate State only when its score is greater than the score of
	 * the current state.
	 * 
	 * @param candidateState
	 * @param currentState
	 * @param useModelDistribution
	 * @return
	 */
	public static boolean strictAccept(State candidateState, State currentState, boolean useModelDistribution) {
		Function<State, Double> getScore = null;
		if (useModelDistribution) {
			getScore = s -> s.getModelScore();
		} else {
			getScore = s -> s.getObjectiveScore();
		}

		double pCurrent = getScore.apply(currentState);
		double pCandidate = getScore.apply(candidateState);
		return pCandidate > pCurrent;
	}

	public static <T> T drawRandomElement(List<T> allNextStates) {
		int randomIndex = (int) (allNextStates.size() * Math.random());
		return allNextStates.get(randomIndex);
	}

	public static <T> List<T> nRandomElements(List<T> allNextStates, int n) {
		List<T> copy = new ArrayList<>(allNextStates);
		Collections.shuffle(copy);
		return copy.subList(0, Math.min(n, copy.size()));

	}
}

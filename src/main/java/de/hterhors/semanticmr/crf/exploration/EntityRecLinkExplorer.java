package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.candprov.nerla.INERLACandidateProvider;
import de.hterhors.semanticmr.candprov.nerla.NerlaCandidateProviderCollection;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 */
public class EntityRecLinkExplorer implements IExplorationStrategy {

	private static final char SPLITTER = ' ';

	final private NerlaCandidateProviderCollection candidateProvider;

	final private HardConstraintsProvider hardConstraintsProvider;

	public EntityRecLinkExplorer(NerlaCandidateProviderCollection candidateProvider,
			HardConstraintsProvider hardConstraintsProvder) {
		this.candidateProvider = candidateProvider;
		this.hardConstraintsProvider = hardConstraintsProvder;
	}

	public EntityRecLinkExplorer(NerlaCandidateProviderCollection candidateProvider) {
		this.candidateProvider = candidateProvider;
		this.hardConstraintsProvider = null;
	}

	/**
	 * Average number of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */
	int averageNumberOfNewProposalStates = 16;

	final int MAX_WINDOW_SIZE = 10;
	final int MIN_WINDOW_SIZE = 1;

	public List<State> explore(State currentState) {

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		final List<DocumentToken> tokens = currentState.getInstance().getDocument().tokenList;

		for (int windowSize = MIN_WINDOW_SIZE; windowSize <= MAX_WINDOW_SIZE; windowSize++) {

			for (int runIndex = 0; runIndex <= tokens.size() - windowSize; runIndex++) {

				final String text = toText(tokens, runIndex, windowSize);

				for (INERLACandidateProvider cp : candidateProvider.getCandidateProvider()) {

					for (EntityType entityType : cp.getEntityTypeCandidates(text)) {

						AbstractSlotFiller<? extends AbstractSlotFiller<?>> newCurrentPrediction = AbstractSlotFiller
								.toSlotFiller(entityType.entityTypeName, text, tokens.get(runIndex).docCharOffset);

						proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));

					}
				}
			}
		}

		proposalStates.forEach(System.out::println);

		System.exit(1);

		if (proposalStates.isEmpty()) {
			System.out.println("WARN no states generated for instance: " + currentState.getInstance().getDocument());
			proposalStates.add(currentState);
		}

		updateAverage(proposalStates);
		return proposalStates;

	}

	private String toText(List<DocumentToken> tokens, int runIndex, int windowSize) {
		final StringBuffer sb = new StringBuffer();
		for (int windowIndex = runIndex; windowIndex < runIndex + windowSize; windowIndex++) {
			sb.append(tokens.get(windowIndex).text).append(SPLITTER);
		}
		return sb.toString().trim();
	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

	/**
	 * TODO: inefficient way of checking constraints! First deep copy and then
	 * discard is a bad way. Rather check first before deep copy!
	 * 
	 * Checks if the newly generated templateEntity violates any constraints.
	 * 
	 * @param deepCopy
	 * 
	 * @return false if the template does NOT violates any constraints, else true.
	 */
	private boolean violatesConstraints(EntityTemplate deepCopy) {
		if (hardConstraintsProvider == null)
			return false;
		else
			return hardConstraintsProvider.violatesConstraints(deepCopy);

	}

}

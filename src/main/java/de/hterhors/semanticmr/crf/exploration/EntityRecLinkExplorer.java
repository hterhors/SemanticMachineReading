package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.candprov.nerla.INerlaCandidateProvider;
import de.hterhors.semanticmr.candprov.nerla.NerlaCandidateProviderCollection;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationCreationHelper;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;

/**
 * @author hterhors
 *
 */
public class EntityRecLinkExplorer implements IExplorationStrategy {

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

		addNewAnnotation(proposalStates, currentState);
		removeAnnotation(proposalStates, currentState);

		updateAverage(proposalStates);

		return proposalStates;

	}

	private void removeAnnotation(List<State> proposalStates, State currentState) {

		for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
				.size(); annotationIndex++) {
			proposalStates.add(currentState.deepRemoveCopy(annotationIndex));
		}

	}

	private void addNewAnnotation(final List<State> proposalStates, State currentState) {
		final List<DocumentToken> tokens = currentState.getInstance().getDocument().tokenList;

		for (int windowSize = MIN_WINDOW_SIZE; windowSize <= MAX_WINDOW_SIZE; windowSize++) {

			for (int runIndex = 0; runIndex < tokens.size() - windowSize; runIndex++) {

				final DocumentToken fromToken = tokens.get(runIndex); // including
				final DocumentToken toToken = tokens.get(runIndex + windowSize - 1); // including

				/*
				 * Check some basic constraints.
				 */

				if (fromToken.isStopWord())
					continue;
				if (fromToken.isPunctuation())
					continue;

				/*
				 * TODO: Might check tokens in between.
				 */

				if (toToken.isStopWord())
					continue;

				if (toToken.isPunctuation())
					continue;

				if (fromToken.getSentenceIndex() != toToken.getSentenceIndex())
					continue;

				if (fromToken == toToken && currentState.containsAnnotationOnTokens(fromToken))
					continue;
				else if (currentState.containsAnnotationOnTokens(fromToken, toToken))
					continue;

				final String text = currentState.getInstance().getDocument().getContent(fromToken, toToken);

				for (INerlaCandidateProvider cp : candidateProvider.getCandidateProvider()) {

					for (EntityType entityType : cp.getEntityTypeCandidates(text)) {

						try {
							AbstractSlotFiller<? extends AbstractSlotFiller<?>> newCurrentPrediction = AnnotationCreationHelper
									.toAnnotation(currentState.getInstance().getDocument(), entityType.entityTypeName,
											text, fromToken.getDocCharOffset());
							proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
						} catch (DocumentLinkedAnnotationMismatchException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

}

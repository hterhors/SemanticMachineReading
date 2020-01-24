package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer.ESamplingMode;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 */
public class RootTemplateCardinalityExplorer implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger(RootTemplateCardinalityExplorer.class);

	public static int MAX_NUMBER_OF_ANNOTATIONS;

	final private EntityTypeAnnotation initAnnotation;
	final private ESamplingMode samplingMode;

	public RootTemplateCardinalityExplorer(ESamplingMode samplingMode, EntityTypeAnnotation initAnnotation) {
		this.initAnnotation = initAnnotation;
		this.samplingMode = samplingMode;
	}

	/**
	 * Average number of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */
	int averageNumberOfNewProposalStates = 16;

	@Override
	public List<State> explore(State currentState) {

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		EntityTypeAnnotation init = initAnnotation.deepCopy();

		if (currentState.getCurrentPredictions().getAnnotations().size() < MAX_NUMBER_OF_ANNOTATIONS) {

			proposalStates.add(currentState.deepAddCopy(new EntityTemplate((init))));

			for (EntityTypeAnnotation templateTypeCandidate : currentState.getInstance()
					.getEntityTypeCandidates(samplingMode, init.getEntityType())) {

				if (templateTypeCandidate.equals(init))
					continue;

				proposalStates.add(currentState.deepAddCopy(new EntityTemplate((templateTypeCandidate))));

			}
		}

		for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
				.size(); annotationIndex++) {

			proposalStates.add(currentState.deepRemoveCopy(annotationIndex));

		}

		if (proposalStates.isEmpty()) {
			log.warn("No states were generated for instance: " + currentState.getInstance().getName());
		}
		updateAverage(proposalStates);
		return proposalStates;

	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

}

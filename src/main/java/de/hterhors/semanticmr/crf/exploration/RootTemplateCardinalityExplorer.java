package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer.EExplorationMode;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

/**
 * @author hterhors
 *
 */
public class RootTemplateCardinalityExplorer implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger(RootTemplateCardinalityExplorer.class);

	public static int MAX_NUMBER_OF_ANNOTATIONS;
	public static int MIN_NUMBER_OF_ANNOTATIONS;

	final private EntityTypeAnnotation initAnnotation;
	final private EExplorationMode samplingMode;
	final private AbstractEvaluator abstractEvaluator;
	final private HardConstraintsProvider hardConstraintsProvider;

	public RootTemplateCardinalityExplorer(AbstractEvaluator abstractEvaluator, EExplorationMode samplingMode,
			EntityTypeAnnotation initAnnotation) {
		this.hardConstraintsProvider = null;
		this.initAnnotation = initAnnotation;
		this.samplingMode = samplingMode;
		this.abstractEvaluator = abstractEvaluator;
	}

	public RootTemplateCardinalityExplorer(HardConstraintsProvider hardConstraintsProvider,
			AbstractEvaluator abstractEvaluator, EExplorationMode samplingMode, EntityTypeAnnotation initAnnotation) {
		this.hardConstraintsProvider = hardConstraintsProvider;
		this.initAnnotation = initAnnotation;
		this.samplingMode = samplingMode;
		this.abstractEvaluator = abstractEvaluator;
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

			EntityTemplate deepInitCopy = new EntityTemplate((init));

			if (!violatesConstraints(currentState, deepInitCopy,
					currentState.getCurrentPredictions().getAnnotations().size() + 1))
				proposalStates.add(currentState.deepAddCopy(deepInitCopy));

			for (EntityTypeAnnotation templateTypeCandidate : currentState.getInstance()
					.getEntityTypeCandidates(samplingMode, init.getEntityType())) {

				if (templateTypeCandidate.evaluateEquals(abstractEvaluator, init))
					continue;

				EntityTemplate deepCopy = new EntityTemplate((templateTypeCandidate));

				if (violatesConstraints(currentState, deepCopy,
						currentState.getCurrentPredictions().getAnnotations().size() + 1))
					continue;

				proposalStates.add(currentState.deepAddCopy(deepCopy));

			}
		}
		if (currentState.getCurrentPredictions().getAnnotations().size() > MIN_NUMBER_OF_ANNOTATIONS) {
			for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
					.size(); annotationIndex++) {

				proposalStates.add(currentState.deepRemoveCopy(annotationIndex));

			}
		}
		if (proposalStates.isEmpty()) {
			log.warn("No states were generated in explorer " + getClass().getSimpleName() + " for instance: "
					+ currentState.getInstance().getName());
		}

		updateAverage(proposalStates);
		return proposalStates;

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
	private boolean violatesConstraints(State state, EntityTemplate deepCopy, int annotationIndex) {
		if (hardConstraintsProvider == null)
			return false;
		else
			return hardConstraintsProvider.violatesConstraints(state, deepCopy, annotationIndex);

	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public State next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(int sentenceIndex) {
		// TODO Auto-generated method stub

	}

}

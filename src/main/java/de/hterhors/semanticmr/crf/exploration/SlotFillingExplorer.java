package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 */
public class SlotFillingExplorer implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger(SlotFillingExplorer.class);

	public static int MAX_NUMBER_OF_ANNOTATIONS = 100;
//	final private AnnotationCandidateRetrievalCollection candidateProvider;

	final private HardConstraintsProvider hardConstraintsProvider;
	final private IObjectiveFunction objectiveFunction;

	public SlotFillingExplorer(IObjectiveFunction objectiveFunction, HardConstraintsProvider hardConstraintsProvder) {
		this.hardConstraintsProvider = hardConstraintsProvder;
		this.objectiveFunction = objectiveFunction;
	}

	public SlotFillingExplorer(IObjectiveFunction objectiveFunction) {
		this.hardConstraintsProvider = null;
		this.objectiveFunction = objectiveFunction;
	}

	/**
	 * Average number of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */
	int averageNumberOfNewProposalStates = 16;

	@Override
	public List<State> explore(State currentState) {

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
				.size(); annotationIndex++) {

			final AbstractAnnotation annotation;

			if (!((annotation = currentState.getCurrentPredictions().getAnnotations()
					.get(annotationIndex)) instanceof EntityTemplate))
				throw new IllegalStateException("Can not handle non-EntityTemplate annotations in this explorer!");

			final EntityTemplate entityTemplateAnnotation = (EntityTemplate) annotation;

			/*
			 * Change root
			 */
			for (EntityTypeAnnotation entityTypeCandidate : currentState.getInstance()
					.getEntityTypeCandidates(entityTemplateAnnotation.getEntityType())) {

				changeTemplateType(proposalStates, currentState, entityTypeCandidate, entityTemplateAnnotation,
						annotationIndex);
			}

			/*
			 * Change props
			 */
			for (SlotType slotType : entityTemplateAnnotation.getSingleFillerSlotTypes()) {

				if (slotType.isExcluded())
					continue;

				for (AbstractAnnotation slotFillerCandidate : currentState.getInstance()
						.getSlotTypeCandidates(slotType)) {

					changeSingleFiller(proposalStates, currentState, slotType, slotFillerCandidate,
							entityTemplateAnnotation, annotationIndex);

					addMultiFiller(proposalStates, currentState, slotType, slotFillerCandidate,
							entityTemplateAnnotation, annotationIndex);

					changeMultiFiller(proposalStates, currentState, slotType, slotFillerCandidate,
							entityTemplateAnnotation, annotationIndex);
				}

				deleteSingleFiller(proposalStates, currentState, slotType, entityTemplateAnnotation, annotationIndex);
				deleteMultiFiller(proposalStates, currentState, slotType, entityTemplateAnnotation, annotationIndex);
			}
		}

		if (proposalStates.isEmpty()) {
			log.warn("No states were generated for instance: " + currentState.getInstance().getName());
		}

		updateAverage(proposalStates);

//		proposalStates.forEach(System.out::println);

		return proposalStates;

	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

	private void changeTemplateType(final List<State> proposalStates, State currentState,
			EntityTypeAnnotation templateTypeCandidate, EntityTemplate entityTemplate, int annotationIndex) {

		if (templateTypeCandidate.equals(entityTemplate.getRootAnnotation()))
			return;

		final EntityTemplate deepCopy = entityTemplate.deepMergeCopy(templateTypeCandidate);

		if (violatesConstraints(currentState, deepCopy))
			return;

		proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
	}

	private void deleteMultiFiller(final List<State> proposalStates, State currentState, SlotType slotType,
			EntityTemplate entityTemplate, int annotationIndex) {

		for (AbstractAnnotation slotFiller : entityTemplate.getMultiFillerSlot(slotType).getSlotFiller()) {

			final EntityTemplate deepCopy = entityTemplate.deepCopy();
			deepCopy.removeMultiFillerSlotFiller(slotType, slotFiller);

			if (violatesConstraints(currentState, deepCopy))
				continue;

			proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
		}

	}

	private void changeMultiFiller(final List<State> proposalStates, State currentState, SlotType slotType,
			AbstractAnnotation slotFillerCandidate, EntityTemplate entityTemplate, int annotationIndex) {

		/*
		 * Do no add itself
		 */
		if (slotFillerCandidate == entityTemplate)
			return;

		if (slotFillerCandidate.getEntityType().hasNoSlots() && slotFillerCandidate instanceof EntityTemplate)
			return;

		if (entityTemplate.getMultiFillerSlot(slotType).containsSlotFiller(objectiveFunction, slotFillerCandidate))
			return;

		for (AbstractAnnotation slotFiller : entityTemplate.getMultiFillerSlot(slotType).getSlotFiller()) {

			final EntityTemplate deepCopy = entityTemplate.deepCopy();

			deepCopy.updateMultiFillerSlot(slotType, slotFiller, slotFillerCandidate);

			if (violatesConstraints(currentState, deepCopy))
				return;

			proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
		}
	}

	private void addMultiFiller(final List<State> proposalStates, State currentState, SlotType slotType,
			AbstractAnnotation slotFillerCandidate, EntityTemplate entityTemplate, int annotationIndex) {

		/*
		 * Do not add if maximum number of fillers is reached.
		 */
		if (entityTemplate.getMultiFillerSlot(slotType).containsMaximumFiller())
			return;

		if (entityTemplate.getMultiFillerSlot(slotType).size() == MAX_NUMBER_OF_ANNOTATIONS)
			return;

		/*
		 * Do no add itself
		 */
		if (slotFillerCandidate == entityTemplate)
			return;

		if (entityTemplate.getMultiFillerSlot(slotType).containsSlotFiller(objectiveFunction, slotFillerCandidate))
			return;

		if (slotFillerCandidate.getEntityType().hasNoSlots() && slotFillerCandidate instanceof EntityTemplate) {
			return;
		}

		final EntityTemplate deepCopy = entityTemplate.deepCopy();
		deepCopy.addMultiSlotFiller(slotType, slotFillerCandidate);

		if (violatesConstraints(currentState, deepCopy))
			return;

		proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
	}

	private void deleteSingleFiller(final List<State> entityTemplates, State currentState, SlotType slotType,
			EntityTemplate entityTemplate, int annotationIndex) {

		final EntityTemplate deepCopy = entityTemplate.deepCopy();

		if (!entityTemplate.getSingleFillerSlot(slotType).containsSlotFiller())
			return;

		deepCopy.clearSlot(slotType);

		if (violatesConstraints(currentState, deepCopy))
			return;

		entityTemplates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
	}

	private void changeSingleFiller(final List<State> proposalStates, State currentState, SlotType slotType,
			AbstractAnnotation slotFillerCandidate, EntityTemplate entityTemplate, int annotationIndex) {

		/*
		 * Do no add itself
		 */
		if (slotFillerCandidate == entityTemplate)
			return;

		/*
		 * Continue if the entity has slots but is no instance of entity template.
		 */
		if (!(slotFillerCandidate.getEntityType().hasNoSlots()) && !(slotFillerCandidate instanceof EntityTemplate)) {
			return;
		}

		/*
		 * Do not add the same value again.
		 */
		if (slotFillerCandidate.equals(entityTemplate.getSingleFillerSlot(slotType).getSlotFiller()))
			return;

		final EntityTemplate deepCopy = entityTemplate.deepCopy();

		deepCopy.setSingleSlotFiller(slotType, slotFillerCandidate);

		if (violatesConstraints(currentState, deepCopy))
			return;

		proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));

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
	private boolean violatesConstraints(State state, EntityTemplate deepCopy) {
		if (hardConstraintsProvider == null)
			return false;
		else
			return hardConstraintsProvider.violatesConstraints(state, deepCopy);

	}

}

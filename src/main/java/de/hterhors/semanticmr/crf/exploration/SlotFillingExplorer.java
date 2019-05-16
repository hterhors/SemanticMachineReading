package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateRetrievalCollection;
import de.hterhors.semanticmr.candprov.sf.IEntityTypeAnnotationCandidateProvider;
import de.hterhors.semanticmr.candprov.sf.ISlotTypeAnnotationCandidateProvider;
import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 */
public class SlotFillingExplorer implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger(SlotFillingExplorer.class);

	public static int MAX_NUMBER_OF_ANNOTATIONS;

	final private AnnotationCandidateRetrievalCollection candidateProvider;

	final private HardConstraintsProvider hardConstraintsProvider;

	public SlotFillingExplorer(AnnotationCandidateRetrievalCollection candidateProvider,
			HardConstraintsProvider hardConstraintsProvder) {
		this.candidateProvider = candidateProvider;
		this.hardConstraintsProvider = hardConstraintsProvder;
	}

	public SlotFillingExplorer(AnnotationCandidateRetrievalCollection candidateProvider) {
		this.candidateProvider = candidateProvider;
		this.hardConstraintsProvider = null;
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

			final EntityTemplate entitytemplateAnnotation = (EntityTemplate) annotation;

			for (IEntityTypeAnnotationCandidateProvider slotFillerCandidateProvider : candidateProvider
					.getEntityTypeCandidateProvider(currentState.getInstance())) {
				changeTemplateType(proposalStates, currentState, slotFillerCandidateProvider, entitytemplateAnnotation,
						annotationIndex);
			}

			for (ISlotTypeAnnotationCandidateProvider slotFillerCandidateProvider : candidateProvider
					.getSlotTypeCandidateProvider(currentState.getInstance())) {

				changeSingleFiller(proposalStates, currentState, slotFillerCandidateProvider, entitytemplateAnnotation,
						annotationIndex);

				addMultiFiller(proposalStates, currentState, slotFillerCandidateProvider, entitytemplateAnnotation,
						annotationIndex);

				changeMultiFiller(proposalStates, currentState, slotFillerCandidateProvider, entitytemplateAnnotation,
						annotationIndex);
			}

			deleteSingleFiller(proposalStates, currentState, entitytemplateAnnotation, annotationIndex);
			deleteMultiFiller(proposalStates, currentState, entitytemplateAnnotation, annotationIndex);

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

	private void changeTemplateType(final List<State> proposalStates, State currentState,
			IEntityTypeAnnotationCandidateProvider slotFillerCandidateProvider, EntityTemplate entityTemplate,
			int annotationIndex) {

		for (EntityTypeAnnotation templateTypeCandidate : slotFillerCandidateProvider
				.getCandidates(entityTemplate.getEntityType())) {

			if (templateTypeCandidate.equals(entityTemplate.getRootAnnotation()))
				continue;

			final EntityTemplate deepCopy = entityTemplate.deepMergeCopy(templateTypeCandidate);

			if (violatesConstraints(deepCopy))
				continue;

			proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
		}
	}

	private void deleteMultiFiller(final List<State> proposalStates, State currentState, EntityTemplate entityTemplate,
			int annotationIndex) {
		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {
			for (AbstractAnnotation slotFiller : entityTemplate.getMultiFillerSlot(slot).getSlotFiller()) {

				final EntityTemplate deepCopy = entityTemplate.deepCopy();
				deepCopy.getMultiFillerSlot(slot).removeSlotFiller(slotFiller);

				if (violatesConstraints(deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
			}
		}

	}

	private void changeMultiFiller(final List<State> proposalStates, State currentState,
			ISlotTypeAnnotationCandidateProvider slotFillerCandidateProvider, EntityTemplate entityTemplate,
			int annotationIndex) {

		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {

			for (AbstractAnnotation slotFillerCandidate : slotFillerCandidateProvider.getCandidates(slot)) {

				/*
				 * Do no add itself
				 */
				if (slotFillerCandidate == entityTemplate)
					continue;

				if (entityTemplate.getMultiFillerSlot(slot).containsSlotFiller(slotFillerCandidate))
					continue;

				if (slotFillerCandidate.getEntityType().isLeafEntityType()
						&& slotFillerCandidate instanceof EntityTemplate) {
					continue;
				}

				for (AbstractAnnotation slotFiller : entityTemplate.getMultiFillerSlot(slot).getSlotFiller()) {

					final EntityTemplate deepCopy = entityTemplate.deepCopy();

					deepCopy.updateMultiFillerSlot(slot, slotFiller, slotFillerCandidate);

					if (violatesConstraints(deepCopy))
						continue;

					proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
				}
			}
		}
	}

	private void addMultiFiller(final List<State> proposalStates, State currentState,
			ISlotTypeAnnotationCandidateProvider slotFillerCandidateProvider, EntityTemplate entityTemplate,
			int annotationIndex) {
		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {
			for (AbstractAnnotation slotFillerCandidate : slotFillerCandidateProvider.getCandidates(slot)) {

				/*
				 * Do not add if maximum number of fillers is reached.
				 */
				if (entityTemplate.getMultiFillerSlot(slot).containsMaximumFiller())
					continue;

				if (entityTemplate.getMultiFillerSlot(slot).size() == MAX_NUMBER_OF_ANNOTATIONS)
					continue;

				/*
				 * Do no add itself
				 */
				if (slotFillerCandidate == entityTemplate)
					continue;

				if (entityTemplate.getMultiFillerSlot(slot).containsSlotFiller(slotFillerCandidate))
					continue;

				if (slotFillerCandidate.getEntityType().isLeafEntityType()
						&& slotFillerCandidate instanceof EntityTemplate) {
					continue;
				}

				final EntityTemplate deepCopy = entityTemplate.deepCopy();
				deepCopy.addMultiSlotFiller(slot, slotFillerCandidate);

				if (violatesConstraints(deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
			}
		}
	}

	private void deleteSingleFiller(final List<State> entityTemplates, State currentState,
			EntityTemplate entityTemplate, int annotationIndex) {
		for (SlotType slot : entityTemplate.getSingleFillerSlots().keySet()) {
			final EntityTemplate deepCopy = entityTemplate.deepCopy();

			if (!entityTemplate.getSingleFillerSlot(slot).containsSlotFiller())
				continue;

			deepCopy.getSingleFillerSlot(slot).removeFiller();
			if (violatesConstraints(deepCopy))
				continue;

			entityTemplates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));
		}
	}

	private void changeSingleFiller(final List<State> proposalStates, State currentState,
			ISlotTypeAnnotationCandidateProvider slotFillerCandidateProvider, EntityTemplate entityTemplate,
			int annotationIndex) {
		for (SlotType slotType : entityTemplate.getSingleFillerSlots().keySet()) {
			for (AbstractAnnotation slotFillerCandidate : slotFillerCandidateProvider.getCandidates(slotType)) {
				/*
				 * Do no add itself
				 */
				if (slotFillerCandidate == entityTemplate)
					continue;

				if (!(slotFillerCandidate.getEntityType().isLeafEntityType())
						&& !(slotFillerCandidate instanceof EntityTemplate)) {
					continue;
				}

				/*
				 * Do not add the same value again.
				 */
				if (slotFillerCandidate.equals(entityTemplate.getSingleFillerSlot(slotType).getSlotFiller()))
					continue;

				final EntityTemplate deepCopy = entityTemplate.deepCopy().setSingleSlotFiller(slotType,
						slotFillerCandidate);

				if (violatesConstraints(deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(annotationIndex, deepCopy));

			}
		}
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

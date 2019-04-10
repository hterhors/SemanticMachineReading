package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hterhors.semanticmr.crf.exploration.candidateprovider.ISlotFillerCandidateProvider;
import de.hterhors.semanticmr.crf.exploration.constraints.IHardConstraintsProvider;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

/**
 * @author hterhors
 *
 */
public class EntityTemplateExploration {

	final private List<ISlotFillerCandidateProvider<?>> slotFillerCandidateProviders;

	final private List<IHardConstraintsProvider> hardConstraintsProviders;

	public EntityTemplateExploration(List<ISlotFillerCandidateProvider<?>> entityCandidateProviders,
			List<IHardConstraintsProvider> hardConstraintsProvders) {
		this.slotFillerCandidateProviders = entityCandidateProviders;
		this.hardConstraintsProviders = hardConstraintsProvders;
	}

	public EntityTemplateExploration(ISlotFillerCandidateProvider<?> entityCandidateProvider,
			IHardConstraintsProvider hardConstraintsProvider) {
		this.slotFillerCandidateProviders = new ArrayList<>(1);
		this.slotFillerCandidateProviders.add(entityCandidateProvider);
		this.hardConstraintsProviders = new ArrayList<IHardConstraintsProvider>(1);
		this.hardConstraintsProviders.add(hardConstraintsProvider);
	}

	public EntityTemplateExploration(List<ISlotFillerCandidateProvider<?>> entityCandidateProviders,
			IHardConstraintsProvider hardConstraintsProvider) {
		this.slotFillerCandidateProviders = entityCandidateProviders;
		this.hardConstraintsProviders = new ArrayList<IHardConstraintsProvider>(1);
		this.hardConstraintsProviders.add(hardConstraintsProvider);
	}

	public EntityTemplateExploration(List<ISlotFillerCandidateProvider<?>> entityCandidateProviders) {
		this.slotFillerCandidateProviders = entityCandidateProviders;
		this.hardConstraintsProviders = Collections.emptyList();
	}

	public EntityTemplateExploration(ISlotFillerCandidateProvider<?> entityCandidateProvider,
			List<IHardConstraintsProvider> hardConstraintsProvders) {
		this.slotFillerCandidateProviders = new ArrayList<>(1);
		this.slotFillerCandidateProviders.add(entityCandidateProvider);
		this.hardConstraintsProviders = hardConstraintsProvders;
	}

	public EntityTemplateExploration(ISlotFillerCandidateProvider<?> entityCandidateProvider) {
		this.slotFillerCandidateProviders = new ArrayList<>(1);
		this.slotFillerCandidateProviders.add(entityCandidateProvider);
		this.hardConstraintsProviders = Collections.emptyList();
	}

	/**
	 * AverageNumber of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */
	int averageNumberOfNewProposalStates = 16;

	public List<State> explore(State currentState) {

		final EntityTemplate entityTemplate = currentState.currentPredictedEntityTemplate;

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		for (ISlotFillerCandidateProvider<?> slotFillerCandidateProvider : slotFillerCandidateProviders) {

			changeTemplateType(proposalStates, currentState, slotFillerCandidateProvider, entityTemplate);

			changeSingleFiller(proposalStates, currentState, slotFillerCandidateProvider, entityTemplate);

			addMultiFiller(proposalStates, currentState, slotFillerCandidateProvider, entityTemplate);

			changeMultiFiller(proposalStates, currentState, slotFillerCandidateProvider, entityTemplate);

		}

		deleteSingleFiller(proposalStates, currentState, entityTemplate);
		deleteMultiFiller(proposalStates, currentState, entityTemplate);

		updateAverage(proposalStates);

		return proposalStates;

	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

	private void changeTemplateType(final List<State> proposalStates, State currentState,
			ISlotFillerCandidateProvider<?> slotFillerCandidateProvider, EntityTemplate entityTemplate) {

		for (EntityType templateTypeCandidate : slotFillerCandidateProvider
				.getTemplateTypeCandidates(entityTemplate.getEntityType())) {

			if (templateTypeCandidate == entityTemplate.getEntityType())
				continue;

			final EntityTemplate deepCopy = entityTemplate.deepMergeCopy(templateTypeCandidate);

			if (violatesConstraints(proposalStates, deepCopy))
				continue;

			proposalStates.add(currentState.deepUpdateCopy(deepCopy));
		}
	}

	private void deleteMultiFiller(final List<State> proposalStates, State currentState,
			EntityTemplate entityTemplate) {
		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {
			for (AbstractSlotFiller<?> slotFiller : entityTemplate.getMultiFillerSlot(slot).getSlotFiller()) {

				final EntityTemplate deepCopy = entityTemplate.deepCopy();
				deepCopy.getMultiFillerSlot(slot).removeSlotFiller(slotFiller);

				if (violatesConstraints(proposalStates, deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(deepCopy));
			}
		}

	}

	private void changeMultiFiller(final List<State> proposalStates, State currentState,
			ISlotFillerCandidateProvider<?> slotFillerCandidateProvider, EntityTemplate entityTemplate) {
		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {
			for (AbstractSlotFiller<?> slotFillerCandidate : slotFillerCandidateProvider
					.getSlotFillerCandidates(slot)) {

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

				for (AbstractSlotFiller<?> slotFiller : entityTemplate.getMultiFillerSlot(slot).getSlotFiller()) {

					final EntityTemplate deepCopy = entityTemplate.deepCopy();

					deepCopy.updateMultiFillerSlot(slot, slotFiller, slotFillerCandidate);

					if (violatesConstraints(proposalStates, deepCopy))
						continue;

					proposalStates.add(currentState.deepUpdateCopy(deepCopy));
				}
			}
		}
	}

	private void addMultiFiller(final List<State> proposalStates, State currentState,
			ISlotFillerCandidateProvider<?> slotFillerCandidateProvider, EntityTemplate entityTemplate) {
		for (SlotType slot : entityTemplate.getMultiFillerSlots().keySet()) {
			for (AbstractSlotFiller<?> slotFillerCandidate : slotFillerCandidateProvider
					.getSlotFillerCandidates(slot)) {

				/*
				 * Do not add if maximum number of fillers is reached.
				 */
				if (entityTemplate.getMultiFillerSlot(slot).containsMaximumFiller())
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
				deepCopy.addToMultiFillerSlot(slot, slotFillerCandidate);

				if (violatesConstraints(proposalStates, deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(deepCopy));
			}
		}
	}

	private void deleteSingleFiller(final List<State> entityTemplates, State currentState,
			EntityTemplate entityTemplate) {
		for (SlotType slot : entityTemplate.getSingleFillerSlots().keySet()) {
			final EntityTemplate deepCopy = entityTemplate.deepCopy();

			if (!entityTemplate.getSingleFillerSlot(slot).containsSlotFiller())
				continue;

			deepCopy.getSingleFillerSlot(slot).removeFiller();
			if (violatesConstraints(entityTemplates, deepCopy))
				continue;

			entityTemplates.add(currentState.deepUpdateCopy(deepCopy));
		}
	}

	private void changeSingleFiller(final List<State> proposalStates, State currentState,
			ISlotFillerCandidateProvider<?> slotFillerCandidateProvider, EntityTemplate entityTemplate) {
		for (SlotType slotType : entityTemplate.getSingleFillerSlots().keySet()) {
			for (AbstractSlotFiller<?> slotFillerCandidate : slotFillerCandidateProvider
					.getSlotFillerCandidates(slotType)) {

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

				final EntityTemplate deepCopy = entityTemplate.deepCopy().updateSingleFillerSlot(slotType,
						slotFillerCandidate);

				if (violatesConstraints(proposalStates, deepCopy))
					continue;

				proposalStates.add(currentState.deepUpdateCopy(deepCopy));

			}
		}
	}

	/**
	 * TODO: inefficient way of checking constraints! First deep copy and then
	 * discard is a bad way. Rather check first before deep copy!
	 * 
	 * Checks if the newly generated templateEntity violates any constraints.
	 * 
	 * @param proposalStates
	 * @param deepCopy
	 * 
	 * @return false if the template does NOT violates any constraints, else true.
	 */
	private boolean violatesConstraints(final List<State> proposalStates, EntityTemplate deepCopy) {
		for (IHardConstraintsProvider hardConstraintsProvider : hardConstraintsProviders) {
			if (hardConstraintsProvider.violatesConstraints(deepCopy))
				return true;
		}
		return false;
	}

}

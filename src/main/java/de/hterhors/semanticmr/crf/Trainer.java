package de.hterhors.semanticmr.crf;

import java.util.Arrays;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exploration.EntityTemplateExploration;

public class Trainer {

	final int numberOfEpochs;

	final int maxNumberOfSamplingStepsPerInstance;

	final EntityTemplateExploration explorer;

	final Model model;

	final ObjectiveFunction objectiveFunction;

	final AbstractSampler sampler;

	private AcceptStrategy acceptStrategy = AcceptStrategies.strictModelAccept();

	public Trainer(final int maxNumberOfSamplingSteps, final int numberOfEpochs, EntityTemplateExploration explorer,
			ObjectiveFunction objectiveFunction, Model model, AbstractSampler sampler) {
		this.numberOfEpochs = numberOfEpochs;
		this.maxNumberOfSamplingStepsPerInstance = maxNumberOfSamplingSteps;
		this.model = model;
		this.explorer = explorer;
		this.objectiveFunction = objectiveFunction;
		this.sampler = sampler;
	}

	public void train(List<Instance> trainingInstances) {

		for (int epoch = 0; epoch < numberOfEpochs; epoch++) {

			for (Instance instance : trainingInstances) {

				State currentState = instance.getInitialState();

				for (int i = 0; i < maxNumberOfSamplingStepsPerInstance; i++) {

					final List<State> proposalStates = explorer.explore(currentState);

					final boolean sampleBasedOnObjectiveFunction = sampler.sampleBasedOnObjectiveScore(epoch);

					if (sampleBasedOnObjectiveFunction) {
						objectiveFunction.score(proposalStates);
					} else {
						model.score(proposalStates);
					}

					State candidateState = sampler.sampleCandidate(proposalStates);

					if (sampleBasedOnObjectiveFunction) {
						model.score(Arrays.asList(candidateState, currentState));
					} else {
						objectiveFunction.score(candidateState);
						objectiveFunction.score(currentState);
					}

					boolean isAccepted = acceptStrategy.isAccepted(candidateState, currentState);

					if (isAccepted) {
						model.updateWeights(currentState, candidateState);
						currentState = candidateState;
					}
				}
			}
		}
		System.out.println(model);
	}
}

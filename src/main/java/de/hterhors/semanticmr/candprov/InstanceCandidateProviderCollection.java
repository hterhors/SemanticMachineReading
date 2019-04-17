package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.variables.Instance;

public class InstanceCandidateProviderCollection {

	final private Map<Instance, List<ISlotFillerCandidateProvider<?>>> candidateProviderPerInstance = new HashMap<>();

	final private List<Instance> instances;

	public InstanceCandidateProviderCollection(List<Instance> instances) {
		this.instances = instances;
		for (Instance instance : instances) {
			candidateProviderPerInstance.putIfAbsent(instance, new ArrayList<>());
		}
	}

	public InstanceCandidateProviderCollection registerCandidateProvider(GeneralCandidateProvider candidateProvider) {
		if (!candidateProviderPerInstance.containsKey(candidateProvider.getRelatedInstance()))
			throw new IllegalArgumentException("Unkown instance: " + candidateProvider.getRelatedInstance().getName());
		candidateProviderPerInstance.get(candidateProvider.getRelatedInstance()).add(candidateProvider);
		return this;
	}

	public InstanceCandidateProviderCollection registerEntityTemplateCandidateProvider(EntityTemplateCandidateProvider candidateProvider) {
		if (!candidateProviderPerInstance.containsKey(candidateProvider.getRelatedInstance()))
			throw new IllegalArgumentException("Unkown instance: " + candidateProvider.getRelatedInstance().getName());
		candidateProviderPerInstance.get(candidateProvider.getRelatedInstance()).add(candidateProvider);
		return this;
	}

	public InstanceCandidateProviderCollection setEntityTypeCandidateProvider() {
		for (Instance instance : instances) {
			candidateProviderPerInstance.putIfAbsent(instance, new ArrayList<>());
			candidateProviderPerInstance.get(instance).add(EntityTypeCandidateProvider.getInstance());
		}
		return this;
	}

	public InstanceCandidateProviderCollection unsetEntityTypeCandidateProvider() {
		for (Instance instance : instances) {
			candidateProviderPerInstance.get(instance).remove(EntityTypeCandidateProvider.getInstance());
		}
		return this;
	}

	public List<ISlotFillerCandidateProvider<?>> getCandidateProviderForInstance(Instance relatedInstance) {

		final List<ISlotFillerCandidateProvider<?>> list = candidateProviderPerInstance.get(relatedInstance);

		if (list == null)
			throw new IllegalArgumentException("Unkown instance: " + relatedInstance.getName());

		return list;
	}

}

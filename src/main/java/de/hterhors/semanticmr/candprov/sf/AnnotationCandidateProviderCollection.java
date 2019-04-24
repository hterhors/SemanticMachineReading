package de.hterhors.semanticmr.candprov.sf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.variables.Instance;

public class AnnotationCandidateProviderCollection {

	final private Map<Instance, List<IAnnotationCandidateProvider>> candidateProviderPerInstance = new HashMap<>();

	final private List<Instance> instances;

	public AnnotationCandidateProviderCollection(List<Instance> instances) {
		this.instances = instances;
		for (Instance instance : instances) {
			candidateProviderPerInstance.putIfAbsent(instance, new ArrayList<>());
		}
	}

	public AnnotationCandidateProviderCollection registerCandidateProvider(GeneralCandidateProvider candidateProvider) {
		if (!candidateProviderPerInstance.containsKey(candidateProvider.getRelatedInstance()))
			throw new IllegalArgumentException("Unkown instance: " + candidateProvider.getRelatedInstance().getName());
		candidateProviderPerInstance.get(candidateProvider.getRelatedInstance()).add(candidateProvider);
		return this;
	}

	public AnnotationCandidateProviderCollection registerEntityTemplateCandidateProvider(
			EntityTemplateCandidateProvider candidateProvider) {
		if (!candidateProviderPerInstance.containsKey(candidateProvider.getRelatedInstance()))
			throw new IllegalArgumentException("Unkown instance: " + candidateProvider.getRelatedInstance().getName());
		candidateProviderPerInstance.get(candidateProvider.getRelatedInstance()).add(candidateProvider);
		return this;
	}

	public AnnotationCandidateProviderCollection setEntityTypeCandidateProvider() {
		for (Instance instance : instances) {
			candidateProviderPerInstance.putIfAbsent(instance, new ArrayList<>());
			candidateProviderPerInstance.get(instance).add(SlotEntityTypeCandidateProvider.getInstance());
		}
		return this;
	}

	public List<IAnnotationCandidateProvider> getCandidateProviderForInstance(Instance relatedInstance) {

		final List<IAnnotationCandidateProvider> list = candidateProviderPerInstance.get(relatedInstance);

		if (list == null)
			throw new IllegalArgumentException("Unkown instance: " + relatedInstance.getName());

		return list;
	}

}

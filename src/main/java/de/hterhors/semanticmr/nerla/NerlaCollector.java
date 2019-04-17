package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.InstanceCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.GeneralCandidateProvider;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;

public class NerlaCollector {

	public InstanceCandidateProviderCollection collect() {

		InstanceCandidateProviderCollection candidateProvider = new InstanceCandidateProviderCollection(this.instances);

		for (INerlaProvider provider : nerlaProvider) {

			try {
				final Map<Instance, List<EntityTypeAnnotation>> nerla = provider.getForInstances(this.instances);

				for (Instance instance : nerla.keySet()) {
					if (instance != null)
						candidateProvider.registerCandidateProvider(
								new GeneralCandidateProvider(instance).addBatchSlotFiller(nerla.get(instance)));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return candidateProvider;
	}

	final private List<INerlaProvider> nerlaProvider = new ArrayList<>();

	private List<Instance> instances;

	public void addNerlaProvider(INerlaProvider reader) {
		this.nerlaProvider.add(reader);
	}

	public NerlaCollector(List<Instance> instances) {
		this.instances = instances;
	}
}

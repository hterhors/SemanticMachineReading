package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.DocumentCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.GeneralCandidateProvider;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;

public class NerlaCollector {

	public DocumentCandidateProviderCollection collect() {

		DocumentCandidateProviderCollection candidateProvider = new DocumentCandidateProviderCollection();

		for (INerlaProvider provider : nerlaProvider) {

			try {
				final Map<Instance, List<EntityTypeAnnotation>> nerla = provider.get(this.instances);

				for (Instance instance : nerla.keySet()) {
					candidateProvider.addLiteralCandidateProvider(new GeneralCandidateProvider(instance.getDocument())
							.addBatchSlotFiller(nerla.get(instance)));
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

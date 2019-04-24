package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.sf.GeneralCandidateProvider;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;

public class NerlaCollector {

	public AnnotationCandidateProviderCollection collect() {

		AnnotationCandidateProviderCollection candidateProvider = new AnnotationCandidateProviderCollection(
				this.instances);

		for (INerlaProvider provider : nerlaProvider) {

			try {
				final Map<Instance, List<DocumentLinkedAnnotation>> nerla = provider.getForInstances(this.instances);

				for (Instance instance : nerla.keySet()) {
					if (instance != null) {
						GeneralCandidateProvider ap = new GeneralCandidateProvider(instance);
						for (DocumentLinkedAnnotation dla : nerla.get(instance)) {
							ap.addSlotFiller(dla);
						}
						candidateProvider.registerCandidateProvider(ap);
					}
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

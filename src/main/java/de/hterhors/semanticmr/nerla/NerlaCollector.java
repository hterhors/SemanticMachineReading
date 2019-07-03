package de.hterhors.semanticmr.nerla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.sf.AnnotationCandidateRetrievalCollection;
import de.hterhors.semanticmr.candprov.sf.GeneralCandidateProvider;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.variables.Instance;

public class NerlaCollector {

	public AnnotationCandidateRetrievalCollection collect() {

		AnnotationCandidateRetrievalCollection candidateProvider = new AnnotationCandidateRetrievalCollection(
				this.instances);

		for (INerlaProvider provider : nerlaProvider) {

			final Map<Instance, List<DocumentLinkedAnnotation>> nerla = provider.getForInstances(this.instances);

			for (Instance instance : nerla.keySet()) {
				if (instance != null) {
					GeneralCandidateProvider ap = new GeneralCandidateProvider(instance);
					for (DocumentLinkedAnnotation dla : nerla.get(instance)) {

						if (dla.getEntityType().hasNoSlots())
							ap.addSlotFiller(dla);
						else
							ap.addSlotFiller(new EntityTemplate(dla));
					}
					candidateProvider.registerCandidateProvider(ap);
				}
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

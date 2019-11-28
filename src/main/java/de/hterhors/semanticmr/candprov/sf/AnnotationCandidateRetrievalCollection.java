package de.hterhors.semanticmr.candprov.sf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * A collections of annotation candidate provider. This class contains a list
 * instances and a list of corresponding candidate provider.
 * 
 * We distinguish between two types:
 * 
 * 1) SlotFillingAnnotation candidate provider which provide candidates for
 * slots.
 * 
 * 2) EntityTypeAnnotation candidate provider which provide candidates for
 * entity types.
 * 
 * @author hterhors
 *
 */
public class AnnotationCandidateRetrievalCollection {

	/**
	 * The list of corresponding slot type provider.
	 */
	final private Map<Instance, List<ISlotTypeAnnotationCandidateProvider>> slotTypeCandidateProvider = new HashMap<>();

	/**
	 * The list of corresponding entity type provider.
	 */
	final private Map<Instance, List<IEntityTypeAnnotationCandidateProvider>> entityTypeCandidateProvider = new HashMap<>();

	/**
	 * The list of instances.
	 */
	final private List<Instance> instances;

	public List<Instance> getInstances() {
		return instances;
	}

	/**
	 * Initialize this collections with a list of possible instances.
	 * 
	 * @param instances
	 */
	public AnnotationCandidateRetrievalCollection(List<Instance> instances) {
		this.instances = instances;
		for (Instance instance : instances) {
			slotTypeCandidateProvider.putIfAbsent(instance, new ArrayList<>());
			entityTypeCandidateProvider.putIfAbsent(instance, new ArrayList<>());
		}
	}

	public AnnotationCandidateRetrievalCollection registerCandidateProvider(ICandidateProvider candidateProvider) {
		if (candidateProvider instanceof ISlotTypeAnnotationCandidateProvider) {
			if (!slotTypeCandidateProvider.containsKey(candidateProvider.getRelatedInstance()))
				throw new IllegalArgumentException(
						"Unkown instance: " + candidateProvider.getRelatedInstance().getName());
			slotTypeCandidateProvider.get(candidateProvider.getRelatedInstance())
					.add((ISlotTypeAnnotationCandidateProvider) candidateProvider);
		}

		if (candidateProvider instanceof IEntityTypeAnnotationCandidateProvider) {
			if (!entityTypeCandidateProvider.containsKey(candidateProvider.getRelatedInstance()))
				throw new IllegalArgumentException(
						"Unkown instance: " + candidateProvider.getRelatedInstance().getName());
			entityTypeCandidateProvider.get(candidateProvider.getRelatedInstance())
					.add((IEntityTypeAnnotationCandidateProvider) candidateProvider);
		}

		return this;
	}

	/**
	 * Sets the SlotEntityTypeCandidateProvider-singleton-instance to all instances.
	 * 
	 * @return this collection.
	 */
	public AnnotationCandidateRetrievalCollection setSlotEntityTypeCandidateProvider() {
		for (Instance instance : instances) {
			slotTypeCandidateProvider.putIfAbsent(instance, new ArrayList<>());
			slotTypeCandidateProvider.get(instance).add(SlotEntityTypeCandidateProvider.getInstance());
			entityTypeCandidateProvider.putIfAbsent(instance, new ArrayList<>());
			entityTypeCandidateProvider.get(instance).add(SlotEntityTypeCandidateProvider.getInstance());
		}
		return this;
	}

	/**
	 * Returns the list of corresponding slot type candidate provider for a given
	 * instance.
	 * 
	 * @param relatedInstance
	 * @return
	 */
	public List<ISlotTypeAnnotationCandidateProvider> getSlotTypeCandidateProvider(Instance relatedInstance) {

		final List<ISlotTypeAnnotationCandidateProvider> list = slotTypeCandidateProvider.get(relatedInstance);

		if (list == null)
			throw new IllegalArgumentException("Unkown instance: " + relatedInstance.getName());

		return list;
	}

	/**
	 * Returns the list of corresponding entity type candidate provider for a given
	 * instance.
	 * 
	 * @param relatedInstance
	 * @return
	 */
	public List<IEntityTypeAnnotationCandidateProvider> getEntityTypeCandidateProvider(Instance relatedInstance) {
		final List<IEntityTypeAnnotationCandidateProvider> list = entityTypeCandidateProvider.get(relatedInstance);

		if (list == null)
			throw new IllegalArgumentException("Unkown instance: " + relatedInstance.getName());

		return list;
	}

}

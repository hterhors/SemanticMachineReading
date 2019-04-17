package de.hterhors.semanticmr.init.specifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.exploration.constraints.IHardConstraintsProvider;
import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint;
import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint.SlotEntityPair;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.INormalizationFunction;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;

public class SystemInitializer {

	final static private List<IRequiresInitialization> requiresInitialization = new ArrayList<>();

	private static SpecificationsProvider specifications;

	private SystemInitializer() {
	}

	private static void register(IRequiresInitialization object) {
		requiresInitialization.add(object);
	}

	public SpecificationsProvider getSpecificationProvider() {
		return specifications;
	}

	public static NormalizationFunctionHandler initialize(SpecificationsProvider specificationProvider) {

		SystemInitializer.specifications = specificationProvider;

		register(SlotType.getInitializationInstance());
		register(EntityType.getInitializationInstance());

		for (IRequiresInitialization iRequiresInitialization : requiresInitialization) {
			iRequiresInitialization.system_init(specifications.getSpecifications());
		}
		return NormalizationFunctionHandler.getInstance();
	}

	/**
	 * Singleton class for normalization function handler. Contains a map of
	 * normalization function for individual entity types.
	 * 
	 * @author hterhors
	 *
	 */
	public static class NormalizationFunctionHandler {

		private final Map<EntityType, INormalizationFunction> normalizationFunctions = new HashMap<>();

		private static NormalizationFunctionHandler handlerInstance = null;

		private NormalizationFunctionHandler() {
		}

		private static NormalizationFunctionHandler getInstance() {
			if (handlerInstance == null) {
				handlerInstance = new NormalizationFunctionHandler();
			}

			return handlerInstance;
		}

		/**
		 * Register a normalization function for a specific entity type.
		 * 
		 * @param entityType
		 * @param normalizationFunction
		 * @return returns the previous normalization function, if any.
		 */
		public NormalizationFunctionHandler registerNormalizationFunction(EntityType entityType,
				INormalizationFunction normalizationFunction) {

			normalizationFunctions.put(entityType, normalizationFunction);
			for (EntityType subEntity : entityType.getSubEntityTypes()) {
				normalizationFunctions.put(subEntity, normalizationFunction);
			}

			return this;
		}

		public SystemInitializer apply() {
			for (Entry<EntityType, INormalizationFunction> entry : normalizationFunctions.entrySet()) {
				entry.getKey().setNormalizationFunction(entry.getValue());
			}
			return new SystemInitializer();
		}
	}

	
}

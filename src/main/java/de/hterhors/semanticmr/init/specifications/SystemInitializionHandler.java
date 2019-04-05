package de.hterhors.semanticmr.init.specifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.normalization.INormalizationFunction;
import de.hterhors.semanticmr.structure.slotfiller.normalization.IRequiresInitialization;

public class SystemInitializionHandler {

	final private List<IRequiresInitialization> requiresInitialization = new ArrayList<>();

	private boolean wasInitialized = false;

	public SystemInitializionHandler() {

	}

	public void register(IRequiresInitialization object) {
		if (wasInitialized)
			throw new IllegalStateException("System was already initialized!");

		requiresInitialization.add(object);

	}

	public NormalizationFunctionHandler initialize(SpecificationsProvider specifications) {
		for (IRequiresInitialization iRequiresInitialization : requiresInitialization) {
			iRequiresInitialization.system_init(specifications.getSpecifications());
		}
		wasInitialized = true;
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

		private boolean isClosed = false;

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
			if (isClosed)
				throw new IllegalStateException(
						"Can not register additional normalization function. Handler was already closed.");

			normalizationFunctions.put(entityType, normalizationFunction);
			for (EntityType subEntity : entityType.getSubEntityTypes()) {
				normalizationFunctions.put(subEntity, normalizationFunction);
			}

			return this;
		}

		public void close() {
			isClosed = true;
			for (Entry<EntityType, INormalizationFunction> entry : normalizationFunctions.entrySet()) {
				entry.getKey().setNormalizationFunction(entry.getValue());
			}
		}
	}
}

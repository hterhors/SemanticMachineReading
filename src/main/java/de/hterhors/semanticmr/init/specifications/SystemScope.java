package de.hterhors.semanticmr.init.specifications;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.INormalizationFunction;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;

public class SystemScope {

	public Specifications getSpecifications() {

		throw new NotImplementedException("NOT IMPL");
	}

	static public class Builder {

		private static Builder builder = null;

		public static SpecificationScopeHandler getSpecsHandler() {

			if (builder == null)
				builder = new Builder();

			return SpecificationScopeHandler.getInstance(builder);
		}

		public SystemScope build() {
			return new SystemScope();
		}

	}

	/**
	 * Singleton class for scope handler. Contains a map of normalization function
	 * for individual entity types.
	 * 
	 * @author hterhors
	 *
	 */
	public static class SpecificationScopeHandler {

		private final Set<ISpecificationsReader> specsReaderSet = new HashSet<>();

		private static SpecificationScopeHandler handlerInstance = null;

		private SpecificationScopeHandler(Builder builder) {
			this.builder = builder;
		}

		private Builder builder;

		private static SpecificationScopeHandler getInstance(Builder builder) {
			if (handlerInstance == null) {
				handlerInstance = new SpecificationScopeHandler(builder);
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
		public SpecificationScopeHandler addScopeSpecification(ISpecificationsReader specsReader) {
			specsReaderSet.add(specsReader);
			return this;
		}

		public NormalizationFunctionHandler apply() {
			for (ISpecificationsReader iSpecificationsReader : specsReaderSet) {

				Specifications specification = iSpecificationsReader.read();

				SlotType.getInitializationInstance().system_init(specification);
				EntityType.getInitializationInstance().system_init(specification);
			}

			return NormalizationFunctionHandler.getInstance(builder);
		}

		public SystemScope build() {
			return apply().build();
		}

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

		private final Builder builder;

		private NormalizationFunctionHandler(Builder builder) {
			this.builder = builder;
		}

		private static NormalizationFunctionHandler getInstance(Builder builder) {
			if (handlerInstance == null) {
				handlerInstance = new NormalizationFunctionHandler(builder);
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
		public NormalizationFunctionHandler registerNormalizationFunction(
				AbstractNormalizationFunction normalizationFunction) {

			normalizationFunctions.put(normalizationFunction.entityType, normalizationFunction);
			for (EntityType subEntity : normalizationFunction.entityType.getSubEntityTypes()) {
				normalizationFunctions.put(subEntity, normalizationFunction);
			}

			return this;
		}

		public Builder apply() {
			for (Entry<EntityType, INormalizationFunction> entry : normalizationFunctions.entrySet()) {
				entry.getKey().setNormalizationFunction(entry.getValue());
			}
			return builder;
		}

		public SystemScope build() {
			return apply().build();
		}
	}

}

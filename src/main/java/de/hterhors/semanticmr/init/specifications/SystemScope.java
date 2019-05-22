package de.hterhors.semanticmr.init.specifications;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.AbstractNormalizationFunction;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IdentityNormalization;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.exce.SystemNotInitializedException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;

public class SystemScope {

	private static Logger log = LogManager.getFormatterLogger(SystemScope.class);

	private static SystemScope instance = null;

	private Set<Specifications> specifications = new HashSet<>();

	private SystemScope(Set<Specifications> specifications) {
		EntityType.getEntityTypes().stream().forEach(et -> {
			if (et.isLiteral && et.getNormalizationFunction() == IdentityNormalization.getInstance()) {
				log.warn("No normalization function for literal entity type \"" + et.entityName
						+ "\" was specified. Set default to \"" + IdentityNormalization.class.getSimpleName() + "\"");
			}
		});
		this.specifications = specifications;
		log.info("Systems scope successfully initialized!");
	}

	public static SystemScope getInstance() {

		if (instance == null)
			throw new SystemNotInitializedException("Systems scope is not initialized!");

		return instance;
	}

	public Specifications getSpecification() {
		if (specifications.size() > 1)
			throw new NotImplementedException("NOT IMPL");
		// TODO: Merge specs, for now just a single specs is supported

		return specifications.iterator().next();
	}

	static public class Builder {

		private static Builder builder = null;

		private Set<Specifications> specifications = new HashSet<>();

		private Builder() {
			log.info("Initialize systems scope...");
		}

		public static SpecificationScopeHandler getSpecsHandler() {

			if (builder == null)
				builder = new Builder();

			return SpecificationScopeHandler.getInstance(builder);
		}

		public SystemScope build() {
			instance = new SystemScope(specifications);
			return instance;
		}

		public void addSpecs(Specifications specification) {
			specifications.add(specification);
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
			if (specsReaderSet.isEmpty()) {
				log.error("No specifications were added. System might not run properly! System exit!");
				System.exit(-1);
			}

			for (ISpecificationsReader iSpecificationsReader : specsReaderSet) {

				Specifications specification = iSpecificationsReader.read();

				SlotType.getInitializationInstance().system_init(specification);
				EntityType.getInitializationInstance().system_init(specification);

				builder.addSpecs(specification);
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
			log.info("Register normalization function  \"" + normalizationFunction.getClass().getSimpleName()
					+ "\" for entity type \"" + normalizationFunction.entityType.entityName + "\"");
			normalizationFunction.entityType.setNormalizationFunction(normalizationFunction);
			for (EntityType subEntity : normalizationFunction.entityType.getSubEntityTypes()) {
				subEntity.setNormalizationFunction(normalizationFunction);
			}

			return this;
		}

		public SystemScope build() {
			return builder.build();
		}
	}

}

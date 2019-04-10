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
import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.normalization.INormalizationFunction;
import de.hterhors.semanticmr.structure.slotfiller.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class SystemInitializionHandler {

	final private List<IRequiresInitialization> requiresInitialization = new ArrayList<>();

	final private SpecificationsProvider specifications;

	public SystemInitializionHandler(SpecificationsProvider specifications) {
		this.specifications = specifications;
		register(SlotType.getInitializationInstance());
		register(EntityType.getInitializationInstance());
	}

	private void register(IRequiresInitialization object) {
		requiresInitialization.add(object);
	}

	public NormalizationFunctionHandler initialize() {
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
		public NormalizationFunctionHandler addNormalizationFunction(EntityType entityType,
				INormalizationFunction normalizationFunction) {

			normalizationFunctions.put(entityType, normalizationFunction);
			for (EntityType subEntity : entityType.getSubEntityTypes()) {
				normalizationFunctions.put(subEntity, normalizationFunction);
			}

			return this;
		}

		public void apply() {
			for (Entry<EntityType, INormalizationFunction> entry : normalizationFunctions.entrySet()) {
				entry.getKey().setNormalizationFunction(entry.getValue());
			}
		}
	}

	public List<IHardConstraintsProvider> getHardConstraints() {

		List<ExcludePairConstraint> hardConstraints = new ArrayList<>();
		for (ExcludeSlotTypePairNames constraint : specifications.getSpecifications().getExcludeSlotTypePairs()) {

			hardConstraints.add(new ExcludePairConstraint(
					constraint.onTemplateType.isEmpty() ? null : EntityType.get(constraint.onTemplateType),
					new SlotEntityPair(SlotType.get(constraint.withSlotTypeName),
							EntityType.get(constraint.withEntityTypeName)),
					new SlotEntityPair(SlotType.get(constraint.excludeSlotTypeName),
							EntityType.get(constraint.excludeEntityTypeName))));
		}

		return Arrays.asList(new HardConstraintsProvider(hardConstraints));
	}
}

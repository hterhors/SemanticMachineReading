package de.hterhors.semanticmr.structure.constraints;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;
import de.hterhors.semanticmr.init.specifications.SpecificationsProvider;
import de.hterhors.semanticmr.structure.constraints.ExcludePairConstraint.SlotEntityPair;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class HardConstraintsProvider implements IHardConstraintsProvider {

	final List<AbstractHardConstraint> hardConstraints = new ArrayList<>();

	public HardConstraintsProvider(SpecificationsProvider specificationProvider) {

		for (ExcludeSlotTypePairNames constraint : specificationProvider.getSpecifications()
				.getExcludeSlotTypePairs()) {

			hardConstraints.add(new ExcludePairConstraint(
					constraint.onTemplateType.isEmpty() ? null : EntityType.get(constraint.onTemplateType),
					new SlotEntityPair(SlotType.get(constraint.withSlotTypeName),
							EntityType.get(constraint.withEntityTypeName)),
					new SlotEntityPair(SlotType.get(constraint.excludeSlotTypeName),
							EntityType.get(constraint.excludeEntityTypeName))));
		}

	}

	@Override
	public boolean violatesConstraints(EntityTemplate template) {
		for (AbstractHardConstraint hardConstraint : hardConstraints) {
			if (hardConstraint.violatesConstraint(template))
				return true;
		}
		return false;
	}

}

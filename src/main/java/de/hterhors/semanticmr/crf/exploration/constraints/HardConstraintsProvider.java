package de.hterhors.semanticmr.crf.exploration.constraints;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint;
import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint.SlotEntityPair;
import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.structure.EntityType;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class HardConstraintsProvider {

	final List<? extends AbstractHardConstraint> hardConstraints;

	private SystemInitializer initializer;

	public HardConstraintsProvider(SystemInitializer initializer) {
		this.initializer = initializer;
		this.hardConstraints = getHardConstraints();
	}

	public HardConstraintsProvider(List<ExcludePairConstraint> hardConstraints) {

		this.hardConstraints = hardConstraints;
	}

	public boolean violatesConstraints(EntityTemplate template) {
		for (AbstractHardConstraint hardConstraint : hardConstraints) {
			if (hardConstraint.violatesConstraint(template))
				return true;
		}
		return false;
	}

	private List<? extends AbstractHardConstraint> getHardConstraints() {

		List<ExcludePairConstraint> hardConstraints = new ArrayList<>();

		for (ExcludeSlotTypePairNames constraint : initializer.getSpecificationProvider().getSpecifications()
				.getExcludeSlotTypePairs()) {

			hardConstraints.add(new ExcludePairConstraint(
					constraint.onTemplateType.isEmpty() ? null : EntityType.get(constraint.onTemplateType),
					new SlotEntityPair(SlotType.get(constraint.withSlotTypeName),
							EntityType.get(constraint.withEntityTypeName)),
					new SlotEntityPair(SlotType.get(constraint.excludeSlotTypeName),
							EntityType.get(constraint.excludeEntityTypeName))));
		}

		return hardConstraints;
	}
}

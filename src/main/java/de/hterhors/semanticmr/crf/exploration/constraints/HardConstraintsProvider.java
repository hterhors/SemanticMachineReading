package de.hterhors.semanticmr.crf.exploration.constraints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint;
import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint.SlotEntityPair;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.init.specifications.StructureSpecification.ExcludeSlotTypePairNames;

public class HardConstraintsProvider {

	final List<AbstractHardConstraint> hardConstraints = new ArrayList<>();

	public boolean violatesConstraints(EntityTemplate template) {
		for (AbstractHardConstraint hardConstraint : hardConstraints) {
			if (hardConstraint.violatesConstraint(template))
				return true;
		}
		return false;
	}

	public HardConstraintsProvider addHardConstraints(EHardConstraintType type, File hardConstraintsFile) {

		try {
			switch (type) {
			case SLOT_PAIR_EXCLUSION:
				addSlotPairExclusionConstraint(hardConstraintsFile);
				break;

			default:
				System.out.println("Unkown constraints type: " + type);
				break;
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		return this;
	}

	private void addSlotPairExclusionConstraint(File hardConstraintsFile) throws IOException {
		List<String[]> slotPairConstriants = Files.readAllLines(hardConstraintsFile.toPath()).stream()
				.filter(l -> !l.startsWith("#")).filter(l -> !l.trim().isEmpty()).map(l -> l.split("\t"))
				.collect(Collectors.toList());

		Set<ExcludeSlotTypePairNames> excludeSlotTypePairs = slotPairConstriants.stream()
				.map(l -> new ExcludeSlotTypePairNames(l[0], l[1], l[2], l[3], l[4])).collect(Collectors.toSet());

		for (ExcludeSlotTypePairNames constraint : excludeSlotTypePairs) {

			hardConstraints.add(new ExcludePairConstraint(
					constraint.onTemplateType.isEmpty() ? null : EntityType.get(constraint.onTemplateType),
					new SlotEntityPair(SlotType.get(constraint.withSlotTypeName),
							EntityType.get(constraint.withEntityTypeName)),
					new SlotEntityPair(SlotType.get(constraint.excludeSlotTypeName),
							EntityType.get(constraint.excludeEntityTypeName))));
		}
	}
}

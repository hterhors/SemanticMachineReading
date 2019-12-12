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
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;

public class HardConstraintsProvider {

	final List<AbstractHardConstraint> hardConstraints = new ArrayList<>();

	public boolean violatesConstraints(EntityTemplate template) {
		for (AbstractHardConstraint hardConstraint : hardConstraints) {
			if (hardConstraint.violatesConstraint(template))
				return true;
		}
		return false;
	}

	public HardConstraintsProvider addHardConstraints(AbstractHardConstraint hardConstraint) {
		hardConstraints.add(hardConstraint);
		return this;
	}

	public HardConstraintsProvider addHardConstraintsFile(EHardConstraintType type, File hardConstraintsFile) {

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

	public static class ExcludeSlotTypePairNames {

		public final String onTemplateType;

		public ExcludeSlotTypePairNames(String onTemplateType, String withSlotTypeName, String withEntityTypeName,
				String excludeSlotTypeName, String excludeEntityTypeName) {
			this.onTemplateType = onTemplateType;
			this.withSlotTypeName = withSlotTypeName;
			this.withEntityTypeName = withEntityTypeName;
			this.excludeSlotTypeName = excludeSlotTypeName;
			this.excludeEntityTypeName = excludeEntityTypeName;
		}

		public final String withSlotTypeName;
		public final String withEntityTypeName;
		public final String excludeSlotTypeName;
		public final String excludeEntityTypeName;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((excludeEntityTypeName == null) ? 0 : excludeEntityTypeName.hashCode());
			result = prime * result + ((excludeSlotTypeName == null) ? 0 : excludeSlotTypeName.hashCode());
			result = prime * result + ((onTemplateType == null) ? 0 : onTemplateType.hashCode());
			result = prime * result + ((withEntityTypeName == null) ? 0 : withEntityTypeName.hashCode());
			result = prime * result + ((withSlotTypeName == null) ? 0 : withSlotTypeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExcludeSlotTypePairNames other = (ExcludeSlotTypePairNames) obj;
			if (excludeEntityTypeName == null) {
				if (other.excludeEntityTypeName != null)
					return false;
			} else if (!excludeEntityTypeName.equals(other.excludeEntityTypeName))
				return false;
			if (excludeSlotTypeName == null) {
				if (other.excludeSlotTypeName != null)
					return false;
			} else if (!excludeSlotTypeName.equals(other.excludeSlotTypeName))
				return false;
			if (onTemplateType == null) {
				if (other.onTemplateType != null)
					return false;
			} else if (!onTemplateType.equals(other.onTemplateType))
				return false;
			if (withEntityTypeName == null) {
				if (other.withEntityTypeName != null)
					return false;
			} else if (!withEntityTypeName.equals(other.withEntityTypeName))
				return false;
			if (withSlotTypeName == null) {
				if (other.withSlotTypeName != null)
					return false;
			} else if (!withSlotTypeName.equals(other.withSlotTypeName))
				return false;
			return true;
		}

	}
}

package de.hterhors.semanticmr.crf.structure.slots;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.exce.UnkownEnityTypeException;
import de.hterhors.semanticmr.exce.UnkownSlotTypeException;
import de.hterhors.semanticmr.init.specifications.StructureSpecification;

public class SlotType implements Comparable<SlotType>, IRequiresInitialization {

	final public String slotTypeName;

	/**
	 * Maximum size as specified in the specifications.
	 */
	final public int slotMaxCapacity;

	final private Set<String> slotFillerEntityTypeNames;

	private Set<EntityType> slotFillerEntityTypes;

	private final HashMap<EntityType, Boolean> matchesCache = new HashMap<>();

	private SlotType(String internalizedSlotTypeName, StructureSpecification specifications) {
		this.slotTypeName = internalizedSlotTypeName;
		this.slotMaxCapacity = specifications.getMultiAnnotationSlotMaxSize(internalizedSlotTypeName);
		this.slotFillerEntityTypeNames = specifications.getSlotFillerEntityTypeNames(internalizedSlotTypeName);
	}

	private SlotType() {
		this.slotTypeName = null;
		this.slotMaxCapacity = 0;
		this.slotFillerEntityTypeNames = Collections.emptySet();
	}

	public Set<EntityType> getSlotFillerEntityTypes() {

		if (slotFillerEntityTypes == null) {
			slotFillerEntityTypes = Collections.unmodifiableSet(
					slotFillerEntityTypeNames.stream().map(n -> EntityType.get(n)).collect(Collectors.toSet()));
		}
		return slotFillerEntityTypes;

	}

	private final static Map<String, SlotType> slotTypeFactory = new HashMap<>();

	public static SlotType get(final String slotTypeName) {

		final String internalized = slotTypeName.intern();

		SlotType SlotType;

		if ((SlotType = slotTypeFactory.get(internalized)) == null) {
			throw new UnkownSlotTypeException(
					"The requested entity type is unkown, since it is not part of the specifications: " + slotTypeName);
		}
		return SlotType;

	}

	private static SlotType initInstance = null;

	public static SlotType getInitializationInstance() {

		if (initInstance == null) {
			initInstance = new SlotType();
		}
		return initInstance;

	}

	@Override
	public void system_init(StructureSpecification specifications) {

		for (String slotTypeName : specifications.getSlotTypeNames()) {
			final String internalized = slotTypeName.intern();

			SlotType SlotType;

			if ((SlotType = slotTypeFactory.get(internalized)) == null) {
				SlotType = new SlotType(internalized, specifications);
				slotTypeFactory.put(internalized, SlotType);
			} else {
				throw new UnkownEnityTypeException(
						"Multiple occurrence for entity type " + slotTypeName + " in specifications.");
			}

		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((slotTypeName == null) ? 0 : slotTypeName.hashCode());
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
		SlotType other = (SlotType) obj;
		if (slotTypeName == null) {
			if (other.slotTypeName != null)
				return false;
		} else if (slotTypeName != other.slotTypeName)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SlotType [slotTypeName=" + slotTypeName + "]";
	}

	@Override
	public int compareTo(SlotType o) {
		return o.slotTypeName.compareTo(this.slotTypeName);
	}

	public boolean matchesEntityType(EntityType entityType) {
		Boolean matches;

		if ((matches = matchesCache.get(entityType)) != null)
			return matches.booleanValue();

		for (EntityType slotEntityType : getSlotFillerEntityTypes()) {
			if (slotEntityType == entityType || slotEntityType.isSuperEntityOf(entityType)) {
				matchesCache.put(entityType, new Boolean(true));
				return true;
			}
		}

		matchesCache.put(entityType, new Boolean(false));
		return false;

	}

	public String toPrettyString() {
		return toPrettyString(0);
	}

	public String toPrettyString(int depth) {
		return slotTypeName;
	}

	public boolean isSingleValueSlot() {
		return slotMaxCapacity == 1;
	}

}

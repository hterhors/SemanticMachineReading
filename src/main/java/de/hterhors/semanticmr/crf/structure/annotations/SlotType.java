package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.normalization.IRequiresInitialization;
import de.hterhors.semanticmr.exce.UnkownEnityTypeException;
import de.hterhors.semanticmr.exce.UnkownSlotTypeException;
import de.hterhors.semanticmr.init.specifications.Specifications;

public class SlotType implements Comparable<SlotType>, IRequiresInitialization {

	/**
	 * Excludes this slot type from sampling. However frozen slot types are still
	 * accessible for feature generation if, e.g. this slot contains values by
	 * default.
	 */
	private boolean frozen = false;

	/**
	 * Excludes this slot type from everything. Makes this slot type basically not
	 * existent.
	 */
	private boolean exclude = false;

	private static Map<SlotType, Boolean> storeExclude = new HashMap<>();

	public static Map<SlotType, Boolean> storeExcludance() {
		for (SlotType st : getAllSlotTypes()) {
			storeExclude.put(st, st.exclude);
		}
		return Collections.unmodifiableMap(storeExclude);
	}

	public static void restoreExcludance() {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude = storeExclude.get(st);
		}
	}

	public static void restoreExcludance(Map<SlotType, Boolean> restore) {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude = restore.get(st);
		}
	}

	public static void excludeAll() {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude();
		}
	}

	public static void includeAll() {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude();
		}
	}

	public static void freezeAll() {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude();
		}
	}

	public static void unfreezeAll() {
		for (SlotType st : getAllSlotTypes()) {
			st.exclude();
		}
	}

	public void exclude() {
		this.exclude = true;
	}

	public void include() {
		this.exclude = false;
	}

	public boolean isExcluded() {
		return exclude;
	}

	public boolean isIncluded() {
		return !exclude;
	}

	public void freeze() {
		this.frozen = true;
	}

	public void unfreeze() {
		this.frozen = false;
	}

	public boolean isFrozen() {
		return frozen;
	}

	final public String name;

	/**
	 * Maximum size as specified in the specifications.
	 */
	final public int slotMaxCapacity;

	final private Set<String> slotFillerEntityTypeNames;

	private Set<EntityType> slotFillerEntityTypes;

	private final HashMap<EntityType, Boolean> matchesCache = new HashMap<>();

	private SlotType(String internalizedSlotTypeName, Specifications specifications) {
		this.name = internalizedSlotTypeName;
		this.slotMaxCapacity = specifications.getMultiAnnotationSlotMaxSize(internalizedSlotTypeName);
		this.slotFillerEntityTypeNames = specifications.getSlotFillerEntityTypeNames(internalizedSlotTypeName);
	}

	private SlotType() {
		this.name = null;
		this.slotMaxCapacity = -1;
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

	public static Collection<SlotType> getAllSlotTypes() {
		return Collections.unmodifiableCollection(slotTypeFactory.values());
	}

	public static SlotType get(final String slotTypeName) {

		final String internalized = slotTypeName.intern();

		SlotType SlotType;

		if ((SlotType = slotTypeFactory.get(internalized)) == null) {
			throw new UnkownSlotTypeException(
					"The requested slot type is unkown, since it is not part of the specifications: " + slotTypeName);
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
	public void system_init(Specifications specifications) {

		for (String slotTypeName : specifications.getSlotTypeNames()) {
			final String internalized = slotTypeName.intern();

			SlotType SlotType;

			if ((SlotType = slotTypeFactory.get(internalized)) == null) {
				SlotType = new SlotType(internalized, specifications);
				slotTypeFactory.put(internalized, SlotType);
			} else {
				throw new UnkownEnityTypeException(
						"Multiple occurrence for slot type " + slotTypeName + " in specifications.");
			}

		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (name != other.name)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SlotType [slotTypeName=" + name + "]";
	}

	@Override
	public int compareTo(SlotType o) {
		return o.name.compareTo(this.name);
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
		if (isFrozen())
			return name + "(frozen)";

		return name;
	}

	public boolean isSingleValueSlot() {
		return slotMaxCapacity == 1;
	}

}

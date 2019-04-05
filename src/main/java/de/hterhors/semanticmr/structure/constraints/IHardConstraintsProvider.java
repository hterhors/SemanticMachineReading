package de.hterhors.semanticmr.structure.constraints;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public interface IHardConstraintsProvider {

	public boolean violatesConstraints(EntityTemplate template);

}

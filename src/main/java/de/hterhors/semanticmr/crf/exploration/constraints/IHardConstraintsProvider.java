package de.hterhors.semanticmr.crf.exploration.constraints;

import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;

public interface IHardConstraintsProvider {

	public boolean violatesConstraints(EntityTemplate template);

}

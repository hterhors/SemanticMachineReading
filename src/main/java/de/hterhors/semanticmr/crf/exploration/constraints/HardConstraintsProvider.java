package de.hterhors.semanticmr.crf.exploration.constraints;

import java.util.List;

import de.hterhors.semanticmr.crf.exploration.constraints.impl.ExcludePairConstraint;
import de.hterhors.semanticmr.structure.annotations.EntityTemplate;

public class HardConstraintsProvider implements IHardConstraintsProvider {

	final List<ExcludePairConstraint> hardConstraints;

	public HardConstraintsProvider(List<ExcludePairConstraint> hardConstraints) {

		this.hardConstraints = hardConstraints;
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

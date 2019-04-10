package de.hterhors.semanticmr.crf.templates;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.templates.TestTemplate.Scope;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class TestTemplate extends AbstractFeatureTemplate<Scope> {

	class Scope extends AbstractFactorScope {

		final String x;

		public Scope(AbstractFeatureTemplate<Scope> template, String x) {
			super(TestTemplate.this);
			this.x = x;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Scope other = (Scope) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x == null) {
				if (other.x != null)
					return false;
			} else if (!x.equals(other.x))
				return false;
			return true;
		}

		@Override
		public int getHashCode() {
			return hashCode();
		}

		@Override
		public boolean getEquals(Object obj) {
			return equals(obj);
		}

		private TestTemplate getOuterType() {
			return TestTemplate.this;
		}

	}

	@Override
	public List<Scope> generateFactorScopes(State state) {
		List<Scope> factors = new ArrayList<>();

		for (EntityTemplate annotation : state.currentPredictions.<EntityTemplate>getAnnotations()) {

			for (SlotType slot : annotation.getSingleFillerSlots().keySet()) {

				if (annotation.getSingleFillerSlot(slot).containsSlotFiller())
					factors.add(new Scope(this,
							annotation.getSingleFillerSlot(slot).getSlotFiller().getEntityType().entityTypeName));

			}
		}

		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<Scope> factor) {
		DoubleVector featureVector = factor.getFeatureVector();
		featureVector.set(factor.getFactorScope().x, true);
	}

}

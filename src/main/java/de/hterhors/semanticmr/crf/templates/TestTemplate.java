package de.hterhors.semanticmr.crf.templates;

import java.util.ArrayList;
import java.util.List;

import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.factor.FactorScope;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.crf.variables.Vector;
import de.hterhors.semanticmr.structure.slots.SlotType;

public class TestTemplate extends AbstractFactorTemplate {

	class Scope extends FactorScope {

		final String x;

		public Scope(AbstractFactorTemplate template, String x) {
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
	public List<FactorScope> generateFactorScopes(State state) {
		List<FactorScope> factors = new ArrayList<>();

		for (SlotType slot : state.currentPredictedEntityTemplate.getSingleFillerSlots().keySet()) {

			if (state.currentPredictedEntityTemplate.getSingleFillerSlot(slot).containsSlotFiller())
				factors.add(new Scope(this, state.currentPredictedEntityTemplate.getSingleFillerSlot(slot)
						.getSlotFiller().getEntityType().entityTypeName));

		}

		return factors;
	}

	@Override
	public void computeFeatureVector(Factor factor) {
		Vector featureVector = factor.getFeatureVector();
		featureVector.set(((Scope) factor.getFactorScope()).x, true);
	}

}

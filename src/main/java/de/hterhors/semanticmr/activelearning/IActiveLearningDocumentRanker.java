package de.hterhors.semanticmr.activelearning;

import java.util.List;

import de.hterhors.semanticmr.crf.variables.Instance;

public interface IActiveLearningDocumentRanker {

	List<Instance> rank(List<Instance> remainingInstances);

	static class HighestFirst implements Comparable<HighestFirst> {
		public final Instance instance;

		public final double score;

		public HighestFirst(Instance instance, double modelScore) {
			this.instance = instance;
			this.score = modelScore;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			long temp;
			temp = Double.doubleToLongBits(score);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			HighestFirst other = (HighestFirst) obj;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
				return false;
			return true;
		}

		@Override
		public int compareTo(HighestFirst o) {
			/*
			 * Highest first.
			 */
			return -Double.compare(score, o.score);
		}

	}

	static class SmallestFirst implements Comparable<SmallestFirst> {

		public final double value;
		public Instance instance;

		public SmallestFirst(Instance instance, double value) {
			this.instance = instance;
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			SmallestFirst other = (SmallestFirst) obj;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
				return false;
			return true;
		}

		@Override
		public int compareTo(SmallestFirst o) {
			/*
			 * Smallest first
			 */
			return Double.compare(value, o.value);
		}
	}
}

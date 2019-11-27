package de.hterhors.semanticmr.activelearning;

import de.hterhors.semanticmr.crf.variables.Instance;

public class RankedInstance implements Comparable<RankedInstance> {

	protected final double value;
	protected Instance instance;

	public RankedInstance(double value, Instance instance) {
		this.value = value;
		this.instance = instance;
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
		RankedInstance other = (RankedInstance) obj;
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
	public int compareTo(RankedInstance o) {
		/*
		 * Highest first
		 */
		return -Double.compare(value, o.value);
	}

	@Override
	public String toString() {
		return "RankedInstance [value=" + value + ", instance=" + instance + "]";
	}

}

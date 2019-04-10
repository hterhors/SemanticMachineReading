package de.hterhors.semanticmr.evaluation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

public interface IEvaluatable<T> {

	public static class Score {

		public static final int MAXIMUM_PERMUTATION_SIZE = 8;

		private static final Collection<List<Integer>>[] permutationCache = new Collection[MAXIMUM_PERMUTATION_SIZE];

		static {
			for (int i = 0; i < MAXIMUM_PERMUTATION_SIZE; i++) {
				permutationCache[i] = Collections2
						.permutations(IntStream.range(0, i).boxed().collect(Collectors.toList()));
			}
		}

		public final static Score ZERO = new Score();

		public static Stream<List<Integer>> getPermutationStream(final int size) {

			if (permutationCache.length < size)
				throw new IllegalArgumentException("Request for permutation of size " + size
						+ " exceeds maximum size of: " + MAXIMUM_PERMUTATION_SIZE);

			return permutationCache[size].stream();
		}

		final public static Score CORRECT = new Score(1, 0, 0, 0);

		final public static Score INCORRECT = new Score(0, 1, 1, 0);

		public static final Score FN = new Score(0, 0, 1, 0);

		public int tp = 0;
		public int fp = 0;
		public int fn = 0;
		public int tn = 0;

		public Score() {
		}

		public Score(int tp, int fp, int fn, int tn) {
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.tn = tn;
		}

		@Override
		public String toString() {
			return "Score [tp=" + tp + ", fp=" + fp + ", fn=" + fn + ", tn=" + tn + ", getPrecision()=" + getPrecision()
					+ ", getRecall()=" + getRecall() + ", getF1()=" + getF1() + "]";
		}

		public void add(Score evaluate) {
			this.tp += evaluate.tp;
			this.fp += evaluate.fp;
			this.fn += evaluate.fn;
			this.tn += evaluate.tn;
		}

		public void set(Score setter) {
			this.tp = setter.tp;
			this.fp = setter.fp;
			this.fn = setter.fn;
			this.tn = setter.tn;
		}

		public double getPrecision() {
			if ((tp + fp) == 0)
				return 0;
			return ((double) tp) / (tp + fp);
		}

		public double getRecall() {
			if ((tp + fn) == 0)
				return 0;
			return ((double) tp) / (tp + fn);
		}

		public double getF1() {
			final double p = getPrecision();
			final double r = getRecall();
			final double d = (p + r);
			return d == 0 ? 0 : (2 * p * r) / d;
		}

		public double getAccuracy() {
			double d = (tp + tn + fp + fn);

			if (d == 0)
				return 0;

			return ((double) (tp + tn)) / (tp + tn + fp + fn);
		}

		public double getJaccard() {
			if ((tp + fn + fp) == 0)
				return 0;
			return ((double) tp) / (tp + fn + fp);
		}

		/**
		 * Inverts the false positive and false negatives
		 * 
		 * @return this with inverted fp and fn
		 */
		public Score invert() {
			final int tmp = this.fn;
			this.fn = this.fp;
			this.fp = tmp;
			return this;
		}
	}

	public Score compare(T otherVal);

}

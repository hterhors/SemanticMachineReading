package de.hterhors.semanticmr.crf.structure;

import de.hterhors.semanticmr.eval.EEvaluationDetail;

public interface IEvaluatable<T> {

	public static class Score {

		final public static Score ZERO = new Score().unmod();

		final public static Score TP = new Score(1, 0, 0, 0).unmod();

		final public static Score FN_FP = new Score(0, 1, 1, 0).unmod();

		final public static Score FN = new Score(0, 0, 1, 0).unmod();

		final public static Score FP = new Score(0, 1, 0, 0).unmod();

		private int tp = 0;
		private int fp = 0;
		private int fn = 0;
		private int tn = 0;

		public Score() {
		}

		private boolean unmod = false;

		private Score unmod() {
			unmod = true;
			return this;
		}

		public Score(int tp, int fp, int fn, int tn) {
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.tn = tn;
		}

		public Score(int tp, int fp, int fn) {
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.tn = 0;
		}

		@Override
		public String toString() {
			return "Score [getF1()=" + getF1() + ", getPrecision()=" + getPrecision() + ", getRecall()=" + getRecall()
					+ ", tp=" + tp + ", fp=" + fp + ", fn=" + fn + ", tn=" + tn + "]";
		}

		public void add(Score evaluate) {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");

			this.tp += evaluate.tp;
			this.fp += evaluate.fp;
			this.fn += evaluate.fn;
			this.tn += evaluate.tn;
		}

		public void set(Score setter) {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
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
			/*
			 * Invert is the same.
			 */
			if (this == FN_FP || this == ZERO)
				return this;

			if (this == FN)
				return FP;

			if (this == FP)
				return FN;

			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");

			final int tmp = this.fn;
			this.fn = this.fp;
			this.fp = tmp;
			return this;
		}

		public void increaseFalsePositive() {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
			this.fp++;
		}

		public void increaseFalseNegative() {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
			this.fn++;
		}
	}

	public default Score evaluate(T otherVal) {
		return evaluate(EEvaluationDetail.DOCUMENT_LINKED, otherVal);
	}

	public Score evaluate(EEvaluationDetail mode, T otherVal);

}

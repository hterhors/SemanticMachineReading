package de.hterhors.semanticmr.crf.structure;

import java.text.DecimalFormat;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public interface IEvaluatable {

	public static class Score {
		public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.000");

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

		public Score(Score score) {
			this.tp = score.tp;
			this.fp = score.fp;
			this.fn = score.fn;
			this.tn = score.tn;
		}

		@Override
		public String toString() {
			return "Score [" + (tn != 0 ? ("getAccuracy()=" + SCORE_FORMAT.format(getAccuracy())+", ") : "") + " getF1()="
					+ SCORE_FORMAT.format(getF1()) + ", getPrecision()=" + SCORE_FORMAT.format(getPrecision())
					+ ", getRecall()=" + SCORE_FORMAT.format(getRecall()) + ", tp=" + tp + ", fp=" + fp + ", fn=" + fn
					+ ", tn=" + tn + "]";
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

		/**
		 * This works only if the tp and fn were added in a purity inverse purity
		 * fashion!
		 * 
		 * @return
		 */
		public double getPurity() {
			if (fn == 0)
				return 0;
//			r= tp / fn;
//			p = tp /fn;
//			(2 * tp / fn * tp /fn) / (tp / fn + tp /fn) = tp/fn
			return ((double) tp) / fn;
		}

		public double getPurityF() {
			if (fn == 0)
				return 0;
			return ((double) tp) / fn;

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

		public double getFbeta(final double beta) {
			final double p = getPrecision();
			final double r = getRecall();
			final double d = (p + r);
			if (d == 0)
				return 0;
			final double pow = Math.pow(beta, 2);
			return (1 + pow) * (p * r) / (pow * p + r);
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + fn;
			result = prime * result + fp;
			result = prime * result + tn;
			result = prime * result + tp;
			result = prime * result + (unmod ? 1231 : 1237);
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
			Score other = (Score) obj;
			if (fn != other.fn)
				return false;
			if (fp != other.fp)
				return false;
			if (tn != other.tn)
				return false;
			if (tp != other.tp)
				return false;
			if (unmod != other.unmod)
				return false;
			return true;
		}

		public int getTp() {
			return tp;
		}

		public int getFp() {
			return fp;
		}

		public int getFn() {
			return fn;
		}

		public int getTn() {
			return tn;
		}

	}

	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable otherVal);

	public Score evaluate(EEvaluationDetail evaluationDetail, IEvaluatable otherVal);

}

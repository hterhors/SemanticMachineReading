package de.hterhors.semanticmr.crf.structure;

import java.text.DecimalFormat;

import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

public interface IEvaluatable {

	public static class Score {
		public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.000");

		final public static Score ZERO_MICRO = new Score().unmod();

		final public static Score TP = new Score(1, 0, 0, 0).unmod();

		final public static Score FN_FP = new Score(0, 1, 1, 0).unmod();

		final public static Score FN = new Score(0, 0, 1, 0).unmod();

		final public static Score FP = new Score(0, 1, 0, 0).unmod();

		final public static Score ZERO_MACRO = new Score(EScoreType.MACRO).unmod();

		private int tp = 0;
		private int fp = 0;
		private int fn = 0;
		private int tn = 0;

		private int macroAddCounter;

		private EScoreType type;

		public static Score getZero(EScoreType scoreType) {
			if (scoreType == EScoreType.MICRO)
				return ZERO_MICRO;
			else
				return ZERO_MACRO;
		}

		public Score(EScoreType type) {
			this.type = type;
		}

		public Score() {
			this(EScoreType.MICRO);
		}

		private boolean unmod = false;

		public enum EScoreType {
			MICRO, MACRO
		}

		public Score unmod() {
			unmod = true;
			return this;
		}

		/**
		 * Converts this score object into a macro score object. Removes tp,fp,fn,tn and
		 * keeps f1, precision and recall. Modification functions such as add do not
		 * work.
		 * 
		 * @return
		 */
		public Score toMacro() {
			if (isMacro())
				return this;

			if (this == ZERO_MICRO)
				return ZERO_MACRO;

			this.macroF1 = getF1();
			this.macroPrecision = getPrecision();
			this.macroRecall = getRecall();
			this.macroAddCounter = 1;
			this.type = EScoreType.MACRO;

			return this;
		}

		public Score(int tp, int fp, int fn, int tn) {
			this();
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.tn = tn;
		}

		public Score(int tp, int fp, int fn) {
			this();
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.tn = 0;
		}

		private double macroPrecision;
		private double macroRecall;
		private double macroF1;

		/**
		 * Macro constructor.
		 * 
		 * @param precision
		 * @param recall
		 * @param f1
		 */
		public Score(double f1, double precision, double recall) {
			this(EScoreType.MACRO);

			this.macroAddCounter = 1;
			this.macroPrecision = precision;
			this.macroRecall = recall;
//			boolean match = SCORE_COMPARATOR.format(f1).equals(SCORE_COMPARATOR.format(getMacroF1()));
			this.macroF1 = f1;

//			if (!match)
//				throw new IllegalArgumentException("Precision and Recall does not match F1: " + toString());

			if (f1 > 1 || precision > 1 || recall > 1)
				throw new IllegalArgumentException(
						"F1, Precision and Recall can not be larger than 1.0D, values: " + toString());

		}

		public Score(Score score) {
			this();
			if (score.isMacro()) {
				toMacro();
			}
			add(score);
		}

		public boolean isMacro() {
			return this.type == EScoreType.MACRO;
		}

		public boolean isMicro() {
			return this.type == EScoreType.MICRO;
		}

		@Override
		public String toString() {
			if (isMicro())
				return microToString();
			else {
				return macroToString();
			}
		}

		public String macroToString() {
			return "Score [macroF1=" + SCORE_FORMAT.format(getF1()) + ", macroPrecision="
					+ SCORE_FORMAT.format(getPrecision()) + ", macroRecall=" + SCORE_FORMAT.format(getRecall()) + "]";
		}

		public String microToString() {
			return "Score [" + (tn != 0 ? ("getAccuracy()=" + SCORE_FORMAT.format(getAccuracy()) + ", ") : "")
					+ "getF1()=" + SCORE_FORMAT.format(getF1()) + ", getPrecision()="
					+ SCORE_FORMAT.format(getPrecision()) + ", getRecall()=" + SCORE_FORMAT.format(getRecall())
					+ ", tp=" + tp + ", fp=" + fp + ", fn=" + fn + ", tn=" + tn + "]";
		}

		public void add(Score adder) {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
			if (this.isMacro() && adder.isMacro()) {
				if (adder == ZERO_MACRO)
					return;
				this.macroPrecision += adder.macroPrecision;
				this.macroRecall += adder.macroRecall;
				this.macroF1 += getF1();

				this.macroAddCounter += adder.macroAddCounter;

			} else if (this.isMicro() && adder.isMicro()) {
				if (adder == ZERO_MICRO)
					return;
				this.tp += adder.tp;
				this.fp += adder.fp;
				this.fn += adder.fn;
				this.tn += adder.tn;
			} else {
				throw new IllegalStateException("Can not add " + adder.type + " to a " + this.type + " score.");
			}
		}

		public void set(Score setter) {
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");

			if (this.isMacro() && setter.isMacro()) {
				this.macroPrecision = setter.macroPrecision;
				this.macroRecall = setter.macroRecall;
				this.macroF1 = setter.macroF1;
				this.macroAddCounter = setter.macroAddCounter;
			} else if (this.isMicro() && setter.isMicro()) {
				this.tp = setter.tp;
				this.fp = setter.fp;
				this.fn = setter.fn;
				this.tn = setter.tn;
			} else {
				throw new IllegalStateException("Can not set " + setter.type + " to a " + this.type + " score.");
			}
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
			if (isMicro()) {
				return getMicroPrecision();
			} else {
				return getMacroPrecision();
			}
		}

		private double getMacroPrecision() {
			return macroAddCounter == 0 ? 0D : (macroPrecision / macroAddCounter);
		}

		private double getMicroPrecision() {
			if ((tp + fp) == 0)
				return 0;
			return ((double) tp) / (tp + fp);
		}

		public double getRecall() {
			if (isMicro()) {
				return getMicroRecall();
			} else {
				return getMacroRecall();
			}
		}

		private double getMacroRecall() {
			return macroAddCounter == 0 ? 0D : (macroRecall / macroAddCounter);
		}

		private double getMicroRecall() {
			if ((tp + fn) == 0)
				return 0;
			return ((double) tp) / (tp + fn);
		}

		public String getF1(DecimalFormat formatter) {
			return formatter.format(getF1());
		}

		public String getPrecision(DecimalFormat formatter) {
			return formatter.format(getPrecision());
		}

		public String getRecall(DecimalFormat formatter) {
			return formatter.format(getRecall());
		}

		public double getF1() {
			if (isMicro()) {
				return getMicroF1();
			} else {
				return getMacroF1();
			}
		}

		private double getMacroF1() {
			final double p = getMacroPrecision();
			final double r = getMacroRecall();
			final double d = (p + r);
			return d == 0 ? 0 : (2 * p * r) / d;
		}

		private double getMicroF1() {
			final double p = getMicroPrecision();
			final double r = getMicroRecall();
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
			if (isMacro())
				throw new IllegalStateException("Can not calculate accuracy for macro score objects.");
			double d = (tp + tn + fp + fn);

			if (d == 0)
				return 0;

			return ((double) (tp + tn)) / (tp + tn + fp + fn);
		}

		public double getJaccard() {
			if (isMacro())
				throw new IllegalStateException("Can not calculate accuracy for macro score objects.");
			if ((tp + fn + fp) == 0)
				return 0;
			return ((double) tp) / (tp + fn + fp);
		}

		/**
		 * Inverts the false positive and false negatives in case of micro score objects
		 * . in case of macro score objects this inverts recall and precision.
		 * 
		 * @return this with inverted fp and fn
		 */
		public Score invert() {
			if (isMicro()) {
				/*
				 * Invert is the same.
				 */
				if (this == FN_FP || this == ZERO_MICRO)
					return this;

				if (this == FN)
					return FP;

				if (this == FP)
					return FN;

				if (unmod)
					throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");

				final int tmp = (int) this.fn;
				this.fn = this.fp;
				this.fp = tmp;
			} else {

				if (unmod)
					throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");

				final double tmpR = this.getMacroRecall();
				this.macroRecall = this.getMacroPrecision();
				this.macroPrecision = tmpR;
			}
			return this;
		}

		public void increaseFalsePositive() {
			if (isMacro())
				throw new IllegalStateException("Can not increase false positives for macro score objects.");
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
			this.fp++;
		}

		public void increaseFalseNegative() {
			if (isMacro())
				throw new IllegalStateException("Can not increase false negatives for macro score objects.");
			if (unmod)
				throw new IllegalStateException("Score can not be changed, already set to unmodifiable.");
			this.fn++;
		}

		public int getTp() {
			if (isMacro())
				throw new IllegalStateException("Can not return true positive for macro score objects.");
			return tp;
		}

		public int getFp() {
			if (isMacro())
				throw new IllegalStateException("Can not return false positive for macro score objects.");
			return fp;
		}

		public int getFn() {
			if (isMacro())
				throw new IllegalStateException("Can not return false negativesfor macro score objects.");
			return fn;
		}

		public int getTn() {
			if (isMacro())
				throw new IllegalStateException("Can not return true negatives for macro score objects.");
			return tn;
		}

		@Override
		public int hashCode() {
			if (isMacro()) {
				return getMacroHashCode();
			} else {
				return getMicroHashCode();
			}
		}

		private int getMicroHashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + fn;
			result = prime * result + fp;
			result = prime * result + tn;
			result = prime * result + tp;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + (unmod ? 1231 : 1237);
			return result;
		}

		private int getMacroHashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(getMacroF1());
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(getMacroPrecision());
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(getMacroRecall());
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + (unmod ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (isMacro()) {
				return getMacroEquals(obj);
			} else {
				return getMicroEquals(obj);
			}
		}

		private boolean getMicroEquals(Object obj) {
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
			if (type != other.type)
				return false;
			if (unmod != other.unmod)
				return false;
			return true;
		}

		private boolean getMacroEquals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Score other = (Score) obj;
			if (Double.doubleToLongBits(getMacroF1()) != Double.doubleToLongBits(other.macroF1))
				return false;
			if (Double.doubleToLongBits(getMacroPrecision()) != Double.doubleToLongBits(other.macroPrecision))
				return false;
			if (Double.doubleToLongBits(getMacroRecall()) != Double.doubleToLongBits(other.macroRecall))
				return false;
			if (type != other.type)
				return false;
			if (unmod != other.unmod)
				return false;
			return true;
		}

	}

	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable otherVal);

	public Score evaluate(EEvaluationDetail evaluationDetail, IEvaluatable otherVal);

	public boolean evaluateEquals(AbstractEvaluator evaluator, IEvaluatable otherVal);

	public boolean evaluateEquals(EEvaluationDetail evaluationDetail, IEvaluatable otherVal);

}

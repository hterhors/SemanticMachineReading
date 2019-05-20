package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;

public abstract class AbstractEvaluator {
	protected static Logger log = LogManager.getFormatterLogger(AbstractEvaluator.class);

	final public EEvaluationDetail evaluationDetail;

	/**
	 * Mean computation time for computing pairwise scores. This variable is used to
	 * update multi thread probabilities.
	 */
	private double meanComputationTime = 0;

	/**
	 * Probabilities of whether to rely on multi thread method or single thread
	 * method for computing scores.
	 */
	private double[] multiThreadProbs = new double[] { 0.0D, 0.5D, 0.5D, 0.5D, 0.5D, 0.5D, 0.5D, 0.5D, 0.5D, 0.5D,
			0.5D };

	public AbstractEvaluator(EEvaluationDetail evaluationDetail) {
		this.evaluationDetail = evaluationDetail;
	}

	private static class ComparePair {

		final AbstractAnnotation value;
		final AbstractAnnotation otherVal;
		final boolean invert;
		final int i;
		final int j;

		public ComparePair(AbstractAnnotation value, AbstractAnnotation othertVal, boolean invert, int i, int j) {
			this.value = value;
			this.otherVal = othertVal;
			this.invert = invert;
			this.i = i;
			this.j = j;
		}

		@Override
		public String toString() {
			return "ComparePair [value=" + value + ", otherVal=" + otherVal + ", invert=" + invert + ", i=" + i + ", j="
					+ j + "]";
		}

	}

	/**
	 * Computes the scores of all possible pairs between the two given collections
	 * of slot filler variables.
	 * 
	 * This method makes use of single and multi threaded computation method. Based
	 * on the computation time for the specific number of values that needs to be
	 * compared, a probability is computed of whether the scores are computed multi
	 * or single threaded.
	 * 
	 * @param slotFiller
	 * @param otherSlotFiller
	 * @param maxSize
	 * @return
	 */
	protected Score[][] computeScores(final Collection<AbstractAnnotation> slotFiller,
			final Collection<AbstractAnnotation> otherSlotFiller, final int maxSize) {

		final double multiThreadProb = multiThreadProbs.length > maxSize ? multiThreadProbs[maxSize]
				: multiThreadProbs[multiThreadProbs.length - 1];

		final boolean multiThread = Math.random() < multiThreadProb;
		final Score[][] scores;

		long t = System.nanoTime();
		if (multiThread) {
			scores = multiThreaded(slotFiller, otherSlotFiller, maxSize);
		} else {
			scores = singleThreaded(slotFiller, otherSlotFiller, maxSize);
		}
		double newTime = System.nanoTime() - t;
		updateProbs(maxSize, multiThread, meanComputationTime > newTime);
		meanComputationTime += newTime;
		meanComputationTime /= 2;

		return scores;
	}

	private void updateProbs(int maxSize, boolean multiThread, boolean faster) {
		if (maxSize >= multiThreadProbs.length)
			return;

		if (multiThread) {
			if (faster) {
				multiThreadProbs[maxSize] = Math.min(0.9D, multiThreadProbs[maxSize] + 0.01);
			} else {
				multiThreadProbs[maxSize] = Math.max(0.1D, multiThreadProbs[maxSize] - 0.01);
			}
		} else {
			if (faster) {
				multiThreadProbs[maxSize] = Math.max(0.1D, multiThreadProbs[maxSize] - 0.01);
			} else {
				multiThreadProbs[maxSize] = Math.min(0.9D, multiThreadProbs[maxSize] + 0.01);
			}
		}
	}

	public Score[][] singleThreaded(final Collection<AbstractAnnotation> slotFiller,
			final Collection<AbstractAnnotation> otherSlotFiller, final int maxSize) {
		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractAnnotation> slotFillerIterator = slotFiller.iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractAnnotation slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractAnnotation> otherSlotFillerIterator = otherSlotFiller.iterator();

			while (j != maxSize) {

				final AbstractAnnotation otherSlotFillerVal;

				if (otherSlotFillerIterator.hasNext()) {
					otherSlotFillerVal = otherSlotFillerIterator.next();
				} else {
					otherSlotFillerVal = null;
				}

				if (slotFillerVal == null) {
					scores[i][j] = scoreSingle(otherSlotFillerVal, slotFillerVal).invert();
				} else {
					scores[i][j] = scoreSingle(slotFillerVal, otherSlotFillerVal);
				}
				j++;
			}
			i++;
		}

		return scores;
	}

	public Score[][] multiThreaded(final Collection<AbstractAnnotation> slotFiller,
			final Collection<AbstractAnnotation> otherSlotFiller, final int maxSize) {
		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractAnnotation> slotFillerIterator = slotFiller.iterator();

		final List<ComparePair> listOfPairs = new ArrayList<>((int) Math.pow(maxSize, 2));

		int i = 0;

		while (i != maxSize) {

			final AbstractAnnotation slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractAnnotation> otherSlotFillerIterator = otherSlotFiller.iterator();

			while (j != maxSize) {

				final AbstractAnnotation otherSlotFillerVal;
				if (otherSlotFillerIterator.hasNext()) {
					otherSlotFillerVal = otherSlotFillerIterator.next();
				} else {
					otherSlotFillerVal = null;
				}
				if (slotFillerVal == null) {
					listOfPairs.add(new ComparePair(otherSlotFillerVal, slotFillerVal, true, i, j));
				} else {
					listOfPairs.add(new ComparePair(slotFillerVal, otherSlotFillerVal, false, i, j));
				}
				j++;
			}
			i++;
		}
		listOfPairs.parallelStream().forEach(pair -> {
			final Score s;
			if (pair.invert)
				s = scoreSingle(pair.value, pair.otherVal).invert();
			else
				s = scoreSingle(pair.value, pair.otherVal);

			scores[pair.i][pair.j] = s;
		});

		return scores;
	}

	public Score scoreSingle(final AbstractAnnotation val, final AbstractAnnotation otherVal) {
		if (val instanceof DocumentLinkedAnnotation
				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			return ((DocumentLinkedAnnotation) val).evaluate(this, (DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			return ((LiteralAnnotation) val).evaluate(this, (LiteralAnnotation) otherVal);
		} else if (val instanceof EntityTypeAnnotation
				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
			return ((EntityTypeAnnotation) val).evaluate(this, (EntityTypeAnnotation) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).evaluate(this, (EntityTemplate) otherVal);
		} else {
			return Score.FN_FP;
		}
	}

	protected abstract Score scoreMax(Collection<AbstractAnnotation> annotations,
			Collection<AbstractAnnotation> otherAnnotations);

	public Score scoreMultiValues(Collection<AbstractAnnotation> annotations,
			Collection<AbstractAnnotation> otherAnnotations) {
		return scoreMax(annotations, otherAnnotations);
	}
}

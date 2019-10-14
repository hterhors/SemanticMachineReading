package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public class CartesianEvaluator extends AbstractEvaluator {
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

	public CartesianEvaluator(EEvaluationDetail evaluationMode) {
		super(evaluationMode);
	}

	public static int MAXIMUM_PERMUTATION_SIZE = 8;

	@SuppressWarnings("unchecked")
	private static final Collection<List<Integer>>[] permutationCache = new Collection[1 + MAXIMUM_PERMUTATION_SIZE];

	static {
		for (int i = 0; i <= MAXIMUM_PERMUTATION_SIZE; i++) {
			permutationCache[i] = Collections2.permutations(IntStream.range(0, i).boxed().collect(Collectors.toList()));
		}
	}

	public static void main(String[] args) {
		for (Collection<List<Integer>> string : permutationCache) {
			string.forEach(System.out::println);
			System.out.println("--------------");
		}
	}

	private static Stream<List<Integer>> getPermutationStream(final int size) {

		if (permutationCache.length <= size)
			throw new IllegalArgumentException(
					"Requested permutation size " + size + " exceeds maximum size of: " + MAXIMUM_PERMUTATION_SIZE);

		return permutationCache[size].stream();
	}

	@Override
	protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		final Score bestScore;
		if (annotations.size() == 1 || otherAnnotations.size() == 1) {

			bestScore = linear(annotations, otherAnnotations);
		} else {
			bestScore = cartesian(annotations, otherAnnotations);
		}

		return bestScore;
	}

	private Score linear(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {

		final Score bestScore = new Score();

		/**
		 * Distinguish to get fp / fn correct.
		 */
		if (annotations.size() == 1) {
			AbstractAnnotation singleInstance = annotations.iterator().next();
			for (Iterator<? extends AbstractAnnotation> mici = otherAnnotations.iterator(); mici.hasNext();) {
				AbstractAnnotation annotation = (AbstractAnnotation) mici.next();
				Score s = scoreSingle(annotation, singleInstance);
				if (s.getF1() == 1.0D)
					return s;
				if (bestScore.getF1() <= s.getF1()) {
					bestScore.set(s);
				}
			}
		} else {
			AbstractAnnotation singleInstance = otherAnnotations.iterator().next();
			for (Iterator<? extends AbstractAnnotation> mici = otherAnnotations.iterator(); mici.hasNext();) {
				AbstractAnnotation annotation = (AbstractAnnotation) mici.next();
				Score s = scoreSingle(singleInstance, annotation);
				if (s.getF1() == 1.0D)
					return s;

				if (bestScore.getF1() <= s.getF1()) {
					bestScore.set(s);
				}

			}
		}
		return bestScore;
	}

	public Score cartesian(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);

		final Score bestScore = new Score();

		getPermutationStream(maxSize).filter(indexPermutation -> {
			final Score sum = new Score();

			for (int index = 0; index < indexPermutation.size(); index++) {
				final int permIndex = indexPermutation.get(index).intValue();
				sum.add(scores[index][permIndex]);
			}

			final double f1 = sum.getF1();

			if (bestScore.getF1() <= f1) {
				bestScore.set(sum);
			}

			return f1 == 1.0D;

		}).findFirst();
		return bestScore;
	}

	private static class Compair {

		final AbstractAnnotation value;
		final AbstractAnnotation otherVal;
		final boolean invert;
		final int i;
		final int j;

		public Compair(AbstractAnnotation value, AbstractAnnotation othertVal, boolean invert, int i, int j) {
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
	protected Score[][] computeScores(final Collection<? extends AbstractAnnotation> slotFiller,
			final Collection<? extends AbstractAnnotation> otherSlotFiller, final int maxSize) {

		final Score[][] scores;

		final double multiThreadProb = multiThreadProbs.length > maxSize ? multiThreadProbs[maxSize]
				: multiThreadProbs[multiThreadProbs.length - 1];

		final boolean multiThread = Math.random() < multiThreadProb;

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

	public Score[][] singleThreaded(final Collection<? extends AbstractAnnotation> slotFiller,
			final Collection<? extends AbstractAnnotation> otherSlotFiller, final int maxSize) {
		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<? extends AbstractAnnotation> slotFillerIterator = slotFiller.iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractAnnotation slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<? extends AbstractAnnotation> otherSlotFillerIterator = otherSlotFiller.iterator();

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

	public Score[][] multiThreaded(final Collection<? extends AbstractAnnotation> slotFiller,
			final Collection<? extends AbstractAnnotation> otherSlotFiller, final int maxSize) {
		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<? extends AbstractAnnotation> slotFillerIterator = slotFiller.iterator();

		final List<Compair> listOfPairs = new ArrayList<>((int) Math.pow(maxSize, 2));

		int i = 0;

		while (i != maxSize) {

			final AbstractAnnotation slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<? extends AbstractAnnotation> otherSlotFillerIterator = otherSlotFiller.iterator();

			while (j != maxSize) {

				final AbstractAnnotation otherSlotFillerVal;
				if (otherSlotFillerIterator.hasNext()) {
					otherSlotFillerVal = otherSlotFillerIterator.next();
				} else {
					otherSlotFillerVal = null;
				}
				if (slotFillerVal == null) {
					listOfPairs.add(new Compair(otherSlotFillerVal, slotFillerVal, true, i, j));
				} else {
					listOfPairs.add(new Compair(slotFillerVal, otherSlotFillerVal, false, i, j));
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

}

package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

import de.hterhors.semanticmr.crf.structure.EntityType;
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

	private final NerlaEvaluator stdEvalForDocLinked;

	public CartesianEvaluator(EEvaluationDetail evaluationMode) {
		super(evaluationMode);
		this.stdEvalForDocLinked = new NerlaEvaluator(EEvaluationDetail.DOCUMENT_LINKED);
	}
	public CartesianEvaluator(EEvaluationDetail slotFillingEvaluationMode,EEvaluationDetail nerlaEvaluationMode) {
		super(slotFillingEvaluationMode);
		this.stdEvalForDocLinked = new NerlaEvaluator(nerlaEvaluationMode);
	}

	public static int MAXIMUM_PERMUTATION_SIZE = 8;

	@SuppressWarnings("unchecked")
	private static final List<List<Integer>>[] permutationCache = new ArrayList[1 + MAXIMUM_PERMUTATION_SIZE];

	static {
		for (int i = 0; i <= MAXIMUM_PERMUTATION_SIZE; i++) {
			permutationCache[i] = new ArrayList<>(
					Collections2.permutations(IntStream.range(0, i).boxed().collect(Collectors.toList())));
		}
	}

	private static Stream<List<Integer>> getPermutationStream(final int size) {

		if (permutationCache.length <= size)
			throw new IllegalArgumentException(
					"Requested permutation size " + size + " exceeds maximum size of: " + MAXIMUM_PERMUTATION_SIZE);

		return permutationCache[size].stream();
	}

	private static List<List<Integer>> getPermutations(final int size) {

		if (permutationCache.length <= size)
			throw new IllegalArgumentException(
					"Requested permutation size " + size + " exceeds maximum size of: " + MAXIMUM_PERMUTATION_SIZE);

		return permutationCache[size];
	}

	@Override
	protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {

		final Score bestScore;
		boolean docLinked = true;
		for (AbstractAnnotation abstractAnnotation : annotations) {
			if (!abstractAnnotation.isInstanceOfDocumentLinkedAnnotation()) {
				docLinked = false;
				break;
			}
		}
		if (docLinked)
			for (AbstractAnnotation abstractAnnotation : otherAnnotations) {
				if (!abstractAnnotation.isInstanceOfDocumentLinkedAnnotation()) {
					docLinked = false;
					break;
				}
			}

		if (docLinked)
			bestScore = stdEvalForDocLinked.prf1(annotations, otherAnnotations);
		else
			bestScore = cartesian(annotations, otherAnnotations);

		return bestScore;
	}

	public Score cartesian(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

//		System.out.println("Annotations:");
//		annotations.forEach(a -> System.out.println(a.toPrettyString()));
//		System.out.println("Other annotations:");
//		otherAnnotations.forEach(a -> System.out.println(a.toPrettyString()));

		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);
		final Score bestScore = new Score();
		final List<List<Integer>> permutations;
		try {

			permutations = getPermutations(maxSize);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			log.info(annotations.size());
			log.info(otherAnnotations.size());
			log.info(annotations);
			log.info(otherAnnotations);
			return bestScore;
		}

		for (List<Integer> indexPermutation : permutations) {

			final Score sum = new Score();

			for (int index = 0; index < indexPermutation.size(); index++) {
				final int permIndex = indexPermutation.get(index).intValue();
				sum.add(scores[index][permIndex]);
			}

			final double f1 = sum.getF1();

			if (bestScore.getF1() <= f1) {
				bestScore.set(sum);
			}

			if (f1 == 1.0D)
				break;

		}
//		System.out.println("Score: " + bestScore);
		return bestScore;
	}

	public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);

		final Score bestScore = new Score();
		final List<List<Integer>> permutations = getPermutations(maxSize);
		int permutationRunIndex = 0;
		int bestIndexPermutation = 0;

		for (List<Integer> indexPermutation : permutations) {

			final Score sum = new Score();

			for (int index = 0; index < indexPermutation.size(); index++) {
				final int permIndex = indexPermutation.get(index).intValue();
				sum.add(scores[index][permIndex]);
			}

			final double f1 = sum.getF1();

			if (bestScore.getF1() <= f1) {
				bestScore.set(sum);
				bestIndexPermutation = permutationRunIndex;
			}

			if (f1 == 1.0D)
				break;

			permutationRunIndex++;

		}

		return permutations.get(bestIndexPermutation);
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

	private Score[][] singleThreaded(final Collection<? extends AbstractAnnotation> slotFiller,
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

	private Score[][] multiThreaded(final Collection<? extends AbstractAnnotation> slotFiller,
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

package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public class CartesianEvaluator extends AbstractEvaluator {

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

	private static Stream<List<Integer>> getPermutationStream(final int size) {

		if (permutationCache.length <= size)
			throw new IllegalArgumentException(
					"Requested permutation size " + size + " exceeds maximum size of: " + MAXIMUM_PERMUTATION_SIZE);

		return permutationCache[size].stream();
	}

	@Override
	protected Score scoreMax(Collection<AbstractAnnotation> annotations,
			Collection<AbstractAnnotation> otherAnnotations) {

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

}

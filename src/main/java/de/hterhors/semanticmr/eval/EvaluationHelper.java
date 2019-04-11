package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

import de.hterhors.semanticmr.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.structure.slotfiller.AbstractSlotFiller;
import de.hterhors.semanticmr.structure.slotfiller.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;
import de.hterhors.semanticmr.structure.slotfiller.LiteralAnnotation;

public class EvaluationHelper {

	public static final int MAXIMUM_PERMUTATION_SIZE = 8;

	private static final Collection<List<Integer>>[] permutationCache = new Collection[MAXIMUM_PERMUTATION_SIZE];

	static {
		for (int i = 0; i < MAXIMUM_PERMUTATION_SIZE; i++) {
			permutationCache[i] = Collections2.permutations(IntStream.range(0, i).boxed().collect(Collectors.toList()));
		}
	}

	private static Stream<List<Integer>> getPermutationStream(final int size) {

		if (permutationCache.length < size)
			throw new IllegalArgumentException("Request for permutation of size " + size + " exceeds maximum size of: "
					+ MAXIMUM_PERMUTATION_SIZE);

		return permutationCache[size].stream();
	}

	private static Score[][] computeScores(final Collection<AbstractSlotFiller<?>> slotFiller,
			final Collection<AbstractSlotFiller<?>> otherSlotFiller, final int maxSize) {

		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractSlotFiller<?>> slotFillerIterator = slotFiller.iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractSlotFiller<?> slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractSlotFiller<?>> otherSlotFillerIterator = otherSlotFiller.iterator();

			while (j != maxSize) {

				final AbstractSlotFiller<?> otherSlotFillerVal;
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

	public static Score scoreSingle(final AbstractSlotFiller<?> val, final AbstractSlotFiller<?> otherVal) {
		if (val instanceof DocumentLinkedAnnotation && (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			return ((DocumentLinkedAnnotation) val).evaluate((DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			return ((LiteralAnnotation) val).evaluate((LiteralAnnotation) otherVal);
		} else if (val instanceof EntityType && (otherVal instanceof EntityType || otherVal == null)) {
			return ((EntityType) val).evaluate((EntityType) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).evaluate((EntityTemplate) otherVal);
		} else {
			return Score.INCORRECT;
		}
	}

	public static Score scoreMax(Collection<AbstractSlotFiller<?>> annotations,
			Collection<AbstractSlotFiller<?>> otherAnnotations) {

		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		final Score[][] scores = EvaluationHelper.computeScores(annotations, otherAnnotations, maxSize);

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

			return f1 == 1;

		}).findFirst();

		return bestScore;
	}

}

package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Collections2;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractSlotFiller;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.examples.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSpecs;

public class EvaluationHelper {

	public static final int MAXIMUM_PERMUTATION_SIZE = 8;

	@SuppressWarnings("unchecked")
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

	private static Score[][] computeScores(EEvaluationMode evaluationMode,
			final Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFiller,
			final Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> otherSlotFiller, final int maxSize) {

		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> slotFillerIterator = slotFiller.iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractSlotFiller<?> slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> otherSlotFillerIterator = otherSlotFiller
					.iterator();

			while (j != maxSize) {

				final AbstractSlotFiller<?> otherSlotFillerVal;
				if (otherSlotFillerIterator.hasNext()) {
					otherSlotFillerVal = otherSlotFillerIterator.next();
				} else {
					otherSlotFillerVal = null;
				}
				if (slotFillerVal == null) {
					scores[i][j] = scoreSingle(evaluationMode, otherSlotFillerVal, slotFillerVal).invert();
				} else {
					scores[i][j] = scoreSingle(evaluationMode, slotFillerVal, otherSlotFillerVal);
				}
				j++;
			}
			i++;
		}

		return scores;
	}

	public static void test() {

		SystemInitializer.initialize(new CSVSpecs().specificationProvider).apply();
		DocumentLinkedAnnotation o1 = AbstractSlotFiller.toSlotFiller("Male", "male", 100);
		LiteralAnnotation o2 = AbstractSlotFiller.toSlotFiller("Male", "male");
		EntityTypeAnnotation o3 = AbstractSlotFiller.toSlotFiller("Male");

		DocumentLinkedAnnotation dl1 = AbstractSlotFiller.toSlotFiller("Age", "Eight-week-old", 0);
		DocumentLinkedAnnotation dl2 = AbstractSlotFiller.toSlotFiller("Age", "Eight-week-old", 1);
		DocumentLinkedAnnotation dl3 = AbstractSlotFiller.toSlotFiller("Age", "Eight-week", 2);

		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, dl1, o1));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, dl2, o2));
		System.out.println("false: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, dl3, o3));

		System.out.println();

		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, dl1, dl2));
		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, dl1, dl3));
		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, dl2, dl3));
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, dl1, dl2));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, dl1, dl3));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, dl2, dl3));
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, dl1, dl2));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, dl1, dl3));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, dl2, dl3));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o3, o3));

		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o1, o2));
		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o1, o3));

		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o2, o1));
		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o2, o3));

		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o3, o1));
		System.out.println("false: " + scoreSingle(EEvaluationMode.DOCUMENT_LINKED, o3, o2));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, o3, o3));

		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, o1, o2));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, o1, o3));

		System.out.println("true: " + scoreSingle(EEvaluationMode.LITERAL, o2, o1));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, o2, o3));

		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, o3, o1));
		System.out.println("false: " + scoreSingle(EEvaluationMode.LITERAL, o3, o2));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o3, o3));

		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o1, o2));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o1, o3));

		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o2, o1));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o2, o3));

		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o3, o1));
		System.out.println("true: " + scoreSingle(EEvaluationMode.ENTITY_TYPE, o3, o2));

	}

	public static Score scoreSingle(final EEvaluationMode evaluationMode, final AbstractSlotFiller<?> val,
			final AbstractSlotFiller<?> otherVal) {
		if (val instanceof DocumentLinkedAnnotation
				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			return ((DocumentLinkedAnnotation) val).evaluate(evaluationMode, (DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			return ((LiteralAnnotation) val).evaluate(evaluationMode, (LiteralAnnotation) otherVal);
		} else if (val instanceof EntityTypeAnnotation
				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
			return ((EntityTypeAnnotation) val).evaluate(evaluationMode, (EntityTypeAnnotation) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).evaluate(evaluationMode, (EntityTemplate) otherVal);
		} else {
			return Score.FN_FP;
		}
	}

	public static Score scoreMax(EEvaluationMode evaluationMode,
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> annotations,
			Collection<AbstractSlotFiller<? extends AbstractSlotFiller<?>>> otherAnnotations) {

		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		final Score[][] scores = EvaluationHelper.computeScores(evaluationMode, annotations, otherAnnotations, maxSize);

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

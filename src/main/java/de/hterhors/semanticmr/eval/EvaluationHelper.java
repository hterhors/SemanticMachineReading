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
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.init.specifications.impl.CSVSlotFillingSpecs;

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

	private static Score[][] computeScores(EEvaluationDetail evaluationMode,
			final Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> slotFiller,
			final Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> otherSlotFiller, final int maxSize) {

		final Score[][] scores = new Score[maxSize][maxSize];

		final Iterator<AbstractAnnotation<? extends AbstractAnnotation<?>>> slotFillerIterator = slotFiller.iterator();

		int i = 0;

		while (i != maxSize) {

			final AbstractAnnotation<?> slotFillerVal;

			if (slotFillerIterator.hasNext()) {
				slotFillerVal = slotFillerIterator.next();
			} else {
				slotFillerVal = null;
			}

			int j = 0;

			final Iterator<AbstractAnnotation<? extends AbstractAnnotation<?>>> otherSlotFillerIterator = otherSlotFiller
					.iterator();

			while (j != maxSize) {

				final AbstractAnnotation<?> otherSlotFillerVal;
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

	public static void test() throws DocumentLinkedAnnotationMismatchException {

		List<DocumentToken> tokenList = new ArrayList<>	();
		tokenList.add(new DocumentToken(0, 0, 0, 0, 0, "male"));
		tokenList.add(new DocumentToken(0, 1, 1, 5, 5, "Eight-week-old"));
		tokenList.add(new DocumentToken(0, 2, 2, 21, 21, "Eight-week-old"));
		tokenList.add(new DocumentToken(0, 3, 3, 36, 36, "8 w"));
		Document d = new Document("name1",tokenList );
		
		SystemInitializer.initialize(new CSVSlotFillingSpecs().specificationProvider).apply();
		DocumentLinkedAnnotation o1 = AnnotationBuilder.toAnnotation(d,"Male", "male", 0);
		LiteralAnnotation o2 = AnnotationBuilder.toAnnotation("Male", "male");
		EntityTypeAnnotation o3 = AnnotationBuilder.toAnnotation("Male");

		DocumentLinkedAnnotation dl1 = AnnotationBuilder.toAnnotation(d,"Age", "Eight-week-old", 5);
		DocumentLinkedAnnotation dl2 = AnnotationBuilder.toAnnotation(d,"Age", "Eight-week-old", 21);
		DocumentLinkedAnnotation dl3 = AnnotationBuilder.toAnnotation(d,"Age", "8 w", 21);

		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, dl1, o1));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, dl2, o2));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, dl3, o3));

		System.out.println();

		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, dl1, dl2));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, dl1, dl3));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, dl2, dl3));
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, dl1, dl2));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, dl1, dl3));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, dl2, dl3));
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, dl1, dl2));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, dl1, dl3));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, dl2, dl3));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o3, o3));

		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o1, o2));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o1, o3));

		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o2, o1));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o2, o3));

		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o3, o1));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.DOCUMENT_LINKED, o3, o2));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, o3, o3));

		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, o1, o2));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, o1, o3));

		System.out.println("true: " + scoreSingle(EEvaluationDetail.LITERAL, o2, o1));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, o2, o3));

		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, o3, o1));
		System.out.println("false: " + scoreSingle(EEvaluationDetail.LITERAL, o3, o2));

		System.out.println();
		System.out.println();

		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o1, o1));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o2, o2));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o3, o3));

		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o1, o2));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o1, o3));

		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o2, o1));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o2, o3));

		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o3, o1));
		System.out.println("true: " + scoreSingle(EEvaluationDetail.ENTITY_TYPE, o3, o2));

	}

	public static Score scoreSingle(final EEvaluationDetail evaluationMode, final AbstractAnnotation<?> val,
			final AbstractAnnotation<?> otherVal) {
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

	public static Score scoreMax(EEvaluationDetail evaluationMode,
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> annotations,
			Collection<AbstractAnnotation<? extends AbstractAnnotation<?>>> otherAnnotations) {

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

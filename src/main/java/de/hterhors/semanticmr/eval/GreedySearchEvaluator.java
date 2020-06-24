package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;

public class GreedySearchEvaluator extends AbstractEvaluator {

	public static void main(String[] args) {

		int maxSize = 100;
		Score[][] scores = new Score[maxSize][maxSize];
		Random rand = new Random(2);

		for (int i = 0; i < scores.length; i++) {
			for (int j = 0; j < scores[i].length; j++) {
				scores[i][j] = new Score(rand.nextInt(5), rand.nextInt(5), rand.nextInt(5));
				System.out.println(i + "," + j + " = " + scores[i][j]);
			}
		}

		GreedySearchEvaluator f = new GreedySearchEvaluator(EEvaluationDetail.ENTITY_TYPE);

		System.out.println(f.greedySearchDecoder(scores));
	}

	private final NerlaEvaluator stdEvalForDocLinked;

	public GreedySearchEvaluator(EEvaluationDetail evaluationMode) {
		super(evaluationMode);
		this.stdEvalForDocLinked = new NerlaEvaluator(EEvaluationDetail.DOCUMENT_LINKED);
	}

	public GreedySearchEvaluator(EEvaluationDetail slotFillingEvaluationMode, EEvaluationDetail nerlaEvaluationMode) {
		super(slotFillingEvaluationMode);
		this.stdEvalForDocLinked = new NerlaEvaluator(nerlaEvaluationMode);
	}

	@Override
	protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return stdEvalForDocLinked.evalEqualsMultiValues(annotations, otherAnnotations);
	}

	@Override
	public Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoretype) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		/*
		 * Init scores
		 */
		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);

		return greedySearchDecoder(scores);
	}

	public Score greedySearchDecoder(final Score[][] scores) {

		Score maxScore = new Score();
		boolean[] tos = new boolean[scores.clone().length];

		for (int from = 0; from < scores.length; from++) {
			Score maxScore2 = new Score();
			int maxTo = 0;
			for (int to = 0; to < scores[from].length; to++) {
				if (tos[to])
					continue;

				if (maxScore2.getF1() <= scores[from][to].getF1()) {
					maxScore2 = scores[from][to];
					maxTo = to;
				}

			}
			tos[maxTo] = true;
			maxScore.add(maxScore2);
		}

		return maxScore;
	}

	protected Score[][] computeScores(final Collection<? extends AbstractAnnotation> slotFiller,
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

	@Override
	public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {
		throw new NotImplementedException("Not impl.");

	}

}

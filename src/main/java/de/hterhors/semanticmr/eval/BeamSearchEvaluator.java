package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public class BeamSearchEvaluator extends AbstractEvaluator {

	final public int beamSize;

	public static void main(String[] args) {

		int maxSize = 3;
		Score[][] scores = new Score[maxSize][maxSize];
		Random rand = new Random(10);

		for (int i = 0; i < scores.length; i++) {
			for (int j = 0; j < scores[i].length; j++) {
				scores[i][j] = new Score(rand.nextInt(5), rand.nextInt(5), rand.nextInt(5));
				System.out.println(i + "," + j + " = " + scores[i][j]);
			}
		}

		BeamSearchEvaluator f = new BeamSearchEvaluator(EEvaluationDetail.ENTITY_TYPE, 5);

		System.out.println(f.beamSearchDecoder(maxSize, scores));
	}

	private final NerlaEvaluator stdEvalForDocLinked;

	public BeamSearchEvaluator(EEvaluationDetail slotFillingEvaluationMode, final int beamSize,
			EEvaluationDetail nerlaEvaluationMode) {
		super(slotFillingEvaluationMode);
		this.beamSize = beamSize;
		this.stdEvalForDocLinked = new NerlaEvaluator(nerlaEvaluationMode);
	}

	public BeamSearchEvaluator(EEvaluationDetail evaluationMode, final int beamSize) {
		super(evaluationMode);
		this.beamSize = beamSize;
		this.stdEvalForDocLinked = new NerlaEvaluator(EEvaluationDetail.DOCUMENT_LINKED);
	}

	@Override
	public Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoretype) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		/*
		 * Init scores
		 */
		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);
		return beamSearchDecoder(maxSize, scores);
	}

	public Score beamSearchDecoder(final int maxSize, final Score[][] scores) {
		/*
		 * Init beam
		 */
		List<Assignment> assignments = new ArrayList<>();

		for (int from = 0; from < scores.length; from++) {
			for (int to = 0; to < scores[from].length; to++) {
				assignments.add(new Assignment(maxSize, from, to, scores[from][to]));
			}
		}

		Collections.sort(assignments);

		Assignment bestAssignment = beamSearchAssignment(scores,
				assignments.subList(0, Math.min(assignments.size(), beamSize)));

		return bestAssignment.score;
	}

	@Override
	protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return stdEvalForDocLinked.evalEqualsMultiValues(annotations, otherAnnotations);
	}

	private Assignment beamSearchAssignment(Score[][] scores, List<Assignment> assignments) {
		List<Assignment> newAssignment = new ArrayList<>();

		for (Assignment assignment : assignments) {

			for (int from = 0; from < scores.length; from++) {
				if (assignment.from[from])
					continue;
				for (int to = 0; to < scores[from].length; to++) {
					if (assignment.to[to])
						continue;

					Assignment newAssignemnt = new Assignment(assignment);

					newAssignemnt.addAssignment(from, to, scores[from][to]);

					newAssignment.add(newAssignemnt);
				}
			}

		}

		if (newAssignment.isEmpty()) {
			return assignments.get(0);
		}

		Collections.sort(newAssignment);

		return beamSearchAssignment(scores, newAssignment.subList(0, Math.min(assignments.size(), beamSize)));
	}

	static class Assignment implements Comparable<Assignment> {

		public final Score score;
		public final boolean[] from;
		public final boolean[] to;
		private final int maxSize;

		private List<Integer> assignmentsPairs;

		/*
		 * Clone
		 */
		public Assignment(Assignment assignments) {
			this.maxSize = assignments.maxSize;
			this.score = new Score(assignments.score);
			this.from = Arrays.copyOf(assignments.from, assignments.maxSize);
			this.to = Arrays.copyOf(assignments.to, assignments.maxSize);
			this.assignmentsPairs = new ArrayList<>(assignments.assignmentsPairs);
		}

		/*
		 * Init
		 */
		public Assignment(int maxSize, int from, int to, Score score) {
			this.maxSize = maxSize;
			this.score = score;
			this.from = new boolean[maxSize];
			this.to = new boolean[maxSize];
			this.from[from] = true;
			this.to[to] = true;
			this.assignmentsPairs = new ArrayList<>();
			for (int i = 0; i < maxSize; i++) {
				this.assignmentsPairs.add(i);
			}
			this.assignmentsPairs.set(from, to);
		}

		public void addAssignment(int from, int to, Score score) {
			this.score.add(score);
			this.from[from] = true;
			this.to[to] = true;
			this.assignmentsPairs.set(from, to);
		}

		@Override
		public int compareTo(Assignment o) {
			/*
			 * Highest first
			 */
			return -Double.compare(this.score.getF1(), o.score.getF1());
		}

		@Override
		public String toString() {
			return "Assignment [score=" + score + ", from=" + Arrays.toString(from) + ", to=" + Arrays.toString(to)
					+ ", maxSize=" + maxSize + " ]";
		}

		public List<Integer> getAssignments() {
//			return Collections.emptyList();
			return Collections.unmodifiableList(this.assignmentsPairs);
		}

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

	public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {

		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		/*
		 * Init scores
		 */
		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize);

		/*
		 * Init beam
		 */
		List<Assignment> assignments = new ArrayList<>();

		for (int from = 0; from < scores.length; from++) {
			for (int to = 0; to < scores[from].length; to++) {
				assignments.add(new Assignment(maxSize, from, to, scores[from][to]));
			}
		}

		Collections.sort(assignments);

		Assignment bestAssignment = beamSearchAssignment(scores,
				assignments.subList(0, Math.min(assignments.size(), beamSize)));

		return bestAssignment.getAssignments();
	}

}

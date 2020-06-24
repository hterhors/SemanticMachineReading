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

/**
 * Attempt to make this class more efficient. is NOT more efficient
 * 
 * @author hterhors
 *
 */
public class BeamSearchEvaluatorArray extends AbstractEvaluator {

	final public int beamSize;

//	[137, 59, 1, 35, 47, 75, 5, 36, 18, 107, 96, 39, 98, 12, 45, 186, 28, 8, 67, 15, 3, 21, 37, 9, 24, 56, 52, 10, 26, 13, 7, 14, 31, 48, 102, 85, 25, 95, 27, 40, 41, 87, 33, 20, 19, 23, 70, 4, 54, 76, 84, 57, 49, 2, 105, 82, 29, 97, 22, 0, 11, 73, 94, 61, 64, 86, 51, 74, 6, 66, 34, 53, 17, 99, 83, 114, 103, 120, 126, 111, 46, 79, 149, 63, 134, 132, 89, 108, 136, 115, 80, 71, 38, 161, 69, 104, 44, 62, 78, 133, 151, 92, 139, 210, 16, 140, 58, 30, 156, 117, 155, 172, 91, 43, 162, 203, 116, 121, 60, 125, 123, 72, 110, 145, 88, 50, 81, 179, 65, 106, 148, 68, 119, 301, 131, 166, 163, 112, 164, 141, 142, 198, 167, 129, 185, 124, 146, 206, 177, 171, 93, 127, 153, 130, 309, 170, 42, 168, 128, 159, 191, 150, 197, 154, 143, 235, 238, 272, 147, 180, 109, 90, 100, 77, 135, 122, 32, 138, 273, 176, 160, 234, 211, 182, 144, 189, 236, 196, 270, 200, 214, 55, 118, 113, 183, 157, 232, 190, 202, 218, 165, 216, 194, 169, 215, 291, 205, 204, 339, 219, 227, 192, 213, 220, 229, 223, 255, 152, 250, 184, 209, 101, 249, 212, 158, 244, 251, 208, 199, 230, 173, 178, 222, 261, 224, 231, 228, 233, 201, 243, 298, 247, 221, 278, 242, 264, 237, 246, 258, 269, 187, 256, 193, 293, 175, 226, 241, 268, 188, 303, 281, 207, 329, 262, 252, 279, 263, 285, 259, 314, 248, 267, 323, 260, 287, 245, 195, 283, 308, 271, 257, 311, 254, 286, 369, 337, 266, 277, 304, 330, 265, 290, 316, 380, 280, 354, 276, 310, 275, 239, 217, 391, 292, 359, 319, 295, 324, 294, 347, 225, 253, 181, 282, 332, 331, 372, 296, 305, 378, 274, 368, 327, 302, 284, 343, 297, 345, 360, 374, 390, 300, 403, 322, 240, 365, 361, 358, 383, 317, 306, 174, 288, 289, 355, 318, 335, 299, 352, 349, 307, 328, 315, 356, 353, 351, 363, 313, 348, 325, 405, 377, 373, 340, 320, 389, 371, 333, 463, 409, 519, 379, 362, 388, 435, 350, 413, 426, 484, 342, 370, 392, 338, 312, 440, 326, 420, 341, 375, 402, 357, 386, 344, 465, 334, 412, 407, 376, 400, 425, 382, 336, 366, 387, 422, 474, 395, 401, 404, 421, 384, 468, 497, 447, 399, 364, 432, 393, 423, 433, 457, 471, 496, 411, 535, 414, 437, 385, 456, 416, 367, 424, 397, 434, 462, 441, 394, 381, 431, 406, 410, 346, 321, 461, 483, 445, 442, 439, 396, 429, 398, 444, 417, 464, 452, 427, 408, 419, 449, 451, 455, 448, 466, 430, 492, 415, 494, 478, 453, 489, 518, 476, 469, 472, 506, 458, 481, 490, 500, 418, 515, 495, 486, 487, 436, 529, 499, 460, 491, 524, 531, 503, 438, 485, 549, 502, 459, 443, 473, 482, 504, 501, 467, 505, 493, 534, 548, 571, 512, 480, 510, 477, 532, 521, 526, 552, 507, 446, 538, 516, 523, 454, 598, 583, 539, 525, 558, 551, 596, 561, 479, 498, 470, 543, 513, 590, 517, 514, 557, 508, 537, 575, 540, 522, 511, 546, 527, 564, 576, 550, 541, 568, 542, 566, 588, 450, 475, 599, 563, 570, 520, 597, 559, 533, 584, 547, 528, 488, 554, 573, 594, 428, 569, 560, 536, 589, 556, 577, 581, 553, 530, 585, 579, 593, 578, 509, 555, 544, 586, 582, 565, 591, 545, 572, 580, 595, 562, 592, 567, 574, 587]
//			Score [getF1()=0.987, getPrecision()=0.990, getRecall()=0.984, tp=1231, fp=12, fn=20, tn=0]
//			187342 8250 RAM

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		int maxSize = 600;
		Score[][] scores = new Score[maxSize][maxSize];
		Random rand = new Random(10);

		for (int i = 0; i < scores.length; i++) {
			for (int j = 0; j < scores[i].length; j++) {
				scores[i][j] = new Score(rand.nextInt(5), rand.nextInt(5), rand.nextInt(5));
				System.out.println(i + "," + j + " = " + scores[i][j]);
			}
		}
		System.out.println("Start decoding...");

		BeamSearchEvaluatorArray f = new BeamSearchEvaluatorArray(EEvaluationDetail.ENTITY_TYPE, 5);

		System.out.println(f.beamSearchDecoder(true, maxSize, scores));
		System.out.println((System.currentTimeMillis() - time));
	}

	private final NerlaEvaluator stdEvalForDocLinked;

	public BeamSearchEvaluatorArray(EEvaluationDetail slotFillingEvaluationMode, final int beamSize,
			EEvaluationDetail nerlaEvaluationMode) {
		super(slotFillingEvaluationMode);
		this.beamSize = beamSize;
		this.stdEvalForDocLinked = new NerlaEvaluator(nerlaEvaluationMode);
	}

	public BeamSearchEvaluatorArray(EEvaluationDetail evaluationMode, final int beamSize) {
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
		return beamSearchDecoder(false, maxSize, scores);
	}

	private Score beamSearchDecoder(final boolean storeAssignemnts, final int maxSize, final Score[][] scores) {
		/*
		 * Init beam
		 */
		List<Assignment> assignments = new ArrayList<>();

		for (int from = 0; from < scores.length; from++) {
			for (int to = 0; to < scores[from].length; to++) {
				assignments.add(new Assignment(storeAssignemnts, maxSize, from, to, scores[from][to]));
			}
		}

		Collections.sort(assignments);

		Assignment[] array = new Assignment[beamSize];
		int i = 0;
		for (Assignment assignment : assignments.subList(0, Math.min(assignments.size(), beamSize))) {
			array[i] = assignment;
			i++;
		}

		Assignment bestAssignment = beamSearchAssignment(scores, array);
		System.out.println(bestAssignment.getAssignments());
		return bestAssignment.score;
	}

	@Override
	protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return stdEvalForDocLinked.evalEqualsMultiValues(annotations, otherAnnotations);
	}

	private Assignment beamSearchAssignment(Score[][] scores, Assignment[] assignments) {

//		for (Assignment assignment : assignments) {
//			System.out.println(assignment);
//		}
//		System.out.println("----");
		Assignment[] newAssignments = new Assignment[beamSize];

//		double currentThreshold = 0;

		// Turn off to allow beam search getting (temporally worse)
//		for (int beamIndex = 0; beamIndex < beamSize; beamIndex++) {
//			currentThreshold = Math.max(currentThreshold, assignments[beamIndex].score.getF1());
//		}
		boolean update = false;
		int indexOfNullValue = 0;
		for (Assignment assignment : assignments) {
			for (int from = 0; from < scores.length; from++) {
				if (assignment.isFrom(from))
					continue;
				// same size
				for (int to = 0; to < scores.length; to++) {
					if (assignment.isTo(to))
						continue;

					final Score score = scores[from][to];

					int indexOfSmallestEntry = -1;

					if (beamSize == indexOfNullValue) {
//						if (score.getF1() < currentThreshold)
//							continue;
//						else {
						// get smallest value of array
						double smallestValue = Double.MAX_VALUE;
						for (int beamIndex = 0; beamIndex < beamSize; beamIndex++) {
							final double oldScoreF1 = newAssignments[beamIndex].score.getF1();
							if (oldScoreF1 <= smallestValue) {
								indexOfSmallestEntry = beamIndex;
								smallestValue = oldScoreF1;
//									currentThreshold = smallestValue;
								update = true;

//								}
							}
						}
					} else {
						indexOfSmallestEntry = indexOfNullValue;
						indexOfNullValue++;
						update = true;
					}

					// if there is a value that score is smaller than a new one, that is there is an
					// update.

					// clone assignment and add new assignment
					if (indexOfSmallestEntry != -1) {
						Assignment a = new Assignment(assignment).addAssignment(from, to, score);
						if (newAssignments[indexOfSmallestEntry] == null
								|| newAssignments[indexOfSmallestEntry].score.getF1() <= a.score.getF1())
							newAssignments[indexOfSmallestEntry] = a;
//					System.out.println(newAssignments[indexOfSmallestEntry]);
					}
				}
			}

		}

		if (!update) {
			double highest = 0;
			int indexOfHighestEntry = -1;
			for (int beamIndex = 0; beamIndex < beamSize; beamIndex++) {
				final double assignmentScoreF1 = assignments[beamIndex].score.getF1();
				if (assignmentScoreF1 > highest) {
					indexOfHighestEntry = beamIndex;
					highest = assignmentScoreF1;
				}
			}
			return assignments[indexOfHighestEntry];
		}

		return beamSearchAssignment(scores, newAssignments);
	}

	static class Assignment implements Comparable<Assignment> {

		public final Score score;
		public final boolean[] from;
		public final boolean[] to;
		private final int maxSize;
		final private boolean storeAssignments;
		private List<Integer> assignmentsPairs;

		/*
		 * Clone
		 */
		public Assignment(Assignment assignments) {
			this.maxSize = assignments.maxSize;
			this.score = new Score(assignments.score);
			this.from = Arrays.copyOf(assignments.from, assignments.maxSize);
			this.to = Arrays.copyOf(assignments.to, assignments.maxSize);
			this.storeAssignments = assignments.storeAssignments;
			if (this.storeAssignments)
				this.assignmentsPairs = new ArrayList<>(assignments.assignmentsPairs);
		}

		public boolean isTo(int to2) {
			return to[to2];
		}

		public boolean isFrom(int from2) {
			return from[from2];
		}

		/*
		 * Init
		 */
		public Assignment(boolean storeAssignemnts, int maxSize, int from, int to, Score score) {
			this.maxSize = maxSize;
			this.score = score;
			this.from = new boolean[maxSize];
			this.to = new boolean[maxSize];
			this.from[from] = true;
			this.to[to] = true;
			this.storeAssignments = storeAssignemnts;
			if (this.storeAssignments) {
				this.assignmentsPairs = new ArrayList<>(maxSize);
				for (int i = 0; i < maxSize; i++) {
					this.assignmentsPairs.add(i);
				}
				this.assignmentsPairs.set(from, to);
			}
		}

		public Assignment addAssignment(int from, int to, Score score) {
			this.score.add(score);
			this.from[from] = true;
			this.to[to] = true;
			if (this.storeAssignments)
				this.assignmentsPairs.set(from, to);
			return this;
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
					+ ", maxSize=" + maxSize + ", storeAssignments=" + storeAssignments + ", assignmentsPairs="
					+ assignmentsPairs + "]";
		}

		public List<Integer> getAssignments() {
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
				assignments.add(new Assignment(true, maxSize, from, to, scores[from][to]));
			}
		}

		Collections.sort(assignments);
		Assignment[] array = new Assignment[beamSize];
		int i = 0;
		for (Assignment assignment : assignments.subList(0, Math.min(assignments.size(), beamSize))) {
			array[i++] = assignment;
		}

		Assignment bestAssignment = beamSearchAssignment(scores, array);

		return bestAssignment.getAssignments();
	}

}

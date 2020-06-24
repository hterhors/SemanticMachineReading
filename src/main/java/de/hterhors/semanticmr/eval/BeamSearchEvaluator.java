package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.plaf.synth.SynthDesktopIconUI;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.eval.BeamSearchEvaluator.Assignment;

public class BeamSearchEvaluator extends AbstractEvaluator {

	final public int beamSize;

//	Score [getF1()=0.971, getPrecision()=0.972, getRecall()=0.970, tp=625, fp=18, fn=19, tn=0]
//			83310
//8500 mb ram by 300

//	Score [getF1()=0.971, getPrecision()=0.972, getRecall()=0.970, tp=625, fp=18, fn=19, tn=0]
//			1070223
//7500 mb ram by 300

//	Score [getF1()=0.974, getPrecision()=0.971, getRecall()=0.977, tp=637, fp=19, fn=15, tn=0]
//			66969
//7800

//	7700RAM by 600
//	Score [getF1()=0.988, getPrecision()=0.988, getRecall()=0.988, tp=1433, fp=17, fn=18, tn=0]
//			76649

//	Score [getF1()=0.990, getPrecision()=0.987, getRecall()=0.992, tp=1432, fp=19, fn=11, tn=0]
//			83766

//	[71, 61, 1, 32, 38, 75, 5, 3, 64, 12, 22, 21, 20, 16, 37, 17, 30, 8, 47, 49, 19, 43, 90, 70, 2, 117, 269, 14, 10, 13, 48, 23, 54, 4, 58, 44, 25, 11, 97, 18, 7, 87, 15, 9, 89, 88, 138, 62, 220, 67, 24, 40, 33, 92, 59, 27, 29, 26, 128, 123, 55, 34, 63, 78, 141, 74, 39, 143, 6, 66, 125, 53, 93, 99, 35, 114, 77, 119, 31, 41, 137, 79, 60, 142, 50, 52, 185, 0, 104, 115, 28, 286, 72, 73, 57, 164, 249, 122, 229, 133, 95, 237, 139, 81, 197, 85, 254, 183, 118, 113, 124, 83, 45, 212, 101, 80, 111, 36, 196, 129, 200, 56, 146, 145, 174, 181, 199, 156, 65, 86, 110, 68, 51, 148, 76, 96, 163, 173, 152, 134, 230, 108, 136, 213, 160, 116, 94, 135, 102, 100, 176, 112, 251, 130, 42, 150, 202, 153, 127, 157, 175, 82, 69, 84, 219, 121, 179, 169, 46, 180, 109, 281, 217, 211, 221, 214, 201, 178, 103, 192, 187, 345, 106, 182, 144, 189, 132, 98, 131, 227, 147, 224, 233, 91, 263, 155, 184, 190, 165, 188, 245, 105, 194, 299, 167, 207, 205, 162, 250, 243, 191, 259, 240, 166, 353, 154, 140, 120, 170, 349, 107, 248, 318, 158, 149, 151, 203, 363, 168, 195, 126, 257, 222, 186, 171, 198, 177, 260, 226, 215, 161, 238, 288, 264, 242, 210, 231, 280, 258, 333, 293, 235, 193, 208, 283, 262, 241, 206, 265, 253, 246, 304, 159, 284, 287, 272, 228, 285, 317, 239, 381, 315, 232, 320, 385, 172, 310, 223, 204, 271, 307, 311, 367, 276, 364, 278, 266, 277, 326, 236, 216, 290, 209, 296, 244, 294, 282, 336, 247, 322, 384, 305, 273, 270, 413, 295, 324, 292, 252, 225, 346, 360, 371, 332, 331, 303, 416, 302, 261, 274, 314, 300, 357, 275, 234, 289, 306, 255, 341, 337, 325, 256, 340, 358, 279, 308, 411, 330, 301, 291, 401, 435, 397, 335, 387, 267, 373, 319, 327, 298, 268, 394, 451, 382, 312, 339, 313, 348, 433, 334, 377, 355, 362, 407, 323, 344, 392, 218, 365, 447, 366, 376, 321, 403, 350, 354, 356, 484, 342, 370, 422, 338, 393, 492, 465, 420, 441, 375, 402, 297, 329, 418, 408, 445, 343, 399, 368, 396, 425, 424, 404, 432, 378, 390, 475, 388, 391, 457, 421, 372, 417, 410, 369, 514, 525, 328, 498, 423, 597, 471, 347, 383, 464, 438, 409, 398, 456, 419, 316, 466, 515, 487, 405, 449, 374, 453, 455, 400, 406, 430, 461, 472, 309, 352, 379, 431, 427, 361, 511, 486, 351, 507, 395, 452, 469, 428, 528, 531, 546, 359, 448, 440, 566, 434, 412, 468, 446, 501, 450, 414, 476, 460, 429, 474, 458, 481, 470, 500, 477, 462, 478, 443, 483, 508, 561, 480, 545, 479, 415, 586, 436, 513, 473, 549, 491, 459, 386, 463, 467, 439, 380, 505, 558, 493, 534, 536, 502, 437, 540, 442, 569, 494, 444, 488, 510, 506, 562, 518, 485, 523, 454, 517, 541, 564, 554, 539, 557, 426, 580, 559, 489, 593, 543, 504, 548, 565, 495, 389, 577, 490, 575, 482, 496, 572, 512, 527, 535, 537, 556, 560, 522, 587, 520, 538, 573, 589, 516, 551, 570, 499, 594, 544, 533, 524, 547, 598, 553, 509, 519, 584, 567, 590, 596, 521, 532, 497, 568, 576, 526, 530, 588, 579, 550, 555, 585, 503, 592, 581, 574, 563, 591, 529, 578, 571, 595, 599, 583, 542, 582, 552]
//			Score [getF1()=0.990, getPrecision()=0.987, getRecall()=0.992, tp=1432, fp=19, fn=11, tn=0]
//			78348

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		int maxSize = 795;
		Score[][] scores = new Score[maxSize][maxSize];
		Random rand = new Random(10);

		for (int i = 0; i < scores.length; i++) {
			for (int j = 0; j < scores[i].length; j++) {
				scores[i][j] = i % 3 == 0 ? new Score() : new Score(rand.nextInt(2), rand.nextInt(5), rand.nextInt(5));
//				System.out.println(i + "," + j + " = " + scores[i][j]);
			}
		}
		System.out.println("Start decoding...");

		BeamSearchEvaluator f = new BeamSearchEvaluator(EEvaluationDetail.ENTITY_TYPE, 10);

		System.out.println(f.beamSearchDecoder(maxSize, scores));
		System.out.println((System.currentTimeMillis() - time));
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
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {
		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		/*
		 * Init scores
		 */
		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize, scoreType);
		return beamSearchDecoder(maxSize, scores);
	}

	private Score beamSearchDecoder(final int maxSize, final Score[][] scores) {
		/*
		 * Init beam
		 */
		List<Assignment> assignments = new ArrayList<>();

		for (int from = 0; from < scores.length; from++) {
			for (int to = 0; to < scores[from].length; to++) {
				assignments.add(new Assignment(false, maxSize, from, to, scores[from][to]));
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
		double currentThreshold = 0;
		List<Assignment> newAssignments = new ArrayList<>();

		for (Assignment assignment : assignments) {
			for (int from = 0; from < scores.length; from++) {
				if (assignment.from[from])
					continue;
				for (int to = 0; to < scores[from].length; to++) {
					if (assignment.to[to])
						continue;

					final Score score = scores[from][to];

					final double newPotentialScore = score.getF1() + assignment.score.getF1();

					if (newPotentialScore < currentThreshold) {
						if (newAssignments.size() >= beamSize) {
							continue;
						}
					} else {
						currentThreshold = newPotentialScore;
					}

					if (score.getF1() == 0 && newAssignments.size() > beamSize)
						continue;

					// clone assignment
					Assignment newAssignemnt = new Assignment(assignment);
					// add new assignment
					newAssignemnt.addAssignment(from, to, score);

					newAssignments.add(newAssignemnt);
				}
			}

		}
		if (newAssignments.isEmpty()) {
			return assignments.get(0);
		}
		Collections.sort(newAssignments);
		return beamSearchAssignment(scores, newAssignments.subList(0, Math.min(newAssignments.size(), beamSize)));
	}

	static class Assignment implements Comparable<Assignment> {

		public final Score score;

		public final boolean[] from;
		public final boolean[] to;
		private final int maxSize;

		private int[] assignmentsPairs;
		final private boolean storeAssignments;

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
				this.assignmentsPairs = Arrays.copyOf(assignments.assignmentsPairs, assignments.maxSize);
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
				this.assignmentsPairs = new int[maxSize];
				for (int i = 0; i < maxSize; i++) {
					this.assignmentsPairs[i] = i;
				}
				this.assignmentsPairs[from] = to;
			}
		}

		public Assignment addAssignment(int from, int to, Score score) {
			this.score.add(score);
			this.from[from] = true;
			this.to[to] = true;
			if (this.storeAssignments)
				this.assignmentsPairs[from] = to;
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
					+ ", maxSize=" + maxSize + ", assignmentsPairs=" + Arrays.toString(assignmentsPairs)
					+ ", storeAssignments=" + storeAssignments + "]";
		}

		public List<Integer> getAssignments() {
			if (this.assignmentsPairs == null) {
				return Collections.emptyList();
			}
			List<Integer> assignment = new ArrayList<>();
			for (int i = 0; i < this.assignmentsPairs.length; i++) {
				assignment.add(this.assignmentsPairs[i]);
			}
			return assignment;
		}

	}

	protected Score[][] computeScores(final Collection<? extends AbstractAnnotation> slotFiller,
			final Collection<? extends AbstractAnnotation> otherSlotFiller, final int maxSize, EScoreType scoreType) {

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
				if (scoreType == EScoreType.MACRO)
					scores[i][j] = scores[i][j].toMacro();
				j++;
			}
			i++;

		}

		return scores;
	}

	@Override
	public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {

		final int maxSize = Math.max(annotations.size(), otherAnnotations.size());

		/*
		 * Init scores
		 */
		final Score[][] scores = computeScores(annotations, otherAnnotations, maxSize, scoreType);

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

		Assignment bestAssignment = beamSearchAssignment(scores,
				assignments.subList(0, Math.min(assignments.size(), beamSize)));

		return bestAssignment.getAssignments();
	}

}

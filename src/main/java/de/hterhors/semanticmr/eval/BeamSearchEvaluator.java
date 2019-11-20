package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public class BeamSearchEvaluator extends AbstractEvaluator {

	final public int beamSize;

	public BeamSearchEvaluator(EEvaluationDetail evaluationMode, final int beamSize) {
		super(evaluationMode);
		this.beamSize = beamSize;
	}

	@Override
	public Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {

		final List<BeamAssignmentTree> assignments = new ArrayList<>();

		assignments.add(new BeamAssignmentTree(new ArrayList<>(annotations), new ArrayList<>(otherAnnotations)));

		final BeamAssignmentTree bestAssignments = beamExploration(assignments).get(0);

		return bestAssignments.overallSimiliarity;
	}

	/**
	 * TODO: make faster by pre calculate all scores and access score[][]
	 * 
	 * 
	 * @param states
	 * @return
	 */
	private List<BeamAssignmentTree> beamExploration(final List<BeamAssignmentTree> states) {

		final List<BeamAssignmentTree> candidates = new ArrayList<>();

		boolean done = true;

		for (BeamAssignmentTree current : states) {

			if (current.checkBreakCondition())
				continue;

			done = false;

			final int maxSize = Math.max(current.remainingGold.size(), current.remainingPrediction.size());

			for (int goldListIndex = 0; goldListIndex < maxSize; goldListIndex++) {

				/*
				 * Get gold object if any, otherwise empty instance.
				 */
				final AbstractAnnotation goldThing;
				if (current.remainingGold.size() > goldListIndex)
					goldThing = current.remainingGold.get(goldListIndex);
				else
					goldThing = null;

				for (int predictionListIndex = 0; predictionListIndex < maxSize; predictionListIndex++) {

					/*
					 * Get prediction object if any, otherwise empty instance.
					 */
					final AbstractAnnotation predThing;

					if (current.remainingPrediction.size() > predictionListIndex)
						predThing = current.remainingPrediction.get(predictionListIndex);
					else
						predThing = null;

					/*
					 * Clone
					 */
					BeamAssignmentTree candidate = new BeamAssignmentTree(current);

					/*
					 * Score
					 */
					Score similarity;
					if (goldThing == null) {
						similarity = scoreSingle(predThing, goldThing).invert();
					} else {
						similarity = scoreSingle(goldThing, predThing);
					}

					/*
					 * Add assignment to assignment list in tree.
					 */
					candidate.addAssignment(new BeamAssignment(goldThing, predThing, similarity));

					/*
					 * Add candidate to possible successor.
					 */
					candidates.add(candidate);

				}
			}

		}

		if (done)
			return states;

		final List<BeamAssignmentTree> successorStates = candidates.stream().sorted().limit(beamSize)
				.collect(Collectors.toList());

		return beamExploration(successorStates);

	}

	class BeamAssignmentTree implements Comparable<BeamAssignmentTree> {

		final private List<BeamAssignment> assignments;
		final private List<AbstractAnnotation> remainingGold;
		final private List<AbstractAnnotation> remainingPrediction;
		final public Score overallSimiliarity;

		/**
		 * Initial.
		 * 
		 * @param assignments
		 * @param gold
		 * @param prediction
		 */
		public BeamAssignmentTree(List<AbstractAnnotation> gold, List<AbstractAnnotation> prediction) {
			this.assignments = new ArrayList<>();
			this.remainingGold = gold;
			this.remainingPrediction = prediction;
			this.overallSimiliarity = new Score();
		}

		/**
		 * Clone.
		 * 
		 * @param tree
		 */
		public BeamAssignmentTree(BeamAssignmentTree tree) {
			this.assignments = new ArrayList<>(tree.assignments);
			this.remainingGold = new ArrayList<>(tree.remainingGold);
			this.remainingPrediction = new ArrayList<>(tree.remainingPrediction);
			this.overallSimiliarity = new Score(tree.overallSimiliarity);
		}

		@Override
		public int compareTo(BeamAssignmentTree o) {
			return -Double.compare(overallSimiliarity.getF1(), o.overallSimiliarity.getF1());
		}

		public void addAssignment(BeamAssignment beamAssignment) {
			this.assignments.add(beamAssignment);
			this.remainingGold.remove(beamAssignment.gold);
			this.remainingPrediction.remove(beamAssignment.pred);
			this.overallSimiliarity.add(beamAssignment.similiarity);
		}

		public boolean checkBreakCondition() {
			return remainingGold.size() == 0 && remainingPrediction.size() == 0;
		}

		@Override
		public String toString() {
			return "BeamAssignmentTree [assignments=" + assignments + ", remainingGold=" + remainingGold
					+ ", remainingPrediction=" + remainingPrediction + ", overallSimiliarity=" + overallSimiliarity
					+ "]";
		}

	}

	class BeamAssignment implements Comparable<BeamAssignment> {

		final public AbstractAnnotation gold;
		final public AbstractAnnotation pred;
		final public Score similiarity;

		public BeamAssignment(AbstractAnnotation gold, AbstractAnnotation pred, Score similiarity) {
			this.gold = gold;
			this.pred = pred;
			this.similiarity = similiarity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gold == null) ? 0 : gold.hashCode());
			result = prime * result + ((pred == null) ? 0 : pred.hashCode());
			result = prime * result + ((similiarity == null) ? 0 : similiarity.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BeamAssignment other = (BeamAssignment) obj;
			if (gold == null) {
				if (other.gold != null)
					return false;
			} else if (!gold.equals(other.gold))
				return false;
			if (pred == null) {
				if (other.pred != null)
					return false;
			} else if (!pred.equals(other.pred))
				return false;
			if (similiarity == null) {
				if (other.similiarity != null)
					return false;
			} else if (!similiarity.equals(other.similiarity))
				return false;
			return true;
		}

		@Override
		public int compareTo(BeamAssignment o) {
			return -Double.compare(this.similiarity.getF1(), o.similiarity.getF1());
		}

		@Override
		public String toString() {
			return "BeamAssignment [gold=" + gold + ", pred=" + pred + ", similiarity=" + similiarity + "]";
		}

	}
}

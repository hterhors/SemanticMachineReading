package de.hterhors.semanticmr.eval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.Annotations;

public class NerlaEvaluator implements IEvaluatable {

	public NerlaEvaluator() {
	}

	public static <E> double microPrecision(double tp, double fp, double fn) {
		return tp / (tp + fp);
	}

	public static <E> double microRecall(double tp, double fp, double fn) {
		return tp / (tp + fn);
	}

	public static <E> double microF1(double tp, double fp, double fn) {
		double p = microPrecision(tp, fp, fn);
		double r = microRecall(tp, fp, fn);
		double f1 = 2 * (p * r) / (p + r);
		return f1;
	}

	public static <E> int getTruePositives(Set<E> gold, Set<E> result) {
		Set<E> intersection = new HashSet<E>(result);
		intersection.retainAll(gold);
		return intersection.size();
	}

	public static <E> int getFalsePositives(Set<E> gold, Set<E> result) {
		Set<E> intersection = new HashSet<E>(result);
		intersection.retainAll(gold);

		return result.size() - intersection.size();

	}

	public static <E> int getFalseNegatives(Set<E> gold, Set<E> result) {
		Set<E> intersection = new HashSet<E>(result);
		intersection.retainAll(gold);

		return gold.size() - intersection.size();

	}

	public static <E> double precision(Set<E> gold, Set<E> result) {

		if (result.size() == 0) {
			return 0;
		}

		Set<E> intersection = new HashSet<E>(result);
		intersection.retainAll(gold);
		return (double) intersection.size() / result.size();

	}

	public static <E> double recall(Set<E> gold, Set<E> result) {

		if (gold.size() == 0) {
			return 0;
		}

		Set<E> intersection = new HashSet<E>(result);
		intersection.retainAll(gold);
		return (double) intersection.size() / gold.size();

	}

	public static <E> double f1(Set<E> gold, Set<E> result) {
		double p = precision(gold, result);
		double r = recall(gold, result);
		if (p == 0 && r == 0) {
			return 0;
		}
		double f1 = 2 * ((p * r) / (p + r));
		return f1;

	}

	public Score prf1(List<? extends EntityTypeAnnotation> gold, List<? extends EntityTypeAnnotation> predictions) {

		int tp;
		int fp;
		int fn;

		int intersectionSize = 0;

		for (EntityTypeAnnotation goldThing : gold) {
			intersectionSize += predictions.contains(goldThing) ? 1 : 0;
		}

		tp = intersectionSize;
		fp = predictions.size() - intersectionSize;
		fn = gold.size() - intersectionSize;

		return new Score(tp, fp, fn);
	}

	public Score prf1(EntityTypeAnnotation gold, EntityTypeAnnotation predictions) {
		return prf1(Arrays.asList(gold), Arrays.asList(predictions));
	}

	public double f1(EntityTypeAnnotation gold, EntityTypeAnnotation prediction) {
		return f1(new HashSet<>(Arrays.asList(gold)), new HashSet<>(Arrays.asList(prediction)));
	}

	public double recall(EntityTypeAnnotation gold, EntityTypeAnnotation prediction) {
		return recall(new HashSet<>(Arrays.asList(gold)), new HashSet<>(Arrays.asList(prediction)));
	}

	public double precision(EntityTypeAnnotation gold, EntityTypeAnnotation prediction) {
		return precision(new HashSet<>(Arrays.asList(gold)), new HashSet<>(Arrays.asList(prediction)));
	}

	public double recall(List<? extends EntityTypeAnnotation> gold, List<? extends EntityTypeAnnotation> predictions) {
		return recall(new HashSet<>(gold), new HashSet<>(predictions));
	}

	public double precision(List<? extends EntityTypeAnnotation> gold,
			List<? extends EntityTypeAnnotation> predictions) {
		return precision(new HashSet<>(gold), new HashSet<>(predictions));
	}

	public double f1(List<? extends EntityTypeAnnotation> gold, List<? extends EntityTypeAnnotation> predictions) {
		return f1(new HashSet<>(gold), new HashSet<>(predictions));
	}

	@Override
	public Score evaluate(EEvaluationDetail evaluationMode, Annotations goldAnnotations,
			Annotations currentPredictions) {
		/**
		 * TODO: implement evaluation details.
		 */
		return prf1(goldAnnotations.getAnnotations(), currentPredictions.getAnnotations());
	}

}

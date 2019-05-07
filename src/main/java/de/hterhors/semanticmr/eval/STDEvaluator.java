package de.hterhors.semanticmr.eval;

import java.util.HashSet;
import java.util.Set;

public class STDEvaluator {

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

}

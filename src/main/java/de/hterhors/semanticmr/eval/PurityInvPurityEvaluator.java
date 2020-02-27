package de.hterhors.semanticmr.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Streams;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;

public class PurityInvPurityEvaluator extends AbstractEvaluator {

	public PurityInvPurityEvaluator(EEvaluationDetail evaluationDetail) {
		super(evaluationDetail);
	}

	public static void main(String[] args) {

		Set<String> clusterA = new HashSet<>(Arrays.asList("A", "B", "C"));
		Set<String> clusterB = new HashSet<>(Arrays.asList("1", "2", "3", "4"));
		Set<String> cluster1 = new HashSet<>(Arrays.asList("A", "C"));
		Set<String> cluster2 = new HashSet<>(Arrays.asList("B"));
		Set<String> cluster3 = new HashSet<>(Arrays.asList("2"));
		Set<String> cluster4 = new HashSet<>(Arrays.asList("3"));

		List<Set<String>> goldCluster = Arrays.asList(clusterA, clusterB);

		List<Set<String>> predCluster = Arrays.asList(cluster1, cluster2, cluster3, cluster4);
//		System.out.println(purity(goldCluster, predCluster));
//		System.out.println(inversePurity(goldCluster, predCluster));
		System.out.println(fPurityInvPurity(goldCluster, predCluster).getPurityF());
		System.out.println();
//		System.out.println(purity(goldCluster, goldCluster));
//		System.out.println(inversePurity(goldCluster, goldCluster));
		System.out.println(fPurityInvPurity(goldCluster, goldCluster).getPurityF());
	}

//	public PurityInvPurityEvaluator(EEvaluationDetail evaluationDetail) {
//		super(evaluationDetail);
//	}

//	public static double inversePurity(List<Set<String>> gold,
//			List<Set<String>> pred) {
//		return purity(pred, gold);
//	}
//
//	public static double purity(List<Set<String>> gold,
//			List<Set<String>> pred) {
//
//		double purity = 0;
//		double N = Streams.concat(gold.stream(), pred.stream()).flatMap(s -> s.stream()).distinct().count();
//
//		for (Set<String> predictionCluster : pred) {
//			double maxVal = 0;
//
//			for (Set<String> goldCluster : gold) {
//				maxVal = Math.max(maxVal, precision(predictionCluster, goldCluster));
//			}
//			purity += (predictionCluster.size() / N) * maxVal;
//		}
//		return purity;
//	}

	private static Score recall(Set<String> c1, Set<String> c2) {
		return precision(c2, c1);
	}

	private static Score precision(Set<String> c1, Set<String> c2) {

		if (c1.isEmpty() || c2.isEmpty())
			return Score.ZERO;

		int count = 0;
		for (String c1Term : c1) {
			for (String c2Term : c2) {
				if (c1Term.equals(c2Term)) {
					count++;
					break;
				}

			}
		}
//		for (AbstractAnnotation c1Term : c1) {
//			for (AbstractAnnotation c2Term : c2) {
//				if (c1Term.evaluate(EEvaluationDetail.ENTITY_TYPE, c2Term).getF1() == 1.0D) {
//					count++;
//					break;
//				}
//				
//			}
//		}

		return new Score(count, 0, c1.size());
	}

	public static Score fPurityInvPurity(List<Set<String>> gold, List<Set<String>> pred) {
		/**
		 * TODO: make more efficient
		 */

		int N = (int) Streams.concat(gold.stream(), pred.stream()).flatMap(s -> s.stream()).distinct().count();
		List<Score> scores = new ArrayList<>();
		for (Set<String> goldCluster : gold) {
			Score maxVal = new Score();

			for (Set<String> predCluster : pred) {
				final Score tmpPF = calcF(goldCluster, predCluster);
				if (maxVal.getPurityF() < tmpPF.getPurityF())
					maxVal = tmpPF;

			}
			if (maxVal.getPurityF() != 0.0D)
				scores.add(new Score((goldCluster.size() * maxVal.getTp()), 0, maxVal.getFn()));

		}
		/*
		 * chain rule.
		 */
		int tp = 0;
		int fn = N;
		for (int i = 0; i < scores.size(); i++) {

			fn *= scores.get(i).getFn();
			int tpTmp = scores.get(i).getTp();
			for (int j = 0; j < scores.size(); j++) {
				if (i == j)
					continue;

				tpTmp *= scores.get(j).getFn();

			}
			tp += tpTmp;

		}
		return new Score(tp, 0, fn);
	}

	private static Score calcF(Set<String> c1, Set<String> c2) {

		Score r = recall(c1, c2);
		if (r.getPurity() == 0)
			return Score.ZERO;
		Score p = precision(c1, c2);
		if (p.getPurity() == 0)
			return Score.ZERO;

		Score f = new Score();
		f.add(r);
		f.add(p);

		return f;
	}

	@Override
	protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoretype) {
//		return fPurityInvPurity(annotations, otherAnnotations);
		return null;
	}

}

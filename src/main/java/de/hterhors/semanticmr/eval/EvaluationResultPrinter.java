package de.hterhors.semanticmr.eval;

import java.util.Map;
import java.util.Map.Entry;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class EvaluationResultPrinter {

	public static void evaluate(Map<Instance, State> testResults) {
		Score mean = new Score();

		for (Entry<Instance, State> res : testResults.entrySet()) {

			System.out.println(res.getKey().getName());
			System.out.println("Model score: " + res.getValue().getModelScore());
			System.out.println("Objective score: " + res.getValue().getObjectiveScore());
			System.out.println("Score: " + res.getValue().getScore());
			mean.add(res.getValue().getScore());
			for (AbstractAnnotation goldAnnotations : res.getKey().getGoldAnnotations().getAnnotations()) {
				System.out.println(goldAnnotations.toPrettyString());
			}
			System.out.println("-----------");
			for (AbstractAnnotation finalAnnotations : res.getValue().getCurrentPredictions().getAnnotations()) {
				System.out.println(finalAnnotations.toPrettyString());
			}
			System.out.println();
			System.out.println();
			System.out.println();
		}
		System.out.println("Mean Score: " + mean);

	}

}

package de.hterhors.semanticmr.projects;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.corpus.log.LogUtils;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.specifications.SystemScope;

public class AbstractSemReadProject {
	CartesianEvaluator c = new CartesianEvaluator(EEvaluationDetail.LITERAL);

	public AbstractSemReadProject(SystemScope build) {
	}

	public void evaluate(Logger log, Map<Instance, State> testResults) {
		Score mean = new Score();
		for (Entry<Instance, State> res : testResults.entrySet()) {

			if (res.getKey().getDocument().documentID.equals("N008 Jin 2016 26852702")) {
				System.out.println(c.scoreMultiValues(res.getKey().getGoldAnnotations().getAnnotations(),
						res.getValue().getCurrentPredictions().getAnnotations()));
			}

			mean.add(res.getValue().getScore());
			LogUtils.logState(log, "======Final Evaluation======", res.getKey(), res.getValue());
		}
		log.info("Mean Score: " + mean);

	}
}

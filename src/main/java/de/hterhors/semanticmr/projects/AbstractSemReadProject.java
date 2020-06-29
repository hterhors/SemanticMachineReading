package de.hterhors.semanticmr.projects;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class AbstractSemReadProject {

//	public AbstractSemReadProject(SystemScope scope) {
//		this.scope = scope;
//	}

	public static Score evaluate(Logger log, Map<Instance, State> testResults, IObjectiveFunction predictionOF) {

		Score mean = new Score();

		for (Entry<Instance, State> res : testResults.entrySet()) {

			log.info("Number to compare: " + res.getValue().getCurrentPredictions().getAnnotations().size() + " with "
					+ res.getKey().getGoldAnnotations().getAbstractAnnotations().size());

			for (Iterator<AbstractAnnotation> iterator = res.getValue().getCurrentPredictions().getAnnotations()
					.iterator(); iterator.hasNext();) {
				AbstractAnnotation a = iterator.next();
				if (a.isInstanceOfEntityTemplate() && a.asInstanceOfEntityTemplate().isEmpty()
						&& a.getEntityType().getTransitiveClosureSuperEntityTypes().isEmpty()) {
					iterator.remove();
				}
			}

			if (predictionOF != null)
				predictionOF.score(res.getValue());

			mean.add(res.getValue().getMicroScore());

			LogUtils.logState(log, "======Final Evaluation======", res.getKey(), res.getValue());
		}
		return mean;

	}

	public void evaluate(Logger log, Map<Instance, State> testResults) {
		evaluate(log, testResults, null);
	}
}

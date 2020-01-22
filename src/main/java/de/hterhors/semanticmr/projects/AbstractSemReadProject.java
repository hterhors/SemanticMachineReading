package de.hterhors.semanticmr.projects;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.init.specifications.SystemScope;

public class AbstractSemReadProject {

	protected final SystemScope scope;

	public AbstractSemReadProject(SystemScope scope) {
		this.scope = scope;
	}

	public static Score evaluate(Logger log, Map<Instance, State> testResults, IObjectiveFunction predictionOF) {

		Score mean = new Score();

		for (Entry<Instance, State> res : testResults.entrySet()) {
			if (predictionOF != null)
				predictionOF.score(res.getValue());

		
			mean.add(res.getValue().getScore());
			
			LogUtils.logState(log, "======Final Evaluation======", res.getKey(), res.getValue());
		}
		return mean;

	}

	public void evaluate(Logger log, Map<Instance, State> testResults) {
		evaluate(log, testResults, null);
	}
}

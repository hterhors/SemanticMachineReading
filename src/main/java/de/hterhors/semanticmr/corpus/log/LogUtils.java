package de.hterhors.semanticmr.corpus.log;

import java.text.DecimalFormat;

import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

public class LogUtils {

	public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");

	public static void logState(Logger logger, String context, Instance instance, State currentState) {
		logger.info("***********************************************************");
		logger.info("|" + context + "_____________" + instance.getName() + "_____________|");
		logger.info("Final State  Model[" + LogUtils.SCORE_FORMAT.format(currentState.getModelScore()) + "] Objective["
				+ LogUtils.SCORE_FORMAT.format(currentState.getObjectiveScore()) + "] {");
		logger.info("GOLD [" + currentState.getGoldAnnotations().getAnnotations().size() + "]:");
		for (AbstractAnnotation ga : instance.getGoldAnnotations().getAnnotations()) {
			logger.info(ga.toPrettyString());
		}
		logger.info("}");
		logger.info("PREDICT [" + currentState.getCurrentPredictions().getAnnotations().size() + "]:");
		for (AbstractAnnotation ga : currentState.getCurrentPredictions().getAnnotations()) {
			logger.info(ga.toPrettyString());
		}
		logger.info("}");
		logger.info(currentState.getScore());
		logger.info("***********************************************************");
		logger.info("\n");
	}

}

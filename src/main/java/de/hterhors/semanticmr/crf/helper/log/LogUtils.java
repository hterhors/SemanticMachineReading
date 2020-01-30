package de.hterhors.semanticmr.crf.helper.log;

import java.text.DecimalFormat;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.StatePair;
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

	public static void logMultipleStates(Logger logger, String context, Instance instance,
			Collection<StatePair> currentStatePairs) {
		logger.info("***********************************************************");
		int count = 1;
		for (StatePair statePair : currentStatePairs) {

			logger.info("|" + count++ + "/" + currentStatePairs.size() + "||" + context + "_____________"
					+ instance.getName() + "_____________|");
			logger.info("Final State  Model[" + LogUtils.SCORE_FORMAT.format(statePair.currentState.getModelScore())
					+ "] Objective[" + LogUtils.SCORE_FORMAT.format(statePair.currentState.getObjectiveScore())
					+ "] {");
			logger.info("GOLD [" + statePair.currentState.getGoldAnnotations().getAnnotations().size() + "]:");
			for (AbstractAnnotation ga : instance.getGoldAnnotations().getAnnotations()) {
				logger.info(ga.toPrettyString());
			}
			logger.info("}");
			logger.info("PREDICT [" + statePair.currentState.getCurrentPredictions().getAnnotations().size() + "]:");
			for (AbstractAnnotation ga : statePair.currentState.getCurrentPredictions().getAnnotations()) {
				logger.info(ga.toPrettyString());
			}
			logger.info("}");
			logger.info(statePair.currentState.getScore());
			logger.info("++++++++++++++++++++++++++++++++");
		}
		logger.info("***********************************************************");
		logger.info("\n");
	}

}

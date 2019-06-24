package de.hterhors.semanticmr.crf.sampling.stopcrit.impl;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

public class NoModelChangeCrit implements ITrainingStoppingCriterion {

	public static Logger log = LogManager.getFormatterLogger(NoModelChangeCrit.class);

	double threshold = 0.001;
	double prevMeanWeight = 0;
	int prevCountWeights = 0;
	private Model model;

	public NoModelChangeCrit(Model model) {
		this.model = model;
	}

	@Override
	public boolean meetsCondition(Collection<State> producedStateChain) {

		log.info("Check stop training criterion: " + this.getClass().getSimpleName());
		log.info("Current mean weight: " + prevMeanWeight);
		log.info("Current feature count: " + prevCountWeights);

		double meanWeight = 0;
		int countWeights = 0;
//		for (AbstractFeatureTemplate<?> template : model.getFactorTemplates()) {
//			for (double weight : template.getWeights().getFeatures()) {
//				meanWeight += weight;
//				countWeights++;
//			}
//		}
		for (AbstractFeatureTemplate template : model.getFactorTemplates()) {
			for (Double weight : template.getWeights().getFeatures().values()) {
				meanWeight += weight.doubleValue();
				countWeights++;
			}
		}

		meanWeight /= countWeights;
		log.info("New mean weight: " + meanWeight);
		log.info("New feature count: " + countWeights);
		log.info("Threshold: " + threshold);
		log.info("Weight updates < threshold: " + (Math.abs(prevMeanWeight - meanWeight) < threshold));
		log.info("Feature count equal: " + (countWeights == prevCountWeights));
		boolean meetConditions = Math.abs(prevMeanWeight - meanWeight) < threshold && countWeights == prevCountWeights;
		log.info("Meet conditions: " + meetConditions);

		prevCountWeights = countWeights;
		prevMeanWeight = meanWeight;
		return meetConditions;

	}

}

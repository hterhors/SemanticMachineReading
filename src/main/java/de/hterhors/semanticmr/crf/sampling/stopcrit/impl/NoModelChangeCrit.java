package de.hterhors.semanticmr.crf.sampling.stopcrit.impl;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

public class NoModelChangeCrit implements ITrainingStoppingCriterion {

	public static Logger log = LogManager.getFormatterLogger(NoModelChangeCrit.class.getSimpleName());

	double threshold = 0.001;
	double prevMeanWeight = 0;
	int prevCountWeights = 0;
	private Model model;

	public NoModelChangeCrit(Model model) {
		this.model = model;
	}

	@Override
	public boolean meetsCondition(Collection<State> producedStateChain) {

		double meanWeight = 0;
		int countWeights = 0;
		for (AbstractFeatureTemplate<?> template : model.getFactorTemplates()) {
			for (Double weight : template.getWeights().getFeatures().values()) {
				meanWeight += weight.doubleValue();
				countWeights++;
			}
		}

		meanWeight /= countWeights;

		boolean meetConditions = Math.abs(prevMeanWeight - meanWeight) < threshold && countWeights == prevCountWeights;

		prevCountWeights = countWeights;
		prevMeanWeight = meanWeight;
		return meetConditions;

	}

}

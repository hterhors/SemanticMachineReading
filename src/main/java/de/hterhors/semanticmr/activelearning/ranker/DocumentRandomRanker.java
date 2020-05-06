package de.hterhors.semanticmr.activelearning.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.activelearning.IActiveLearningDocumentRanker;
import de.hterhors.semanticmr.crf.variables.Instance;

public class DocumentRandomRanker implements IActiveLearningDocumentRanker {

	final Logger log = LogManager.getRootLogger();

	final Random random;

	public DocumentRandomRanker() {
		random = new Random(123456L);
	}

	@Override
	public List<Instance> rank(List<Instance> remainingInstances) {
		log.info("Apply random rank...");
		log.info("Copy...");
		List<Instance> randomized = new ArrayList<>(remainingInstances);

//		List<RankedInstance> objectiveInstances = new ArrayList<>();

		log.info("Sort...");
		Collections.sort(randomized);

		log.info("Shuffle...");
		Collections.shuffle(randomized, random);

//		log.info("Analyze objectve score...");
//		List<SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState>> predictions = runner
//				.test(remainingInstances);
//
//		for (SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> sampledInstance : predictions) {
//			runner.scoreWithObjectiveFunction(sampledInstance.getState());
//
//			final double inverseObjectiveRank = 1 - sampledInstance.getState().getObjectiveScore();
//			objectiveInstances.add(new RankedInstance(inverseObjectiveRank, sampledInstance.getInstance()));
//		}
//		Collections.sort(objectiveInstances);
//		SpearmansCorrelation s = new SpearmansCorrelation();
//
//		double[] entropy = randomized.stream().map(i -> (double) i.getName().hashCode())
//				.mapToDouble(Double::doubleValue).toArray();
//		double[] objective = objectiveInstances.stream().map(i -> (double) i.instance.getName().hashCode())
//				.mapToDouble(Double::doubleValue).toArray();
//
//		double correlation = s.correlation(entropy, objective);
//
//		final double meanObjectiveF1Score = objectiveInstances.stream().map(i -> i.value).reduce(0D, Double::sum)
//				/ objectiveInstances.size();
//
//		log.info("Spearmans Correlation: " + correlation);
//		log.info("Mean F1 score (Objective): " + meanObjectiveF1Score);
		return randomized;
	}

}

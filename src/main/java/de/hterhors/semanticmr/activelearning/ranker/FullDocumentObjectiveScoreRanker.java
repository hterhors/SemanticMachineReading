package de.hterhors.semanticmr.activelearning.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import corpus.SampledInstance;
import de.hterhors.obie.ml.run.AbstractOBIERunner;
import de.hterhors.obie.ml.variables.InstanceTemplateAnnotations;
import de.hterhors.obie.ml.variables.OBIEInstance;
import de.hterhors.obie.ml.variables.OBIEState;
import de.hterhors.semanticmr.activelearning.IActiveLearningDocumentRanker;

/**
 * Ranks the remaining instances according to the oracles objective score.
 * 
 * This ranker is just used for analyzing, can not be used in a productive
 * session.
 * 
 * @author hterhors
 *
 */
public class FullDocumentObjectiveScoreRanker implements IActiveLearningDocumentRanker {

	final private AbstractOBIERunner runner;

	public FullDocumentObjectiveScoreRanker(AbstractOBIERunner runner) {
		this.runner = runner;
	}

	@Override
	public List<OBIEInstance> rank(List<OBIEInstance> remainingInstances) {

		List<OBIEInstance> rankedInstances = new ArrayList<>();

		List<SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState>> predictions = runner
				.test(remainingInstances);

		for (SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> sampledInstance : predictions) {
			runner.scoreWithObjectiveFunction(sampledInstance.getState());
		}

		/*
		 * Smallest first.
		 */
		Collections.sort(predictions,
				new Comparator<SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState>>() {

					@Override
					public int compare(SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> o1,
							SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> o2) {
						return Double.compare(o1.getState().getObjectiveScore(), o2.getState().getObjectiveScore());
					}
				});

		for (SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> sampledInstance : predictions) {
			rankedInstances.add(sampledInstance.getInstance());
		}

		return rankedInstances;
	}

}

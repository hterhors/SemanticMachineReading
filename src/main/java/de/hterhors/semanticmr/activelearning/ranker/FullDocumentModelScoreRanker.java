package de.hterhors.semanticmr.activelearning.ranker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import corpus.SampledInstance;
import de.hterhors.obie.ml.variables.InstanceTemplateAnnotations;
import de.hterhors.obie.ml.variables.OBIEInstance;
import de.hterhors.obie.ml.variables.OBIEState;
import de.hterhors.semanticmr.activelearning.IActiveLearningDocumentRanker;

public class FullDocumentModelScoreRanker implements IActiveLearningDocumentRanker {

	final private AbstractSlotFillingPredictor runner;

	public FullDocumentModelScoreRanker(AbstractSlotFillingPredictor predictor) {
		this.runner = predictor;
	}

	@Override
	public List<OBIEInstance> rank(List<OBIEInstance> remainingInstances) {

		List<OBIEInstance> rankedInstances = new ArrayList<>();

		List<SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState>> predictions = runner
				.test(remainingInstances);

		Collections.sort(predictions,
				new Comparator<SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState>>() {

					/*
					 * Smallest first.
					 */
					@Override
					public int compare(SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> o1,
							SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> o2) {
						return Double.compare(o1.getState().getModelScore(), o2.getState().getModelScore());
					}
				});

		for (SampledInstance<OBIEInstance, InstanceTemplateAnnotations, OBIEState> sampledInstance : predictions) {
			rankedInstances.add(sampledInstance.getInstance());
		}

		return rankedInstances;
	}

}

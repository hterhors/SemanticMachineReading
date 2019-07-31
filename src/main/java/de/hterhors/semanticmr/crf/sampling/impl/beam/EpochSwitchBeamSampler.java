package de.hterhors.semanticmr.crf.sampling.impl.beam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hterhors.semanticmr.crf.StatePair;
import de.hterhors.semanticmr.crf.sampling.AbstractBeamSampler;
import de.hterhors.semanticmr.crf.sampling.AcceptStrategy;
import de.hterhors.semanticmr.crf.sampling.IBeamSamplingStrategy;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.variables.Annotations;

public class EpochSwitchBeamSampler extends AbstractBeamSampler {

	public EpochSwitchBeamSampler(IBeamSamplingStrategy samplingStrategy) {
		super(samplingStrategy);
	}

	public EpochSwitchBeamSampler() {
		super(new SwitchBeamSamplingStrategy());
	}

	private final static Comparator<StatePair> objectiveComparator = new Comparator<StatePair>() {

		@Override
		public int compare(StatePair s1, StatePair s2) {
			return -Double.compare(s1.candidateState.getObjectiveScore(), s2.candidateState.getObjectiveScore());
		}
	};
	private final static Comparator<StatePair> modelComparator = new Comparator<StatePair>() {

		@Override
		public int compare(StatePair s1, StatePair s2) {
			return -Double.compare(s1.candidateState.getModelScore(), s2.candidateState.getModelScore());
		}
	};

	@Override
	public List<StatePair> sampleCandidate(List<StatePair> candidates, int beamSize) {

		if (sampleBasedOnObjectiveScore(currentEpoch)) {
			Collections.sort(candidates, objectiveComparator);
		} else {
			Collections.sort(candidates, modelComparator);
		}

		List<StatePair> distinct = new ArrayList<>();

		Set<Annotations> annotations = new HashSet<>();

		for (StatePair statePair : candidates) {

			if (annotations.contains(statePair.candidateState.getCurrentPredictions()))
				continue;
			annotations.add(statePair.candidateState.getCurrentPredictions());

			distinct.add(statePair);

			if (distinct.size() == beamSize)
				break;

		}

		return distinct;
	}

	@Override
	public AcceptStrategy getAcceptanceStrategy(int epoch) {
		if (sampleBasedOnObjectiveScore(currentEpoch)) {
			return AcceptStrategies.strictObjectiveAccept();
		} else {
			return AcceptStrategies.strictModelAccept();
		}
	}

}

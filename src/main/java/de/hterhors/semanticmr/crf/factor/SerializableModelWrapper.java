package de.hterhors.semanticmr.crf.factor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;

public class SerializableModelWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final List<GenericTemplate> templates;

	public SerializableModelWrapper(List<AbstractFeatureTemplate<?, ?>> templates) {
		this.templates = templates.stream().map(t -> convert(t)).collect(Collectors.toList());
	}

	private GenericTemplate convert(AbstractFeatureTemplate<?, ?> t) {
		final Map<String, Double> features = new HashMap<>();
		for (Entry<Integer, Double> e : t.getWeights().getFeatures().entrySet()) {
			features.put(Model.getFeatureForIndex(e.getKey()), e.getValue());
		}
		return new GenericTemplate(features, t.getClass().getPackage().getName(), t.getClass().getSimpleName());

	}

}

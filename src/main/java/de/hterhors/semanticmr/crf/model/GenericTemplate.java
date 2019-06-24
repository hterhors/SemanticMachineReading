package de.hterhors.semanticmr.crf.model;

import java.io.Serializable;
import java.util.Map;

public class GenericTemplate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final protected Map<String, Double> features;
	final protected String packageName;
	final protected String templateName;

	public GenericTemplate(Map<String, Double> features, String packageName, String templateName) {
		this.features = features;
		this.packageName = packageName;
		this.templateName = templateName;
	}

}
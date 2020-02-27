package de.hterhors.semanticmr.crf.variables;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class basically wraps a Map of feature names (index) and values.
 * Additionally, it provides convenience functions for some of the computations
 * during the learning process.
 * 
 * 
 * TODO: Implement boolean feature vector for memory savings.
 * 
 * @author hterhors
 *
 */
public class DoubleVector {

	private static final Double DEFAULT_VALUE_ZERO = new Double(0.0);
	private static final Double DEFAULT_VALUE_ONE = new Double(1.0);

	private Map<String, Double> features;
//	private Map<Integer, Double> features;

	private double length = 0D;
	private boolean isDirty = true;

//
	public DoubleVector() {
		this.features = new HashMap<String, Double>();
//		this.features = new HashMap<Integer, Double>();
		this.isDirty = true;
		this.length = 0;

	}

	private void setForceZero(String index, Double value) {
//		index = index.intern();
		if (value.doubleValue() != 0.0) {
			features.put(index, value);
		} else {
			features.remove(index);
		}
		this.isDirty = true;
	}

	public void set(String feature, Double value) {
		if (value.doubleValue() == 0.0D)
			return;
		setForceZero(feature, value);
	}

	public void set(String feature, boolean flag) {
		set(feature, flag ? DEFAULT_VALUE_ONE : DEFAULT_VALUE_ZERO);
	}

	/**
	 * Returns the value of this feature. If this vector does not contain this
	 * feature a default value is stored and returned for this feature.
	 * 
	 * @param feature
	 * @return
	 */
	public Double getValueOfFeature(String feature) {
		return features.getOrDefault(feature, DEFAULT_VALUE_ZERO);
	}

	public Map<String, Double> getFeatures() {
		return features;
	}

	private void addToValue(String feature, double alpha) {
		Double featureValue = getValueOfFeature(feature);
		setForceZero(feature, new Double(featureValue.doubleValue() + alpha));
	}

	/*
	 * *********************
	 * 
	 * VECTOR ARITHMETICS
	 * 
	 * *********************
	 */
	public double dotProduct(DoubleVector weights) {
		DoubleVector smaller = null;
		DoubleVector bigger = null;
		if (features.size() <= weights.features.size()) {
			smaller = this;
			bigger = weights;
		} else {
			smaller = weights;
			bigger = this;
		}
		if (smaller.getFeatures().size() != 0)
			return computeDot(smaller, bigger);
		else
			return 0;
	}

	private double computeDot(DoubleVector smaller, DoubleVector bigger) {
		double result = 0;
		for (Entry<String, Double> e : smaller.getFeatures().entrySet()) {
			result += e.getValue() * bigger.getValueOfFeature(e.getKey());
		}
		return result;
	}

	public void mul(double f) {
		for (Entry<String, Double> feature : features.entrySet()) {
			setForceZero(feature.getKey(), feature.getValue() * f);
		}
		this.isDirty = true;

	}

	public void add(DoubleVector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue());
		}
		this.isDirty = true;
	}

	public void mulAndAdd(DoubleVector v, double mul) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), feature.getValue() * mul);
		}
		this.isDirty = true;
	}

	public void sub(DoubleVector v) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -feature.getValue());
		}
		this.isDirty = true;
	}

	public void mulAndSub(DoubleVector v, double mul) {
		for (Entry<String, Double> feature : v.getFeatures().entrySet()) {
			addToValue(feature.getKey(), -(feature.getValue() * mul));
		}
		this.isDirty = true;
	}

//	public void normalize() {
//		double length = length();
//		if (length > 0) {
//			for (Entry<String, Double> feature : features.entrySet()) {
//				setForceZero(feature.getKey(), feature.getValue() / length);
//			}
//		}
//	}

	public double length() {

		if (!isDirty)
			return length;

		double length = 0;
		for (Entry<String, Double> feature : features.entrySet()) {
			length += Math.pow(feature.getValue(), 2);
		}
		length = Math.sqrt(length);
		this.length = length;
		this.isDirty = false;
		return length;
	}

	@Override
	public String toString() {
		return features.toString();
	}
}

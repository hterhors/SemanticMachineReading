package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hterhors.semanticmr.crf.model.Model;

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
public class DoubleVectorARRAY {

	private static final double DEFAULT_VALUE_ZERO = 0.0;
	private static final double DEFAULT_VALUE_ONE = 1.0;

//	private Map<String, Double> features;
//	private Map<Integer, Double> features;
	private double[] features;

	private double length = 0D;
	private boolean isDirty = true;

//
	public DoubleVectorARRAY() {
		this.features = new double[10];
//		this.features = new HashMap<String, Double>();
//		this.features = new HashMap<Integer, Double>();
		this.isDirty = true;
		this.length = 0;

	}

	private void set(Integer index, double value) {
		features[index] = value;
		this.isDirty = true;
	}

	public void set(String feature, double value) {
		if (value == 0.0D)
			return;
		final Integer index = Model.getIndexForFeatureName(feature);
		if (index >= features.length) {
			final int newSize = Math.max(index + 1, (int) (features.length * 1.4));
			double[] newArray = new double[newSize];
			System.arraycopy(features, 0, newArray, 0, features.length);
			this.features = newArray;
		}
		set(index, value);
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
	private double getValueOfFeature(Integer index) {
		return features[index];
	}

	public double[] getFeatures() {
		return features;
	}

	private void addToValue(Integer feature, double alpha) {
		double featureValue = getValueOfFeature(feature);
		set(feature, featureValue + alpha);
	}

	/*
	 * *********************
	 * 
	 * VECTOR ARITHMETICS
	 * 
	 * *********************
	 */
	public double dotProduct(DoubleVectorARRAY weights) {
		DoubleVectorARRAY smaller = null;
		DoubleVectorARRAY bigger = null;
		if (features.length <= weights.features.length) {
			smaller = this;
			bigger = weights;
		} else {
			smaller = weights;
			bigger = this;
		}
		if (smaller.getFeatures().length != 0)
			return computeDot(smaller, bigger);
		else
			return 0;
	}

	private double computeDot(DoubleVectorARRAY smaller, DoubleVectorARRAY bigger) {
		double result = 0;
		double[] smallerVec = smaller.getFeatures();
		double[] biggerVec = bigger.getFeatures();
		for (int i = 0; i < smallerVec.length; i++) {
			result += smallerVec[i] * biggerVec[i];
		}
		return result;
	}

	public void mul(double f) {
		for (int i = 0; i < features.length; i++) {
			set(i, features[i] * f);
		}
		this.isDirty = true;
	}

	public void add(DoubleVectorARRAY v) {
		for (int i = 0; i < v.getFeatures().length; i++) {
			addToValue(i, features[i]);
		}
		this.isDirty = true;
	}

	public void mulAndAdd(DoubleVectorARRAY v, double mul) {
		for (int i = 0; i < v.getFeatures().length; i++) {
			addToValue(i, v.getFeatures()[i] * mul);
		}
		this.isDirty = true;
	}

	public void sub(DoubleVectorARRAY v) {
		for (int i = 0; i < v.getFeatures().length; i++) {
			addToValue(i, -v.getFeatures()[i]);
		}
		this.isDirty = true;
	}

	public void mulAndSub(DoubleVectorARRAY v, double mul) {
		for (int i = 0; i < v.getFeatures().length; i++) {
			addToValue(i, -(v.getFeatures()[i] * mul));
		}
		this.isDirty = true;
	}

	public void normalize() {
		double length = length();
		if (length > 0) {
			for (int i = 0; i < features.length; i++) {
				set(i, features[i] / length);
			}
		}
	}

	public double length() {

		if (!isDirty)
			return length;

		double length = 0;
		for (int i = 0; i < features.length; i++) {
			length += Math.pow(features[i], 2);
		}
		length = Math.sqrt(length);
		this.length = length;
		this.isDirty = false;
		return length;
	}

	@Override
	public String toString() {
		return Arrays.toString(features);
	}
}

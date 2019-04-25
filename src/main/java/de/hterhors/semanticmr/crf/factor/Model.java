package de.hterhors.semanticmr.crf.factor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.ModelLoadException;

public class Model {

	final private static FactorPool FACTOR_POOL_INSTANCE = FactorPool.getInstance();

	/**
	 * Converts a feature name to its index.
	 */
	private final static Map<String, Integer> featureNameIndex = new ConcurrentHashMap<>();

	private boolean wasLoaded = false;
	/**
	 * Converts an index to its feature name.
	 */
	private final static Map<Integer, String> indexFeatureName = new ConcurrentHashMap<>();

	private static final String DEFAULT_READABLE_DIR = "/readable/";

	private static final String MODEL_SUFFIX = ".crf";

	public static Integer getIndexForFeatureName(String feature) {
		Integer index;

		if ((index = featureNameIndex.get(feature)) != null) {
			return index;
		}

		index = new Integer(featureNameIndex.size());
		featureNameIndex.put(feature, index);
		indexFeatureName.put(index, feature);

		return index;
	}

	public static String getFeatureForIndex(Integer feature) {
		return indexFeatureName.get(feature);
	}

	final private List<AbstractFeatureTemplate<?, ?>> factorTemplates;

	public Model(List<AbstractFeatureTemplate<?, ?>> factorTemplates) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
	}

	public void score(State state) {

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate<?, ?> template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			collectFactorScopesForState(template, state);

			/*
			 * Compute all selected factors in parallel.
			 */
			computeRemainingFactors(template,
					state.getFactorGraphs().stream().flatMap(l -> l.getFactorScopes().stream()));
		}

		/*
		 * Compute and set model score
		 */
		computeAndSetModelScore(state);

	}

	public void score(List<State> states) {

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate<?, ?> template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			states.parallelStream().forEach(state -> collectFactorScopesForState(template, state));

			/*
			 * Compute all selected factors in parallel.
			 */
			computeRemainingFactors(template, states.stream().flatMap(state -> state.getFactorGraphs().stream())
					.flatMap(l -> l.getFactorScopes().stream()));
		}
		/*
		 * Compute and set model score
		 */
		states.parallelStream().forEach(state -> computeAndSetModelScore(state));

	}

	private void computeAndSetModelScore(State state) {
		state.setModelScore(computeScore(state));
	}

	@SuppressWarnings("unchecked")
	private void computeRemainingFactors(AbstractFeatureTemplate<?, ?> template,
			@SuppressWarnings("rawtypes") Stream<AbstractFactorScope> stream) {

		Stream<Factor<?>> s = stream.parallel().filter(fs -> !FACTOR_POOL_INSTANCE.containsFactorScope(fs))
				.map(remainingFactorScope -> {
					@SuppressWarnings({ "rawtypes" })
					Factor f = new Factor(remainingFactorScope);
					template.generateFeatureVector(f);
					return f;
				});

		s.sequential().forEach(factor -> FACTOR_POOL_INSTANCE.addFactor(factor));
	}

	private void collectFactorScopesForState(AbstractFeatureTemplate<?, ?> template, State state) {
		state.getFactorGraph(template).addFactorScopes(template.generateFactorScopes(state));
	}

	/**
	 * Computes the score of this state according to the trained model. The computed
	 * score is returned but also updated in the state objects <i>score</i> field.
	 * 
	 * @param list
	 * @return
	 */
	private double computeScore(State state) {

		double score = 1;
		boolean factorsAvailable = false;
		for (FactorGraph abstractFactorTemplate : state.getFactorGraphs()) {

			final List<Factor<?>> factors = abstractFactorTemplate.getFactors();

			factorsAvailable |= factors.size() != 0;

			for (Factor<?> factor : factors) {
				score *= factor.computeScalarScore();
			}

		}
		if (factorsAvailable)
			return score;

		return 0;

	}

	public void updateWeights(final AdvancedLearner learner, final State currentState, final State candidateState) {
		learner.updateWeights(this.factorTemplates, currentState, candidateState);
	}

	@Override
	public String toString() {
		factorTemplates.get(0).getWeights().getFeatures().entrySet()
				.forEach(f -> System.out.println(indexFeatureName.get(f.getKey()) + ":" + f.getValue()));
		return "";
	}

	public void print(File modelDir, String modelName) throws IOException {
		for (AbstractFeatureTemplate<?, ?> template : this.factorTemplates) {
			File parentDir = new File(modelDir, modelName + "/" + DEFAULT_READABLE_DIR);
			parentDir.mkdirs();

			PrintStream ps = new PrintStream(new File(parentDir, template.getClass().getSimpleName()));

			List<Entry<Integer, Double>> sortedWeights = new ArrayList<>(
					template.getWeights().getFeatures().entrySet());
			Collections.sort(sortedWeights, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
			for (Entry<Integer, Double> feature : sortedWeights) {
				ps.println(indexFeatureName.get(feature.getKey()) + "\t" + feature.getValue());
			}
			ps.close();

		}

	}

	public void save(final File modelDir, final String modelName, boolean printAsReadable) throws IOException {

		if (!modelDir.exists())
			modelDir.mkdirs();

		if (!modelDir.isDirectory())
			throw new IllegalArgumentException("Model directory s not a directory: " + modelDir);

		final File modelFile;

		if ((modelFile = new File(modelDir, modelName + MODEL_SUFFIX)).exists())
			System.out.println("Warn: model already exists override model!");

		if (printAsReadable)
			print(modelDir, modelName);
		// Serialization
		try {
			// Saving of object in a file
			FileOutputStream file = new FileOutputStream(modelFile);
			ObjectOutputStream out = new ObjectOutputStream(file);

			// Method for serialization of object
			out.writeObject(new SerializableModelWrapper(this.factorTemplates));

			out.close();
			file.close();

			System.out.println("Object has been serialized");

		}

		catch (IOException ex) {
			ex.printStackTrace();

		}

	}

	public static Model load(final File modelDir, final String modelName) throws IOException, ClassNotFoundException {

		SerializableModelWrapper modelWrapper = null;

		FileInputStream file = new FileInputStream(new File(modelDir, modelName + MODEL_SUFFIX));
		ObjectInputStream in = new ObjectInputStream(file);

		modelWrapper = (SerializableModelWrapper) in.readObject();

		in.close();
		file.close();
		System.out.println("Object has been deserialized ");
		return toModel(modelWrapper);

	}

	private static Model toModel(SerializableModelWrapper modelWrapper) {
		Model model = new Model(
				modelWrapper.templates.stream().map(t -> toAbstractFeaturetemplate(t)).collect(Collectors.toList()));
		model.wasLoaded = true;
		return model;
	}

	private static AbstractFeatureTemplate<?, ?> toAbstractFeaturetemplate(final GenericTemplate t) {
		try {
			final AbstractFeatureTemplate<?, ?> template = (AbstractFeatureTemplate<?, ?>) Class
					.forName(t.packageName + "." + t.templateName).newInstance();

			final DoubleVector v = new DoubleVector();

			for (Entry<String, Double> fw : t.features.entrySet()) {
				v.set(fw.getKey(), fw.getValue());
			}

			template.setWeights(v);

			return template;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new ModelLoadException(e.getMessage());
		}
	}

	public boolean wasLoaded() {
		return wasLoaded;
	}

}

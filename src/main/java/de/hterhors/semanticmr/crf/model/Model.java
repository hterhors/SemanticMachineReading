package de.hterhors.semanticmr.crf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.ModelLoadException;
import de.hterhors.semanticmr.exce.ModelSaveException;

public class Model {
	private static Logger log = LogManager.getFormatterLogger(Model.class);

	public static boolean alwaysTrainModel = false;

//	private FactorPoolCache factorCache = new FactorPoolCache(this, 800_000, 400_000);
	private FactorPoolCache factorCache = new FactorPoolCache(this, 6400, 3200);
//	final public FactorPool factorCache = new FactorPool();

	public FactorPoolCache getFactorCache() {
		return factorCache;
	}

	/**
	 * Converts a feature name to its index.
	 */
	private final static Map<String, Integer> featureNameIndex = new HashMap<>();

	private boolean isTrained = false;
	/**
	 * Converts an index to its feature name.
	 */
	public final static Map<Integer, String> indexFeatureName = new HashMap<>();

	private static final String DEFAULT_READABLE_DIR = "/readable/";

	private static final String MODEL_SUFFIX = ".smcrf";

	private final File modelBaseDir;
	private final String modelName;

	public void setCache(FactorPoolCache factorCache) {
		this.factorCache = factorCache;
	}

	/**
	 * Synchronized method
	 * 
	 * @param feature
	 * @return
	 */
	public static synchronized Integer getIndexForFeatureName(String feature) {
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

	/**
	 * A comparator implementation that allows to sort states in descending order
	 * with respect to their objective score.
	 */
	public static final Comparator<State> objectiveScoreComparator = new Comparator<State>() {

		@Override
		public int compare(State s1, State s2) {
			return -Double.compare(s1.getObjectiveScore(), s2.getObjectiveScore());
		}
	};

	/**
	 * A comparator implementation that allows to sort states in descending order
	 * with respect to their model score.
	 */
	public static final Comparator<State> modelScoreComparator = new Comparator<State>() {

		@Override
		public int compare(State s1, State s2) {
			return -Double.compare(s1.getModelScore(), s2.getModelScore());
		}
	};

	final private List<AbstractFeatureTemplate> factorTemplates;

	public Model(List<AbstractFeatureTemplate<?>> factorTemplates) {
		this(factorTemplates, null, null);
	}

	public Model(List<AbstractFeatureTemplate<?>> factorTemplates, File modelDir, String modelName) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
		this.modelBaseDir = modelDir;
		this.modelName = modelName;
	}

	public void score(State state) {
		score(Arrays.asList(state));
	}

	public void score(List<State> states) {

		/*
		 * Collect all factor scopes of all states to that this template can be applied
		 */
		states.parallelStream().forEach(state -> {
			for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

				FactorGraph factorGraph = state.getFactorGraph(template);

				if (factorGraph == null) {
					factorGraph = new FactorGraph(factorCache, template);
				}
				state.setFactorGraph(template, factorGraph);

				factorGraph.addFactorScopes((List<AbstractFactorScope>) template.generateFactorScopes(state));
			}

		});

		/*
		 * Compute all selected factors in parallel.
		 */
		computeRemainingFactors(states);
		/*
		 * Compute and set model score
		 */
		states.parallelStream().forEach(state -> state.setModelScore(computeScore(state)));
	}

	/**
	 * Computes the score of this state according to the trained model. The computed
	 * score is returned but also updated in the state objects <i>score</i> field.
	 * 
	 * @param preComputedFactors
	 * 
	 * @param list
	 * @return
	 */
	private double computeScore(State state) {

		double score = 1;
		boolean factorsAvailable = false;
		for (FactorGraph factorGraph : state.getFactorGraphs()) {

			final List<Factor> factors = factorGraph.getCachedFactors();

			factorsAvailable |= factors.size() != 0;

			for (Factor factor : factors) {
				score *= factor.computeScalarScore();
			}

		}
		if (factorsAvailable)
			return score;

		return 0;

	}

	Map<Class<?>, Map<Boolean, Integer>> cacheCount = new ConcurrentHashMap<>();

	private void computeRemainingFactors(List<State> states) {

		List<List<Factor>> factorsToCache = Collections.synchronizedList(new ArrayList<>());

		states.parallelStream().forEach(state -> {

			List<Factor> factors = new ArrayList<>();
			for (FactorGraph factorGraph : state.getFactorGraphs()) {

				List<Factor> computedFactors = new ArrayList<>();
				for (AbstractFactorScope scope : factorGraph.getFactorScopes()) {

					Factor factor;

					cacheCount.putIfAbsent(scope.getClass(), new HashMap<>());
					if ((factor = factorCache.getIfPresent(scope)) == null) {
						factor = new Factor(scope);
						factor.getFactorScope().template.generateFeatureVector(factor);
						cacheCount.get(scope.getClass()).put(false,
								cacheCount.get(scope.getClass()).getOrDefault(false, 0) + 1);
					} else {
						cacheCount.get(scope.getClass()).put(true,
								cacheCount.get(scope.getClass()).getOrDefault(true, 0) + 1);
					}

					computedFactors.add(factor);
					factors.add(factor);
				}

				factorsToCache.add(computedFactors);

				factorGraph.cacheFactors(computedFactors);

			}
		});

		/*
		 * This might removes cache values form the cache due to fix cache size. But
		 * factors for factor graphs are already stored in the factor graph.
		 */
		for (List<Factor> list : factorsToCache) {
			for (Factor<?> factor : list) {
				factorCache.addFactor(factor);
			}
		}

	}

	public void updateWeights(final AdvancedLearner learner, final State currentState, final State candidateState) {
		learner.updateWeights(this.factorTemplates, currentState, candidateState);
	}

	public void printReadable() {
		printReadable(this.modelBaseDir, this.modelName);
	}

	static class Pair {
		final public Integer index;
		final public double value;

		public Pair(Integer index, double value) {
			super();
			this.index = index;
			this.value = value;
		}

	}

	public void printReadable(File modelDir, String modelName) {
		log.info("Print model in readable format...");
		try {
			for (AbstractFeatureTemplate template : this.factorTemplates) {
				File parentDir = new File(modelDir, modelName + DEFAULT_READABLE_DIR);
				parentDir.mkdirs();

				final File f = new File(parentDir, template.getClass().getSimpleName());

				PrintStream ps = new PrintStream(f);
				log.info("Print template to " + f.getAbsolutePath());
				List<Entry<String, Double>> sortedWeights = new ArrayList<>(
						template.getWeights().getFeatures().entrySet());
				if (sortedWeights.size() == 0)

					log.warn("No features found for template: " + template.getClass().getSimpleName());
				Collections.sort(sortedWeights, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
				for (Entry<String, Double> feature : sortedWeights) {
					ps.println(feature.getKey() + "\t" + feature.getValue());
//					ps.println(indexFeatureName.get(feature.getKey()) + "\t" + feature.getValue());
				}
				ps.close();

			}
		} catch (IOException ex) {
			throw new RuntimeException("The model could not be printed. Failed with error: " + ex.getMessage());
		}
	}

	public void save() {
		save(false);
	}

	public void save(boolean overrideOnExistence) {
		save(this.modelBaseDir, this.modelName, overrideOnExistence);
	}

	public void save(final File modelBaseDir, final String modelName, boolean overrideOnExistence) {
		log.info("Save model binaries to filesystem...");

		try {

			final File modelDir = getModelDir(modelBaseDir, modelName);

			if (!modelDir.exists())
				modelDir.mkdirs();

			if (!modelBaseDir.isDirectory())
				throw new IllegalArgumentException("Model base directory is not a directory: " + modelBaseDir);

			final boolean modelExists = exists(modelBaseDir, modelName);

			if (!overrideOnExistence && modelExists) {
				log.warn("Model already exists but can not override model! MODEL NOT SAVED!");
				return;
			} else if (overrideOnExistence && modelExists) {
				log.warn("Model already exists override model!");
			}

			File modelFile = getAbsoluteModelFile(modelBaseDir, modelName);

			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile));

			out.writeObject(new SerializableModelWrapper(this.factorTemplates));

			out.close();

			log.info("Model saved under " + modelFile.getAbsolutePath());

		}

		catch (IOException ex) {
			throw new ModelSaveException("The model could not be saved. Failed with error: " + ex.getMessage());
		}

	}

	public static Model load(final File modelBaseDir, final String modelName) {

		log.info("Load model binaries from filesystem...");

		try {
			SerializableModelWrapper modelWrapper = null;

			final File modelFile = getAbsoluteModelFile(modelBaseDir, modelName);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile));

			modelWrapper = (SerializableModelWrapper) in.readObject();
			in.close();

			final Model m = toModel(modelWrapper, modelBaseDir, modelName);
			log.info("Model successfully loaded from: " + modelFile);
			return m;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ModelLoadException("The model could not be loaded. Failed with error: " + e.getMessage());
		}

	}

	private static Model toModel(SerializableModelWrapper modelWrapper, File modelBaseDir, String modelName) {

		Model model = new Model(
				modelWrapper.templates.stream().map(t -> toAbstractFeatureTemplate(t)).collect(Collectors.toList()));
		model.isTrained = true;

		return model;
	}

	public void setfeatureTemplateParameter(Map<Class<? extends AbstractFeatureTemplate<?>>, Object[]> parameter) {

		for (AbstractFeatureTemplate<?> featureTemplate : this.factorTemplates) {
			if (parameter.containsKey(featureTemplate.getClass())) {
				featureTemplate.initalize(parameter.get(featureTemplate.getClass()));
			}
		}
	}

	private static AbstractFeatureTemplate<?> toAbstractFeatureTemplate(final GenericTemplate t) {
		try {
			final AbstractFeatureTemplate<?> template = (AbstractFeatureTemplate<?>) Class
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

	public boolean isTrained() {
		return isTrained;
	}

	private static File getModelDir(File modelBaseDir, String modelName) {
		return new File(modelBaseDir, modelName);
	}

	public static boolean exists(File modelBaseDir, String modelName) {
		return getAbsoluteModelFile(modelBaseDir, modelName).exists();
	}

	public static File getAbsoluteModelFile(File modelBaseDir, String modelName) {
		return new File(getModelDir(modelBaseDir, modelName), modelName + MODEL_SUFFIX);
	}

	public String getName() {
		return modelName;
	}

	public List<AbstractFeatureTemplate> getFactorTemplates() {
		return factorTemplates;
	}

}

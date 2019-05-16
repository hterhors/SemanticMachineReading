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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.ModelLoadException;
import de.hterhors.semanticmr.exce.ModelSaveException;

public class Model {
	private static Logger log = LogManager.getFormatterLogger(Model.class);

	public static boolean alwaysTrainModel = false;

	final private static FactorPool FACTOR_POOL_INSTANCE = FactorPool.getInstance();

	/**
	 * Converts a feature name to its index.
	 */
	private final static Map<String, Integer> featureNameIndex = new ConcurrentHashMap<>();

	private boolean isTrained = false;
	/**
	 * Converts an index to its feature name.
	 */
	private final static Map<Integer, String> indexFeatureName = new ConcurrentHashMap<>();

	private static final String DEFAULT_READABLE_DIR = "/readable/";

	private static final String MODEL_SUFFIX = ".smcrf";

	private File modelBaseDir;
	private String modelName;

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

	final private List<AbstractFeatureTemplate<?>> factorTemplates;

	public Model(List<AbstractFeatureTemplate<?>> factorTemplates) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
	}

	public Model(List<AbstractFeatureTemplate<?>> factorTemplates, File modelDir, String modelName) {
		this.factorTemplates = Collections.unmodifiableList(factorTemplates);
		this.modelBaseDir = modelDir;
		this.modelName = modelName;
	}

	public void score(State state) {

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

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
		for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

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
	private void computeRemainingFactors(AbstractFeatureTemplate<?> template,
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

	private void collectFactorScopesForState(AbstractFeatureTemplate<?> template, State state) {
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

	public void printReadable() {
		printReadable(this.modelBaseDir, this.modelName);
	}

	public void printReadable(File modelDir, String modelName) {
		log.info("Print model in readable format...");
		try {
			for (AbstractFeatureTemplate<?> template : this.factorTemplates) {
				File parentDir = new File(modelDir, modelName + DEFAULT_READABLE_DIR);
				parentDir.mkdirs();

				final File f = new File(parentDir, template.getClass().getSimpleName());

				PrintStream ps = new PrintStream(f);
				log.info("Print template to " + f.getAbsolutePath());
				List<Entry<Integer, Double>> sortedWeights = new ArrayList<>(
						template.getWeights().getFeatures().entrySet());

				if (sortedWeights.size() == 0)
					log.warn("No features found for template: " + template.getClass().getSimpleName());
				Collections.sort(sortedWeights, (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()));
				for (Entry<Integer, Double> feature : sortedWeights) {
					ps.println(indexFeatureName.get(feature.getKey()) + "\t" + feature.getValue());
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
			final Model m = toModel(modelWrapper);
			log.info("Model successfully loaded from: " + modelFile);
			return m;
		} catch (Exception e) {
			throw new ModelLoadException("The model could not be loaded. Failed with error: " + e.getMessage());

		}

	}

	private static Model toModel(SerializableModelWrapper modelWrapper) {
		Model model = new Model(
				modelWrapper.templates.stream().map(t -> toAbstractFeaturetemplate(t)).collect(Collectors.toList()));
		model.isTrained = true;
		return model;
	}

	private static AbstractFeatureTemplate<?> toAbstractFeaturetemplate(final GenericTemplate t) {
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

	public void changeModelBaseDir(final File newBaseDir) {
		this.modelBaseDir = newBaseDir;
	}

	public void changeModelName(final String newName) {
		this.modelName = newName;
	}

	public String getName() {
		return modelName;
	}

	public List<AbstractFeatureTemplate<?>> getFactorTemplates() {
		return Collections.unmodifiableList(factorTemplates);
	}

}

package de.hterhors.semanticmr.crf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DoubleVector;
import de.hterhors.semanticmr.crf.variables.DoubleVectorARRAY;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.exce.ModelLoadException;
import de.hterhors.semanticmr.exce.ModelSaveException;

public class Model {
	private static Logger log = LogManager.getFormatterLogger(Model.class);

	public static boolean alwaysTrainModel = false;

	final private FactorPool factorPool = new FactorPool();

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

	private static void COMPARE_VECTORS() {
		DoubleVector av1[] = new DoubleVector[300];
		DoubleVector av2[] = new DoubleVector[av1.length];
		DoubleVectorARRAY bv1[] = new DoubleVectorARRAY[av1.length];
		DoubleVectorARRAY bv2[] = new DoubleVectorARRAY[av1.length];

		for (int j = 0; j < av1.length; j++) {
			DoubleVector avec = new DoubleVector();
			DoubleVector avec2 = new DoubleVector();
			DoubleVectorARRAY bvec = new DoubleVectorARRAY();
			DoubleVectorARRAY bvec2 = new DoubleVectorARRAY();
			for (int i = 0; i < 200000; i++) {
				String r = i + "feature";
				float a = (float) Math.random();
				float b = (float) Math.random();
				float c = (float) Math.random();
				float d = (float) Math.random();
				avec.set(r, a < 0.5 ? b : 0D);
				avec2.set(r, c < 0.5 ? d : 0D);
				bvec.set(r, a < 0.5 ? b : 0F);
				bvec2.set(r, c < 0.5 ? d : 0F);
			}
			av1[j] = avec;
			av2[j] = avec2;
			bv1[j] = bvec;
			bv2[j] = bvec2;
			System.out.println(j);
		}
		long t = System.nanoTime();
		System.out.println("done");
		double x = 0;
		for (int i = 0; i < av1.length; i++) {
			x += av1[i].dotProduct(av2[i]);
		}
		System.out.println(x);
		System.out.println(System.nanoTime() - t);

		t = System.nanoTime();
		System.out.println("done");
		x = 0;
		for (int i = 0; i < bv1.length; i++) {
			x += bv1[i].dotProduct(bv2[i]);
		}
		System.out.println(x);
		System.out.println(System.nanoTime() - t);
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

	public static synchronized String getFeatureForIndex(Integer feature) {
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

		/**
		 * TODO: measure efficiency of streams
		 */
		for (AbstractFeatureTemplate template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			collectFactorScopesForState(template, state);
//
//			System.out.println(template.getClass().getSimpleName());
//			state.getFactorGraph(template).getFactorScopes().forEach(System.out::println);
//
//			System.out.println("------------------------------------------------------");
		}
		/*
		 * Compute all selected factors in parallel.
		 */
		computeRemainingFactors(state.getFactorGraphs().stream().flatMap(l -> l.getFactorScopes().stream()));

		/*
		 * Compute and set model score
		 */
		state.setModelScore(computeScore(state));
	}

	public void score(List<State> states) {

		for (AbstractFeatureTemplate<?> template : this.factorTemplates) {

			/*
			 * Collect all factor scopes of all states to that this template can be applied
			 */

			states.parallelStream().forEach(state -> collectFactorScopesForState(template, state));

		}
		/*
		 * Compute all selected factors in parallel.
		 */
		computeRemainingFactors(states.stream().flatMap(state -> state.getFactorGraphs().stream())
				.flatMap(fg -> fg.getFactorScopes().stream()));
		/*
		 * Compute and set model score
		 */
		states.parallelStream().forEach(state -> state.setModelScore(computeScore(state)));

	}

	private void computeRemainingFactors(Stream<AbstractFactorScope> stream) {

		List<Factor> factors = new ArrayList<>();

		for (AbstractFactorScope scope : stream.distinct()
				.filter(fs -> !fs.template.enableFactorCaching
						|| (fs.template.enableFactorCaching && !factorPool.containsFactorScope(fs)))
				.collect(Collectors.toList())) {
			factors.add(new Factor(scope));
		}

		factors.parallelStream().forEach(factor -> {
			factor.getFactorScope().template.generateFeatureVector(factor);
		});
		
		
		for (Factor<?> factor : factors) {
			if (!factor.getFactorScope().template.enableFactorCaching)
				continue;
			factorPool.addFactor(factor);
		}
	}

	private void collectFactorScopesForState(AbstractFeatureTemplate template, State state) {

		FactorGraph factorGraph = state.getFactorGraph(template);

		if (factorGraph == null) {
			factorGraph = new FactorGraph(factorPool, template);
		}
		state.addIfAbsentFactorGraph(template, factorGraph);

		factorGraph.addFactorScopes(template.generateFactorScopes(state));
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
		for (FactorGraph factorGraph : state.getFactorGraphs()) {

			final List<Factor> factors = factorGraph.getFactors();

			factorsAvailable |= factors.size() != 0;

			for (Factor factor : factors) {
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

//	public void printReadable(File modelDir, String modelName) {
//		log.info("Print model in readable format...");
//		try {
//			for (AbstractFeatureTemplate template : this.factorTemplates) {
//				File parentDir = new File(modelDir, modelName + DEFAULT_READABLE_DIR);
//				parentDir.mkdirs();
//
//				final File f = new File(parentDir, template.getClass().getSimpleName());
//
//				PrintStream ps = new PrintStream(f);
//				log.info("Print template to " + f.getAbsolutePath());
//
//				List<Pair> sortedWeights = new ArrayList<>();
//				for (int i = 0; i < template.getWeights().getFeatures().length; i++) {
//					sortedWeights.add(new Pair(i, template.getWeights().getFeatures()[i]));
//				}
//
//				if (sortedWeights.size() == 0)
//
//					log.warn("No features found for template: " + template.getClass().getSimpleName());
//				Collections.sort(sortedWeights, (o1, o2) -> -Double.compare(o1.value, o2.value));
//				for (Pair feature : sortedWeights) {
//					ps.println(indexFeatureName.get(feature.index) + "\t" + feature.value);
//				}
//				ps.close();
//
//			}
//		} catch (IOException ex) {
//			throw new RuntimeException("The model could not be printed. Failed with error: " + ex.getMessage());
//		}
//	}
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

	public void setParameter(Map<Class<? extends AbstractFeatureTemplate<?>>, Object[]> parameter) {

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

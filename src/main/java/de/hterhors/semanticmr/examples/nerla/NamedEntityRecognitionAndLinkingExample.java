package de.hterhors.semanticmr.examples.nerla;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.candprov.nerla.InMEMDictionaryBasedCandidateProvider;
import de.hterhors.semanticmr.candprov.nerla.NerlaCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.CRF;
import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
import de.hterhors.semanticmr.crf.factor.Model;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.NerlaObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.stopcrit.IStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.NoChangeCrit;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.IntraTokenNerlaTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.MorphologicalNerlaTemplate;
import de.hterhors.semanticmr.crf.templates.nerla.NerlaTokenContextTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.EvaluationResultPrinter;
import de.hterhors.semanticmr.examples.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;

/**
 * Example of how to perform named entity recognition and linking.
 * 
 * @author hterhors
 *
 */
public class NamedEntityRecognitionAndLinkingExample {

	/**
	 * Start the named entity recognition and linking procedure.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		new NamedEntityRecognitionAndLinkingExample();
	}

	/**
	 * The file that contains specifications about the entities. This file is the
	 * only specification file which is necessary for NERLA as it contains basically
	 * a list of entities that need to be found.
	 */
	private final File entities = new File("src/main/resources/examples/nerla/specs/csv/entities.csv");

	/**
	 * Specification file that contains information about slots. This file is
	 * internally not used for NERLA as it is not necessary. However, additional
	 * information can be exploited during feature generation.
	 **/
	private final File slots = new File("src/main/resources/examples/nerla/specs/csv/slots.csv");

	/**
	 * Specification file that contains information about slots of entities. This
	 * file is internally not used for NERLA as it is not necessary. However,
	 * additional information can be exploited during feature generation.
	 **/
	private final File structures = new File("src/main/resources/examples/nerla/specs/csv/structures.csv");

	/**
	 * Specification file of entity hierarchies. This is not necessary for NERLA but
	 * might be helpful for feature generation.
	 */
	private final File hierarchies = new File("src/main/resources/examples/nerla/specs/csv/hierarchies.csv");

	/**
	 * A dictionary file that is used for the in-memory dictionary based candidate
	 * retrieval component. It is basically a list of terms and synonyms for
	 * specific entities.
	 * 
	 * In a real world scenario dictionary lookups for candidate retrieval is mostly
	 * not sufficient! Consider implementing your own candidate retrieval e.g. fuzzy
	 * lookup, Lucene-based etc...
	 */
	private final File dictionaryFile = new File("src/main/resources/examples/nerla/dicts/organismModel.dict");

	/**
	 * The directory of the corpus instances. In this example each instance is
	 * stored in its own json-file.
	 */
	private final File instanceDirectory = new File("src/main/resources/examples/nerla/corpus/instances/");

	public NamedEntityRecognitionAndLinkingExample() {

		/**
		 * The systems scope. The scope represents the specifications of the 4 (in this
		 * case only the entities-file is important) previously defined specification
		 * files. The scope mainly defines the exploration.
		 */
		SystemScope.Builder.getSpecsHandler()
				.addScopeSpecification(new CSVScopeReader(entities, hierarchies, slots, structures)).apply()
				.registerNormalizationFunction(new WeightNormalization()).apply().build();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(instanceDirectory, shuffleCorpusDistributor);

		NerlaCandidateProviderCollection candidateProvider = new NerlaCandidateProviderCollection(
				new InMEMDictionaryBasedCandidateProvider(dictionaryFile));

		IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(EEvaluationDetail.DOCUMENT_LINKED);

		AdvancedLearner learner = new AdvancedLearner(new SGD(0.01, 0), new L2(0.0001));

		List<AbstractFeatureTemplate<?, ?>> featureTemplates = new ArrayList<>();

		featureTemplates.add(new IntraTokenNerlaTemplate());
		featureTemplates.add(new NerlaTokenContextTemplate());
		featureTemplates.add(new MorphologicalNerlaTemplate());

		IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations()));

		int numberOfEpochs = 4;

//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
		AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

		IStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);
		IStoppingCriterion noModelChangeCrit = new NoChangeCrit(3, s -> s.getModelScore());

		EntityRecLinkExplorer explorer = new EntityRecLinkExplorer(candidateProvider);

		final File modelDir = new File("models/nerla/test1/");
		final String modelName = "ModelName34";

		Model model;
		try {
			model = Model.load(modelDir, modelName);
		} catch (Exception e) {
		}
		model = new Model(featureTemplates);

		CRF crf = new CRF(model, explorer, sampler, stateInitializer, objectiveFunction);

		if (!model.wasLoaded()) {
			crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, maxStepCrit,
					noModelChangeCrit);

			try {
				model.save(modelDir, modelName, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Map<Instance, State> testResults = crf.test(instanceProvider.getRedistributedTestInstances(), maxStepCrit,
				noModelChangeCrit);

		EvaluationResultPrinter.evaluate(crf, testResults);

		crf.printTrainingStatistics(System.out);
		crf.printTestStatistics(System.out);
	}

}

package de.hterhors.semanticmr.projects.examples.corpus.slotfilling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.corpus.JsonCorpus;
import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.OriginalCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.container.DocumentPosition;
import de.hterhors.semanticmr.crf.structure.annotations.container.TextualContent;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.exce.DocumentLinkedAnnotationMismatchException;
import de.hterhors.semanticmr.init.reader.ISpecificationsReader;
import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;

/**
 * Example of how to create a small slot filling corpus using this framework. In
 * this example, we create one training, develop and test instance and store the
 * corpus in json-format to the file system.
 * 
 * 
 * Each instance in a corpus for slot filling consists of a document (document
 * id and the documents content) and a list of entity template annotations with
 * slot filler annotations that are linked to the document by char offset
 * positions.
 * 
 * Further, we create some named entity recognition and linking annotations
 * (nerla) that can be used later for search space exploration.
 * 
 * 
 * @author hterhors
 *
 */
public class SlotFillingCorpusCreationExample {

	private static Logger log = LogManager.getFormatterLogger(SlotFillingCorpusCreationExample.class);

	public static void main(String[] args) {

		/*
		 * ### BUILD CORPUS ###
		 */
		/**
		 * To create a corpus using this tool, we first need to initialize the systems
		 * scope. This is done by specification file(s). For slot filling, we need to
		 * specify 1. the list of existing entities, 2. the list of hierarchies of the
		 * entities (this is important during the exploration), 3. a list of slots 4.
		 * the enittiy template structure(s). This is done in four different files: see
		 * folder src/main/resources/examples/corpus_creation/slotfilling/specs/)
		 */

		/**
		 * The entity file.
		 */
		File entitiesFile = new File("src/main/resources/examples/corpus_creation/slotfilling/specs/entities.csv");
		/**
		 * The hierarchies file.
		 */
		File hierarchiesFile = new File(
				"src/main/resources/examples/corpus_creation/slotfilling/specs/hierarchies.csv");
		/**
		 * The slots file.
		 */
		File slotsFile = new File("src/main/resources/examples/corpus_creation/slotfilling/specs/slots.csv");
		/**
		 * The structures file.
		 */
		File structureFile = new File("src/main/resources/examples/corpus_creation/slotfilling/specs/structure.csv");

		/**
		 * The csv specification file reader. For nerl, we can use the single parameter
		 * constructor passing only the entity file.
		 */
		ISpecificationsReader specsReader = new CSVScopeReader(entitiesFile, hierarchiesFile, slotsFile, structureFile);

		/**
		 * We initialize the system.
		 */
		SystemScope.Builder.getSpecsHandler().addScopeSpecification(specsReader).build();

		/**
		 * The corpus.
		 */
		JsonCorpus nerlCorpus = new JsonCorpus();

		/**
		 * Add training instance:
		 */
		/**
		 * The training document.
		 */
		Document trainDoc = new Document("TrainingDoc",
				"Manuel Peter Neuer (born 27 March 1986) is a German professional footballer who plays as a goalkeeper and captain for the Germany national team. ");

		/**
		 * In each slot filling scenario the root annotation of
		 */
		EntityTemplate trainingAnnotation = new EntityTemplate(AnnotationBuilder.toAnnotation("Manuel_Neuer"));

		/**
		 * Annotations that belong to the document. We basically distinguish between 4
		 * types of annotations. Each type is a sub class of AbstractAnnotation.
		 * 
		 * 1. DocumentLinkedAnnotation is an annotation that is linked to a specific
		 * document. It consists of an entity type, a literal, and the offset position
		 * in its document. This type of annotation is usually used for named entity
		 * recognition and linking. When creating DocumentLinkedAnnotation the framework
		 * checks if the given offset-literal pair can be found in the documents
		 * content. If not a DocumentLinkedAnnotationMismatchException is thrown.
		 * 
		 * 2. LiteralAnnotation is an annotation that consists of a literal and an
		 * entity type but is not necessarily linked to a document. This type can be
		 * used for annotations where just the literal is important but not a specific
		 * one in the text.
		 * 
		 * 3. EntityTypeAnnotation is an annotation that consists solely of an entity
		 * type. This type of annotation can be used e.g. for document classification
		 * tasks where no direct textual relation is required.
		 * 
		 * 4. EntityTemplate is a complex annotation type that consists of a root
		 * annotation which is one of the three mentioned above and a predefined list of
		 * slots. Each slot can take either one or multiple slot filler annotations, as
		 * specified in the specification files. This type of annotation has multiple
		 * purposes as it has a very general structure. many tasks can be formulated as
		 * slot filling e.g. relation extraction (see relation extraction example),
		 * structured prediction problems (see slot filling example)...
		 * 
		 * 
		 * In this example, we use DocumentLinkedAnnotations to create our corpus.
		 * 
		 */
		List<AbstractAnnotation> trainGoldAnnotations = new ArrayList<>();

		try {
			/**
			 * We can either create our own Document linked annotation...
			 */
			trainGoldAnnotations.add(new DocumentLinkedAnnotation(trainDoc, EntityType.get("Barack_Obama"),
					new TextualContent("Barack Obama"), new DocumentPosition(0)));
		} catch (DocumentLinkedAnnotationMismatchException e) {
			/**
			 * This exception is thrown if the textual content and the document position do
			 * not match the documents content.
			 */
			e.printStackTrace();
		}
		/**
		 * Or use the AnnotationBuilder that makes this process a bit easier...
		 * 
		 * Note: using the annotation builder, the
		 * DocumentLinkedAnnotationMismatchException is converted into a runtime
		 * exception.
		 */
		trainGoldAnnotations.add(AnnotationBuilder.toAnnotation(trainDoc, "USA", "USA", 44));

		/**
		 * Now, we can create the first training instance and add it to our corpus.
		 */
		nerlCorpus.addInstance(new Instance(EInstanceContext.TRAIN, trainDoc, new Annotations(trainGoldAnnotations)));

		/**
		 * Development instance.
		 */
		Document devDoc = new Document("DevelopmentDoc", "Donald Trump is the current president of the United States.");
		nerlCorpus.addInstance(new Instance(EInstanceContext.DEVELOPMENT, devDoc, new Annotations(//
				Arrays.asList(//
						AnnotationBuilder.toAnnotation(devDoc, "Donald_Trump", "Donald Trump", 0),
						AnnotationBuilder.toAnnotation(devDoc, "USA", "United States", 45)))));

		/**
		 * Test instance.
		 */
		Document testDoc = new Document("TestDoc", "Angela Merkel is the current federal chancellor of Germany.");
		nerlCorpus.addInstance(new Instance(EInstanceContext.TEST, testDoc, new Annotations(//
				Arrays.asList(//
						AnnotationBuilder.toAnnotation(testDoc, "Angela_Merkel", "Angela Merkel", 0),
						AnnotationBuilder.toAnnotation(testDoc, "Germany", "Germany", 51)))));

		/*
		 * ### WRITE CORPUS ###
		 */

		/**
		 * In this small example we set json pretty string to true; Setting this to
		 * false saves disc space and IO-time when reading / writing this corpus!
		 * Recommended for large corpora.
		 */
		boolean prettyString = true;
		final File corpusDir = new File("src/main/resources/examples/corpus_creation/nerl/corpus/");

		try {
			/**
			 * Writes each instance into a single file.
			 */
			nerlCorpus.write(corpusDir, prettyString);
		} catch (IOException e) {
			/**
			 * Catch std. IOException when working with print streams / files.
			 */
			e.printStackTrace();
		}

		/*
		 * ### READ CORPUS ###
		 */
		/**
		 * In order to read the corpus from the file system, we first need to specify
		 * the train/dev/test - distribution of the instances. In this example, we chose
		 * the original distribution. here we do not need to specify anything for now.
		 * This distribution is also the default if none is specified.
		 */
		AbstractCorpusDistributor originalDistributor = new OriginalCorpusDistributor.Builder().build();

		/**
		 * We can also redistribute the instances if necessary, e.g. in 10 fold cross
		 * validation setting or if there is no original distribution provided. One way
		 * is to make use of the shuffle distributor. Here we set the corpus size to 100
		 * % (that means we use all data, reduce during development to save time), then
		 * we set the proportion of training, development and test set. In this example,
		 * we set all to the same value (in this case 50). We further set a seed to
		 * ensure same distribution of multiple runs.
		 */
//		TODO: try
		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(50).setDevelopmentProportion(50).setTestProportion(50)
				.setSeed(100L).build();

		/**
		 * The instance provider reads all json files in the given directory. We can set
		 * the distributor in the constructor. If not all instances should be read from
		 * the file system, we can add an additional parameter that specifies how many
		 * instances should be read. NOTE: in contrast to the corpusSizeFraction in the
		 * CorpusDistributor, we here set a limit to the number of files that should be
		 * read.
		 */
		InstanceProvider instanceProvider = new InstanceProvider(corpusDir, originalDistributor);

		/**
		 * Now we can print the instances. The instance provider lets us to chose to get
		 * either the original distributed instances (even if we selected a different
		 * distributor) or the redistributed instances.
		 */
		/**
		 * 
		 */
		log.info("Original...");
		log.info(instanceProvider.getOriginalTrainingInstances());
		log.info(instanceProvider.getOriginalDevelopInstances());
		log.info(instanceProvider.getOriginalTestInstances());

		log.info("Redistributed...");
		log.info(instanceProvider.getRedistributedTrainingInstances());
		log.info(instanceProvider.getRedistributedDevelopmentInstances());
		log.info(instanceProvider.getRedistributedTestInstances());

	}

}

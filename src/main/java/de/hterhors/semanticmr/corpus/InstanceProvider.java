package de.hterhors.semanticmr.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.IInstanceDistributor;
import de.hterhors.semanticmr.corpus.distributor.OriginalCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.SpecifiedDistributor;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.Instance.DeduplicationRule;
import de.hterhors.semanticmr.crf.variables.Instance.GoldModificationRule;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.exce.DuplicateDocumentException;
import de.hterhors.semanticmr.json.JsonInstanceReader;

/**
 * Reads and provides instances.
 * 
 * Set {@link #removeEmptyInstances} to true to remove empty instances while
 * loading.
 * 
 * Set {@link #removeInstancesWithToManyAnnotations} to true to remove instances
 * that exceed the maximum number of annotations while loading.
 * 
 * 
 * @author hterhors
 *
 */
public class InstanceProvider {
	private static Logger log = LogManager.getFormatterLogger(InstanceProvider.class);

	public static boolean removeEmptyInstances = true;
	public static boolean removeInstancesWithToManyAnnotations = false;
	public static int maxNumberOfAnnotations = CartesianEvaluator.MAXIMUM_PERMUTATION_SIZE;

	final private File jsonInstancesDirectory;

	final private List<Instance> instances;

	/**
	 * Returns all instances that could be found in the corpus.
	 * 
	 * @return
	 */
	public List<Instance> getInstances() {
		return instances;
	}

	final private List<Instance> redistTrainInstances = new ArrayList<>();
	final private List<Instance> redistDevInstances = new ArrayList<>();
	final private List<Instance> redistTestInstances = new ArrayList<>();
	public static boolean verbose = true;
	private IInstanceDistributor distributor;

	/**
	 * Redistribute the instances of the data set based on the given distributor
	 * strategy.
	 * 
	 * @param distributor
	 * @return
	 */
	private List<Instance> redistribute(List<Instance> instancesToRedistribute) {
		log.info("Redistribute instances based on: " + this.distributor.getDistributorID() + "...");
		this.distributor.distributeInstances(instancesToRedistribute).distributeTrainingInstances(redistTrainInstances)
				.distributeDevelopmentInstances(redistDevInstances).distributeTestInstances(redistTestInstances);

		List<Instance> redistributedInstances = Streams.concat(this.redistTrainInstances.stream(),
				this.redistDevInstances.stream(), this.redistTestInstances.stream()).collect(Collectors.toList());

		final List<String> notDistributableInstances = new ArrayList<>();

		notDistributableInstances
				.addAll(instancesToRedistribute.stream().map(i -> i.getName()).collect(Collectors.toList()));
		notDistributableInstances
				.removeAll(redistributedInstances.stream().map(i -> i.getName()).collect(Collectors.toList()));

		if (!notDistributableInstances.isEmpty()) {
			log.warn("Could not redistribute following instances: ");
			notDistributableInstances.forEach(log::warn);
		}

		log.info("Number of trainings instances: " + redistTrainInstances.size());
		log.info("Number of develop instances: " + redistDevInstances.size());
		log.info("Number of test instances: " + redistTestInstances.size());
		return redistributedInstances;
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, Collection<GoldModificationRule> modifyGoldRules) {
		this(jsonInstancesDirectory, null, Integer.MAX_VALUE, modifyGoldRules, (a, b) -> false);
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * Applies the distributor to the original data set.
	 * 
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param distributor            the distributor.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor,
			Collection<GoldModificationRule> modifyGoldRules, DeduplicationRule duplicationRule) {
		this(jsonInstancesDirectory, distributor, Integer.MAX_VALUE, modifyGoldRules, duplicationRule);
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * Applies the distributor to the original data set.
	 * 
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param distributor            the distributor.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor,
			Collection<GoldModificationRule> modifyGoldRules) {
		this(jsonInstancesDirectory, distributor, Integer.MAX_VALUE, modifyGoldRules, (a, b) -> false);
	}

	/**
	 * Reads <code>numToRead</code> .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param numToRead              number to read.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final int numToRead,
			Collection<GoldModificationRule> modifyGoldRules) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				numToRead, modifyGoldRules, (a, b) -> false);
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 */
	public InstanceProvider(final File jsonInstancesDirectory) {
		this(jsonInstancesDirectory, null, Integer.MAX_VALUE, Collections.emptySet(), (a, b) -> false);
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * Applies the distributor to the original data set.
	 * 
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param distributor            the distributor.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor) {
		this(jsonInstancesDirectory, distributor, Integer.MAX_VALUE, Collections.emptySet(), (a, b) -> false);
	}

	/**
	 * Reads <code>numToRead</code> .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param numToRead              number to read.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final int numToRead) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				numToRead, Collections.emptySet(), (a, b) -> false);
	}

	private <T> Set<T> findDuplicates(Collection<T> collection) {
		final Set<T> uniques = new HashSet<>();
		return collection.stream().filter(e -> !uniques.add(e)).collect(Collectors.toSet());
	}

	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor,
			final int numToRead, Collection<GoldModificationRule> modifyGoldRules, DeduplicationRule duplicationRule) {
		log.info("Read instances from: " + jsonInstancesDirectory);
		log.info("Distributor: " + distributor);
		try {
			this.jsonInstancesDirectory = jsonInstancesDirectory;

			this.distributor = distributor;

			this.validateFiles();

			JsonInstanceReader reader = new JsonInstanceReader(jsonInstancesDirectory, modifyGoldRules,
					duplicationRule);

			List<Instance> instancesToRedistribute;

			if (distributor instanceof SpecifiedDistributor && ((SpecifiedDistributor) distributor).filter)
				instancesToRedistribute = reader.readInstances(numToRead,
						((SpecifiedDistributor) this.distributor).instanceNames);
			else
				instancesToRedistribute = reader.readInstances(numToRead);

			checkInstancesForDuplicats(instancesToRedistribute);

			log.info("Total number of instances loaded: " + instancesToRedistribute.size());
			filterInstancesByCardinality(instancesToRedistribute);
			log.info("Instances remain after cardinality filter: " + instancesToRedistribute.size());

			if (this.distributor != null)
				this.instances = redistribute(instancesToRedistribute);
			else
				this.instances = instancesToRedistribute;

			log.info("Total number of distributed instances: " + getInstances().size());

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void checkInstancesForDuplicats(List<Instance> instancesToRedistribute) {
		Set<String> dups = findDuplicates(
				instancesToRedistribute.stream().map(i -> i.getDocument().documentID).collect(Collectors.toList()));

		if (!dups.isEmpty())
			throw new DuplicateDocumentException("Duplicate document IDs detected: " + dups);
	}

	public void filterInstancesByCardinality(List<Instance> allInstances) {
		for (Iterator<Instance> iterator = allInstances.iterator(); iterator.hasNext();) {
			Instance instance = iterator.next();
			if (instance.getGoldAnnotations().getAnnotations().isEmpty()) {
				if (verbose)
					log.debug("Instance " + instance.getName() + " has no annotations!");
				if (removeEmptyInstances) {
					iterator.remove();
					if (verbose)
						log.debug("Remove instance!");
				} else {
					if (verbose)
						log.debug("Keep instance!");
				}
			}

			if (instance.getGoldAnnotations().getAnnotations().size() > maxNumberOfAnnotations) {
				if (verbose)
					log.debug("WARN: Instance " + instance.getName() + " has to many annotations: "
							+ instance.getGoldAnnotations().getAnnotations().size() + " max number: "
							+ maxNumberOfAnnotations + "!");
				if (removeInstancesWithToManyAnnotations) {
					iterator.remove();
					if (verbose)
						log.debug("Remove instance!");
				} else {
					if (verbose)
						log.debug("Keep instance! Apply Fallback evaluator");
				}
			}

		}
	}

	private void validateFiles() {
		if (!jsonInstancesDirectory.exists())
			throw new IllegalArgumentException("File does not exist: " + jsonInstancesDirectory.getAbsolutePath());

		if (!jsonInstancesDirectory.isDirectory())
			log.warn("Expect a directory: " + jsonInstancesDirectory.getName());

	}

	/**
	 * Returns the list of instances that were originally tagged as training
	 * instance.
	 * 
	 * @return a list of training instances
	 */
	public List<Instance> getOriginalTrainingInstances() {
		return this.instances.stream().filter(i -> i.getOriginalContext() == EInstanceContext.TRAIN)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the list of instances that were originally tagged as development
	 * instance.
	 * 
	 * @return a list of development instances
	 */
	public List<Instance> getOriginalDevelopInstances() {
		return this.instances.stream().filter(i -> i.getOriginalContext() == EInstanceContext.DEVELOPMENT)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the list of instances that were originally tagged as test instance.
	 * 
	 * @return a list of test instances
	 */
	public List<Instance> getOriginalTestInstances() {
		return this.instances.stream().filter(i -> i.getOriginalContext() == EInstanceContext.TEST)
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list of training instances based on the distribution strategy.
	 * 
	 * @return a list of training instances based on the distributor.
	 */
	public List<Instance> getTrainingInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistTrainInstances);
	}

	/**
	 * Returns a list of development instances based on the distribution strategy.
	 * 
	 * @return a list of development instances based on the distributor.
	 */
	public List<Instance> getDevelopmentInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistDevInstances);
	}

	/**
	 * Returns a list of test instances based on the distribution strategy.
	 * 
	 * @return a list of test instances based on the distributor.
	 */
	public List<Instance> getTestInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistTestInstances);
	}
}

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

import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.IInstanceDistributor;
import de.hterhors.semanticmr.corpus.distributor.OriginalCorpusDistributor;
import de.hterhors.semanticmr.crf.variables.Instance;
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

	public static boolean removeEmptyInstances = false;
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
	 */
	public void redistribute(final IInstanceDistributor distributor) {
		this.distributor = distributor;
		log.info("Redistribute instances based on: " + distributor.getDistributorID() + "...");
		this.distributor.distributeInstances(this).distributeTrainingInstances(redistTrainInstances)
				.distributeDevelopmentInstances(redistDevInstances).distributeTestInstances(redistTestInstances);
		log.info("Number of trainings instances: " + redistTrainInstances.size());
		log.info("Number of develop instances: " + redistDevInstances.size());
		log.info("Number of test instances: " + redistTestInstances.size());
	}

	/**
	 * Reads all .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 */
	public InstanceProvider(final File jsonInstancesDirectory) {
		this(jsonInstancesDirectory, null, Integer.MAX_VALUE);
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
		this(jsonInstancesDirectory, distributor, Integer.MAX_VALUE);
	}

	/**
	 * Reads <code>numToRead</code> .json files from the given directory.
	 * 
	 * @param jsonInstancesDirectory the data set directory.
	 * @param numToRead              number to read.
	 */
	public InstanceProvider(final File jsonInstancesDirectory, final int numToRead) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				numToRead);
	}

	private <T> Set<T> findDuplicates(Collection<T> collection) {
		final Set<T> uniques = new HashSet<>();
		return collection.stream().filter(e -> !uniques.add(e)).collect(Collectors.toSet());
	}

	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor,
			final int numToRead) {
		try {
			this.jsonInstancesDirectory = jsonInstancesDirectory;

			this.distributor = distributor;

			this.validateFiles();

			this.instances = new JsonInstanceReader(jsonInstancesDirectory).readInstances(numToRead);

			Set<String> dups = findDuplicates(
					this.instances.stream().map(i -> i.getDocument().documentID).collect(Collectors.toList()));

			if (!dups.isEmpty())
				throw new DuplicateDocumentException("Duplicate document IDs detected: " + dups);

			log.info("Total number of instances loaded: " + getInstances().size());

			for (Iterator<Instance> iterator = instances.iterator(); iterator.hasNext();) {
				Instance instance = iterator.next();
				if (instance.getGoldAnnotations().getAnnotations().isEmpty()) {
				if(verbose)
					log.warn("Instance " + instance.getName() + " has no annotations!");
					if (removeEmptyInstances) {
						iterator.remove();
						if(verbose)
						log.warn("Remove instance!");
					} else {
						if(verbose)
						log.warn("Keep instance!");
					}
				}

				if (instance.getGoldAnnotations().getAnnotations().size() >= maxNumberOfAnnotations) {
					if(verbose)
					log.warn("WARN: Instance " + instance.getName() + " has to many annotations!");
					if (removeInstancesWithToManyAnnotations) {
						iterator.remove();
						if(verbose)
						log.warn("Remove instance!");
					} else {
						if (verbose)
							log.warn("Keep instance!");
					}
				}

			}

			if (this.distributor != null)
				redistribute(this.distributor);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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
	public List<Instance> getRedistributedTrainingInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistTrainInstances);
	}

	/**
	 * Returns a list of development instances based on the distribution strategy.
	 * 
	 * @return a list of development instances based on the distributor.
	 */
	public List<Instance> getRedistributedDevelopmentInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistDevInstances);
	}

	/**
	 * Returns a list of test instances based on the distribution strategy.
	 * 
	 * @return a list of test instances based on the distributor.
	 */
	public List<Instance> getRedistributedTestInstances() {
		if (distributor == null)
			throw new IllegalStateException("No distributor specified!");
		return Collections.unmodifiableList(redistTestInstances);
	}
}

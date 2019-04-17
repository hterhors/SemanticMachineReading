package de.hterhors.semanticmr.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.IInstanceDistributor;
import de.hterhors.semanticmr.corpus.distributor.OriginalCorpusDistributor;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.json.JsonInstancesReader;

public class InstanceProvider {

	final private File jsonInstancesDirectory;

	final private List<Instance> instances;

	public List<Instance> getInstances() {
		return instances;
	}

	final private List<Instance> redistTrainInstances = new ArrayList<>();
	final private List<Instance> redistDevInstances = new ArrayList<>();
	final private List<Instance> redistTestInstances = new ArrayList<>();

	private IInstanceDistributor distributor;

	public void redistribute(final IInstanceDistributor distributor) {
		this.distributor = distributor;
		this.distributor.distributeInstances(this).distributeTrainingInstances(redistTrainInstances)
				.distributeDevelopmentInstances(redistDevInstances).distributeTestInstances(redistTestInstances);
		System.out.println("Number of trainings instances: " + redistTrainInstances.size());
		System.out.println("Number of develop instances: " + redistDevInstances.size());
		System.out.println("Number of test instances: " + redistTestInstances.size());

	}

	public List<Instance> getRedistributedTrainingInstances() {
		return Collections.unmodifiableList(redistTrainInstances);
	}

	public List<Instance> getRedistributedDevelopmentInstances() {
		return Collections.unmodifiableList(redistDevInstances);
	}

	public List<Instance> getRedistributedTestInstances() {
		return Collections.unmodifiableList(redistTestInstances);
	}

	public InstanceProvider(final File jsonInstancesDirectory) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				Integer.MAX_VALUE);
	}

	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				Integer.MAX_VALUE);
	}

	public InstanceProvider(final File jsonInstancesDirectory, final int numToRead) {
		this(jsonInstancesDirectory, new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build(),
				numToRead);
	}

	public InstanceProvider(final File jsonInstancesDirectory, final AbstractCorpusDistributor distributor,
			final int numToRead) {
		try {
			this.jsonInstancesDirectory = jsonInstancesDirectory;

			this.distributor = distributor;

			this.validateFiles();

			this.instances = new JsonInstancesReader(jsonInstancesDirectory).readInstances(numToRead);

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
			System.out.println("Warn! Expected directory: " + jsonInstancesDirectory.getName());

	}

	public List<Instance> getOriginalTrainingInstances() {
		return this.instances.stream().filter(i -> i.getContext() == EInstanceContext.TRAIN)
				.collect(Collectors.toList());
	}

	public List<Instance> getOriginalDevelopInstances() {
		return this.instances.stream().filter(i -> i.getContext() == EInstanceContext.DEVELOPMENT)
				.collect(Collectors.toList());
	}

	public List<Instance> getOriginalTestInstances() {
		return this.instances.stream().filter(i -> i.getContext() == EInstanceContext.TEST)
				.collect(Collectors.toList());
	}

}

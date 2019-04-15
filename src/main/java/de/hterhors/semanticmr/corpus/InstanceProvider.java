package de.hterhors.semanticmr.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.candprov.DocumentCandidateProviderCollection;
import de.hterhors.semanticmr.candprov.GeneralCandidateProvider;
import de.hterhors.semanticmr.corpus.distributor.IInstanceDistributor;
import de.hterhors.semanticmr.corpus.distributor.OriginalCorpusDistributor;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;
import de.hterhors.semanticmr.json.JsonInstancesReader;
import de.hterhors.semanticmr.json.JsonNerlaReader;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;

public class InstanceProvider {

	final private File jsonNerlaAnnotationsFile;
	final private File jsonInstancesDirectory;

	final private List<Instance> instances;

	final private DocumentCandidateProviderCollection candidateProvider;

	public List<Instance> getAllInstances() {
		return instances;
	}

	final private List<Instance> redistTrainInstances = new ArrayList<>();
	final private List<Instance> redistDevInstances = new ArrayList<>();
	final private List<Instance> redistTestInstances = new ArrayList<>();

	private IInstanceDistributor distributor;

	public void redistribute(final IInstanceDistributor distributor) {
		this.distributor = distributor;
		this.distributor.distributeInstances(this).distributeDevelopmentInstances(redistTrainInstances)
				.distributeTestInstances(redistTestInstances).distributeTrainingInstances(redistTrainInstances);
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

	public DocumentCandidateProviderCollection getCandidateProvider() {
		return candidateProvider;
	}

	public InstanceProvider(final SystemInitializer initializer, final File jsonInstancesDirectory,
			final File jsonNerlaAnnotationsFile) {
		this(initializer, jsonInstancesDirectory, jsonNerlaAnnotationsFile,
				new OriginalCorpusDistributor.Builder().setCorpusSizeFraction(1F).build());
	}

	public InstanceProvider(final SystemInitializer initializer, final File jsonInstancesDirectory,
			final File jsonNerlaAnnotationsFile, final IInstanceDistributor distributor) {
		try {
			this.jsonInstancesDirectory = jsonInstancesDirectory;
			this.jsonNerlaAnnotationsFile = jsonNerlaAnnotationsFile;

			this.distributor = distributor;

			this.validateFiles();

			this.instances = new JsonInstancesReader(initializer, jsonInstancesDirectory).readInstances();

			this.candidateProvider = new DocumentCandidateProviderCollection();

			final Map<String, List<EntityTypeAnnotation>> nerla = new JsonNerlaReader(initializer,
					jsonNerlaAnnotationsFile).read();

			for (Instance instance : instances) {
				candidateProvider.addLiteralCandidateProvider(new GeneralCandidateProvider(instance.getDocument())
						.addBatchSlotFiller(nerla.get(instance.getDocument().documentID)));
			}

			redistribute(distributor);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void validateFiles() {
		if (!jsonInstancesDirectory.exists())
			throw new IllegalArgumentException("File does not exist: " + jsonInstancesDirectory.getAbsolutePath());

		if (!jsonNerlaAnnotationsFile.exists())
			throw new IllegalArgumentException("File does not exist: " + jsonNerlaAnnotationsFile.getAbsolutePath());

		if (!jsonInstancesDirectory.isDirectory())
			System.out.println("Warn! Expected directory: " + jsonInstancesDirectory.getName());
		if (!jsonNerlaAnnotationsFile.getName().endsWith(".json"))
			System.out.println("Warn! Unexpected file format: " + jsonNerlaAnnotationsFile.getName());

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
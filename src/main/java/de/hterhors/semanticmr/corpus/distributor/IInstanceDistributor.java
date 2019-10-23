package de.hterhors.semanticmr.corpus.distributor;

import java.util.List;

import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor.IDistributorStrategy;
import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * Classes that implement this interface contain the configuration for the
 * corpus provider.
 * 
 * @author hterhors
 *
 * @date Oct 13, 2017
 */
public interface IInstanceDistributor {

	IDistributorStrategy distributeInstances(List<Instance> instancesToRedistribute);

	public String getDistributorID();

}
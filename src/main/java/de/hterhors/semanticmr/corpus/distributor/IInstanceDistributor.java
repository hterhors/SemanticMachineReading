package de.hterhors.semanticmr.corpus.distributor;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor.IDistributorStrategy;

/**
 * Classes that implement this interface contain the configuration for the
 * corpus provider.
 * 
 * @author hterhors
 *
 * @date Oct 13, 2017
 */
public interface IInstanceDistributor {

	IDistributorStrategy distributeInstances(InstanceProvider corpusProvider);

	public String getDistributorID();

}
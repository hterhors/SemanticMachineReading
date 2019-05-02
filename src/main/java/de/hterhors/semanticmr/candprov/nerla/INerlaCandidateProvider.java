package de.hterhors.semanticmr.candprov.nerla;

import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;

/**
 * The interface for the named entity recognition and linking candidate
 * provider.
 * 
 * @author hterhors
 *
 */
public interface INerlaCandidateProvider {

	/**
	 * Given any free text, this method returns a list of entity-types that serve as
	 * potential candidates for the given text.
	 * 
	 * @param text the free text
	 * @return a set of entity-type candidates.
	 */
	public Set<EntityType> getEntityTypeCandidates(final String text);

}

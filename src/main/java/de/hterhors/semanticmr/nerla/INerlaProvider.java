package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;

/**
 * The named entity recognition and linking annotation (nerla) provider
 * interface needs to be implemented by classes that provide nerla during slot
 * filling.
 * 
 * @author hterhors
 *
 */
@Deprecated
public interface INerlaProvider {

	/**
	 * Returns a list of named entity recognition and linking annotations of type
	 * DocumentLinkedAnnotation for each instance that is passed as parameter to
	 * this method.
	 * 
	 * @param instances
	 * @return a list of DocumentLinkedAnnotations mapped to the corresponding
	 *         instance.
	 * @throws IOException
	 */
	public Map<Instance, List<DocumentLinkedAnnotation>> getForInstances(List<Instance> instances);

}

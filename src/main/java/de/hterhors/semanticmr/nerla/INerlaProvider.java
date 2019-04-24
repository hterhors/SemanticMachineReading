package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;

public interface INerlaProvider {

	public Map<Instance, List<DocumentLinkedAnnotation>> getForInstances(List<Instance> instance) throws IOException;

}

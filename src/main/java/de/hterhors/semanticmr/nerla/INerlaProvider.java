package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;

public interface INerlaProvider {

	public Map<Instance, List<EntityTypeAnnotation>> getForInstances(List<Instance> instance) throws IOException;

}

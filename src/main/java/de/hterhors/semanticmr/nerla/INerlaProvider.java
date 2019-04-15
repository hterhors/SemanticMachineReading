package de.hterhors.semanticmr.nerla;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.structure.annotations.EntityTypeAnnotation;

public interface INerlaProvider {

	public Map<Instance, List<EntityTypeAnnotation>> get(List<Instance> instance) throws IOException;

}

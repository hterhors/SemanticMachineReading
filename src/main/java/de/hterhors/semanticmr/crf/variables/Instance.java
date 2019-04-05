package de.hterhors.semanticmr.crf.variables;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;
import de.hterhors.semanticmr.structure.slotfiller.EntityType;

public class Instance {

	public final Document document;
	public final EntityTemplate goldTemplate;

	public Instance(Document document, EntityTemplate goldTemplate) {
		this.document = document;
		this.goldTemplate = goldTemplate;
	}

	public State getInitialState() {
		return new State(goldTemplate, new EntityTemplate(EntityType.get("AnimalModel")));
	}

}

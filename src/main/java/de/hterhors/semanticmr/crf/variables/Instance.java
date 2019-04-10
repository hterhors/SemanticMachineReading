package de.hterhors.semanticmr.crf.variables;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

public class Instance {

	private final Document document;

	private final EntityTemplate goldTemplate;

	public Instance(Document document, EntityTemplate goldTemplate) {
		this.goldTemplate = goldTemplate;
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public EntityTemplate getGoldTemplate() {
		return goldTemplate;
	}

}

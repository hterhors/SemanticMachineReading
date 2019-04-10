package de.hterhors.semanticmr.crf.variables;

import de.hterhors.semanticmr.structure.slotfiller.EntityTemplate;

/**
 * The Instance object couples a document and the gold annotation of that
 * document. This class is used during training as training instance, during
 * testing as testing instance and during prediction as prediction instance.
 * 
 * @author hterhors
 *
 */
public class Instance {

	/**
	 * The instance document.
	 */
	private final Document document;

	/**
	 * The corresponding gold annotation.
	 */
	private final EntityTemplate goldAnnotation;

	public Instance(Document document, EntityTemplate goldTemplate) {
		this.goldAnnotation = goldTemplate;
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public EntityTemplate getGoldTemplate() {
		return goldAnnotation;
	}

}

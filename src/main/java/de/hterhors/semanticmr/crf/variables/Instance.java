package de.hterhors.semanticmr.crf.variables;

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
	private final Annotations goldAnnotations;

	public Instance(Document document, Annotations goldAnnotations) {
		this.goldAnnotations = goldAnnotations;
		this.document = document;
		this.goldAnnotations.unmodifiable();
	}

	public Document getDocument() {
		return document;
	}

	public Annotations getGoldAnnotations() {
		return goldAnnotations;
	}

}

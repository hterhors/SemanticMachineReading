package de.hterhors.semanticmr.exce;

/**
 * Is thrown if an annotation was added to a document, but the char offset does
 * not match the tokenization of the document.
 * 
 * @author hterhors
 *
 */
public class DocumentLinkedAnnotationMismatchException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DocumentLinkedAnnotationMismatchException(String msg) {
		super(msg);
	}
}

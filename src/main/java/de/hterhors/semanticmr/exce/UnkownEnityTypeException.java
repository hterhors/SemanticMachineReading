package de.hterhors.semanticmr.exce;

/**
 * This exception is thrown, if an entity type is requested from the entity type
 * pool but it does not exist.
 * 
 * @author hterhors
 *
 */
public class UnkownEnityTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnkownEnityTypeException(String msg) {
		super(msg);
	}

}

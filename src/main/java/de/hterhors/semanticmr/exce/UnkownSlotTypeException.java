package de.hterhors.semanticmr.exce;

/**
 * This exception is thrown, if an slot type is requested from the slot type
 * pool but it does not exist.
 * 
 * @author hterhors
 *
 */
public class UnkownSlotTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnkownSlotTypeException(String msg) {
		super(msg);
	}

}

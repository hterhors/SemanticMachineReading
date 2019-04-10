package de.hterhors.semanticmr.exceptions;

/**
 * This exception is thrown, if a single slot is requested but it does not
 * exist.
 * 
 * @author hterhors
 *
 */
public class UnkownSingleSlotException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnkownSingleSlotException(String msg) {
		super(msg);
	}

}

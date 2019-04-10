package de.hterhors.semanticmr.exceptions;

/**
 * This exception is thrown if the slot filler does not match the slot
 * specifications.
 * 
 * @author hterhors
 *
 */
public class IllegalSlotFillerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalSlotFillerException(String msg) {
		super(msg);
	}

}

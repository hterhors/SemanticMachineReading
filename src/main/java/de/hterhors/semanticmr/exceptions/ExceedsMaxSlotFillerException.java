package de.hterhors.semanticmr.exceptions;

/**
 * This exception is thrown if a new slot filler is added to a multi filler slot
 * but the maximum number of allowed slot filler values is reached.
 * 
 * @author hterhors
 *
 */
public class ExceedsMaxSlotFillerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExceedsMaxSlotFillerException(String msg) {
		super(msg);
	}

}

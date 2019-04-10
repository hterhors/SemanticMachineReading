package de.hterhors.semanticmr.exce;

/**
 * This exception is thrown, if a multi slot is requested but it does not exist.
 * 
 * @author hterhors
 *
 */
public class UnkownMultiSlotException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnkownMultiSlotException(String msg) {
		super(msg);
	}

}

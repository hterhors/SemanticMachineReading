package de.hterhors.semanticmr.exce;

/**
 * This exception is thrown, if the specification file is not readable.
 * 
 * @author hterhors
 *
 */
public class InvalidSpecificationFileFormatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSpecificationFileFormatException(String msg) {
		super(msg);
	}

	public InvalidSpecificationFileFormatException(Exception e) {
		super(e);
	}

}

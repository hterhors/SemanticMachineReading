package de.hterhors.semanticmr.exceptions;

public class InvalidFileFormatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileFormatException(String msg) {
		super(msg);
	}

	public InvalidFileFormatException(Exception e) {
		super(e);
	}

}

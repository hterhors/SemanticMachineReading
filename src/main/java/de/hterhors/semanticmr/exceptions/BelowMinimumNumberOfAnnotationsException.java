package de.hterhors.semanticmr.exceptions;

public class BelowMinimumNumberOfAnnotationsException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BelowMinimumNumberOfAnnotationsException(String msg) {
		super(msg);
	}
}

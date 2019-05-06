package de.hterhors.semanticmr.exce;

public class SystemNotInitializedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SystemNotInitializedException(String msg) {
		super(msg);
	}

	public SystemNotInitializedException(Exception e) {
		super(e);
	}

}

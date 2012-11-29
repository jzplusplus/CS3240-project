package parser;

public class ParseException extends Exception {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3549416292529404463L;

	public ParseException() { }

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
}
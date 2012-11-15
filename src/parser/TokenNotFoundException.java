package parser;

public class TokenNotFoundException extends Exception {
	  public TokenNotFoundException() { }

	  public TokenNotFoundException(String message) {
	    super(message);
	  }

	  public TokenNotFoundException(String message, Throwable cause) {
	    super(message, cause);
	  }

	  public TokenNotFoundException(Throwable cause) {
	    super(cause);
	  }	  
}
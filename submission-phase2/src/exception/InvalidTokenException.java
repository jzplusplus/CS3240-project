package exception;

public class InvalidTokenException extends Exception {

	public InvalidTokenException() {
		super("The token is not a valid MiniRE token");
	}
}

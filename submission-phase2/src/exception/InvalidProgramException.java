package exception;

public class InvalidProgramException extends Exception {

	public InvalidProgramException() {
		super("The input program is invalid.");
	}
}

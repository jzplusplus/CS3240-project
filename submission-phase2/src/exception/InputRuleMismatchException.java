package exception;

public class InputRuleMismatchException extends Exception {
	
	public InputRuleMismatchException() {
		super("There is a mismatch between the input and the grammar.");
	}
}

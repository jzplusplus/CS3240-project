package exception;

public class IncorrectRuleFormatException extends Exception {
	
	public IncorrectRuleFormatException() {
		super("Rules in the grammar specification are formatted incorrectly");
	}

}

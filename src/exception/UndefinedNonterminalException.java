package exception;

public class UndefinedNonterminalException extends Exception {

	public UndefinedNonterminalException() {
		super("There is undefined nonterminal in the grammar specification.");
	}
}

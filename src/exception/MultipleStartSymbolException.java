package exception;

public class MultipleStartSymbolException extends Exception {
	public MultipleStartSymbolException() {
		super("Multiple Start Symbols found in the grammar specification.");
		
	}

}

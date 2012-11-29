package parser;

public interface Symbol {
	
	boolean isTerminal();
	String getValue();

	
	public static enum NonterminalSymbol implements Symbol {
		REGEX, // hmm, this doesn't DO anything
		REXP,
		REXP1,
		REXP_PRIME,
		REXP1_PRIME,
		REXP2,
		REXP2_TAIL,
		REXP3,
		CHAR_CLASS,
		CHAR_CLASS1,
		CHAR_SET_LIST,
		CHAR_SET,
		CHAR_SET_TAIL,
		EXCLUDE_SET,
		EXCLUDE_SET_TAIL,
		DEFINED_CLASS;
	

		@Override
		public boolean isTerminal() {
			return false;
		}
	
		@Override
		public String getValue() {
			return this.name();
		}
	}
	
	public static class TerminalSymbol implements Symbol {
		// probably gonna be a regex or at least a chunk of one
		private String value;
		public TerminalSymbol(String value) {
			this.value = value;
		}
		@Override
		public boolean isTerminal() {
			return true;
		}
		@Override
		public String getValue() {
			return this.value;
		}
	}
}

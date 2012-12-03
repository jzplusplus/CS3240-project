package parser;

import java.util.HashSet;
import java.util.Set;

public interface Symbol {
	
	boolean isTerminal();
	String getValue();

	
	public static enum NonterminalMiniReSymbol implements Symbol {
		MINIRE_PROGRAM,
		STATEMENT_LIST,
		STATEMENT_LIST_TAIL,
		STATEMENT,
		FILE_NAMES,
		SOURCE_FILE,
		DESTINATION_FILE,
		EXP_LIST,
		EXP_LIST_TAIL,
		EXP,
		EXP_TAIL,
		TERM,
		FILE_NAME,
		BIN_OP;

		@Override
		public boolean isTerminal() {
			return false;
		}

		@Override
		public String getValue() {
			return this.name();
		}
	}
	
	public static enum NonterminalRegexSymbol implements Symbol {
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
	
	public static enum ReservedWord implements Symbol {
		

		BEGIN("begin"),
		END("end"),
		ASSIGN("="),
		REPLACE("replace"),
		WITH("with"),
		IN("in"),
		SEMICOLON(";"),
		RECURSIVE_REPLACE("recursivereplace"),
		WRITE_TO(">!"),
		PRINT("print"),
		INT("#"),
		FIND("find"),
		DIFF("diff"),
		UNION("union"),
		INTERS("inters"),
		MAXFREQSTRING("maxfreqstring");
		
		private static Set<String> values;
		
		private String value;
		private ReservedWord(String value) {
			this.value = value;
		}
		
		@Override
		public boolean isTerminal() {
			return true;
		}
		@Override
		public String getValue() {
			return value;
		}
		
		public static Set<String> reservedWordSet() {
			if (values == null) {
				values = new HashSet<String>();
				for (ReservedWord w : values()) {
					values.add(w.getValue());
				}
			}
			return values;
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

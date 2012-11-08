package parser;

import java.util.regex.Pattern;

public enum TokenClass implements TokenClassInterface {
	
	// token classes
	TOK_OPEN_PAREN("\\("),
	TOK_CLOSE_PAREN("\\)"),
	TOK_ADDITIVE("[-+]"),
	TOK_MULTIPLICATIVE("[*/]"),
	TOK_EXPONENT("\\^"),
	TOK_NUMBER("\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?"); // need to add TOK_LETTERS

	// lexes
	public static final String LEX_ADD      = "+";
	public static final String LEX_SUBTRACT = "-";
	public static final String LEX_MULTIPLY = "*";
	public static final String LEX_DIVIDE   = "/";
	public static final String LEX_EXPONENT = "^";
	public static final String LEX_OPEN_PAREN  = "(";
	public static final String LEX_CLOSE_PAREN = ")";
	
	private Pattern pattern;
	
	@Override
	public Object getType() {
		return this;
	}
	@Override
	public Pattern getPattern() {
		return this.pattern;
	}

	TokenClass(String pattern) {
		this.pattern = Pattern.compile(pattern);
 	}
	
}

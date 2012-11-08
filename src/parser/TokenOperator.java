package parser;

import java.util.HashMap;

public enum TokenOperator {
	
	ADD      { double evaluate(double a, double b) { return a + b; } },
	SUBTRACT { double evaluate(double a, double b) { return a - b; } },
	MULTIPLY { double evaluate(double a, double b) { return a * b; } },
	DIVIDE   { double evaluate(double a, double b) { return a / b; } },
	EXPONENT { double evaluate(double a, double b) { return Math.pow(a, b); } };

	static final HashMap<String, TokenOperator> operators;

	static {
		operators = new HashMap<String, TokenOperator>();

		operators.put(TokenClass.LEX_ADD, ADD);
		operators.put(TokenClass.LEX_SUBTRACT, SUBTRACT);
		operators.put(TokenClass.LEX_MULTIPLY, MULTIPLY);
		operators.put(TokenClass.LEX_DIVIDE, DIVIDE);
		operators.put(TokenClass.LEX_EXPONENT, EXPONENT);
	}

	public static final TokenOperator getOperator(Object token) {
		return (TokenOperator)operators.get(token);
	}

	abstract double evaluate(double a, double b);
}
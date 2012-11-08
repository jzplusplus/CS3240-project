package parser;

import java.util.Arrays;
import java.util.LinkedList;

public final class Parser {

	TokenEncoder te;
	transient LinkedList<Object> tokenTree;
	transient Token ahead;
	
	void match(String token) throws ParseException{
	    if(token == null || ahead.getValue().equals(token)) {
	    	try {
	    		ahead = te.nextToken();
	        } catch(TokenNotFoundException e) {
	        	throw new ParseException(e);
	        }
	    } else {
	    	throw new ParseException();
	    }		
	}
	
	void parseNumber() throws ParseException { // need to implement LETTER later
	    double number;

	    if(ahead == null) {
	      throw new ParseException();
	    }

	    try {
	    	number = Double.parseDouble((String)ahead.getValue()); 
	    	System.out.println(number); // before push to the stack, print out to debug
	    	tokenTree.push(new Double(number)); // since it was a token
	    } catch(NumberFormatException e) {
	    	throw new ParseException(e);
	    }
	    match(null);
	}


	void parseFactor() throws ParseException {
		if(ahead == null) {
	      throw new ParseException();
	    }

	   if(ahead.getType() == TokenClass.TOK_OPEN_PAREN) {
		   match(TokenClass.LEX_OPEN_PAREN);
		   parseExpression();
		   match(TokenClass.LEX_CLOSE_PAREN);
	   } else {
		   parseNumber();
	  }
	}

	void parseExponent() throws ParseException {
		Token token;

	    parseFactor();

	    while(ahead != null) {
	    	if(ahead.getType() == TokenClass.TOK_EXPONENT) {
	    		token = ahead;
	    		match((String)ahead.getValue());
	    		parseFactor();
	    		System.out.println((String)token.getValue()); // before push to the stack, print out to debug
	    		tokenTree.push(TokenOperator.getOperator((String)token.getValue()));
	    	} else {
	    		break;
	    	}
	    }
	}

	void parseTerm() throws ParseException {
		Token token;

	    parseExponent();

	    while(ahead != null) {
	    	if(ahead.getType() == TokenClass.TOK_MULTIPLICATIVE) {
	    		token = ahead;
	    		match((String)ahead.getValue());
	    		parseExponent();
	    		System.out.println((String)token.getValue()); // before push to the stack, print out to debug
	    		tokenTree.push(TokenOperator.getOperator((String)token.getValue()));
	    	} else {
	    		break;
	    	}
	    }
	}

	void parseExpression() throws ParseException {
		Token token;

	    parseTerm();

	    while(ahead != null) {
	    	if(ahead.getType() == TokenClass.TOK_ADDITIVE) {
	    		token = ahead;
	    		match((String)ahead.getValue());
	    		parseTerm();
	    		System.out.println((String)token.getValue()); // before push to the stack, print out to debug
	    		tokenTree.push(TokenOperator.getOperator((String)token.getValue()));
	    	} else {
	    		break;
	    	}
	    }
	}
	
	public Parser() {
	    te = new TokenEncoder(Arrays.asList((TokenClassInterface[])TokenClass.values()));
	}

	public LinkedList<Object> parse(String input) throws ParseException { // need to change a parameter to ArrayList<String> inputs
	    te.setInput(input); // need to add a loop here

	    match(null);

	    if(ahead == null) {
	      throw new ParseException();
	    }

	    tokenTree = new LinkedList<Object>();

	    parseExpression();

	    if(ahead != null) {
	      throw new ParseException();
	    }

	    return tokenTree;
	}
	
}

package parser;

import java.util.Arrays;


/**
 * 
 */
public final class Parser {

	Tokenizer te;
	transient Tree<String> tokenTree;
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

	// <rexp> -> <rexp1> <rexpPrime>
	void rexp() throws ParseException{
		System.out.println("rexp");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		rexp1();
		rexpPrime();
		System.out.println("rexp ENDS");
	}
	
	// <rexp1> -> <rexp2> <rexp1Prime> | E
	void rexp1() throws ParseException{
		System.out.println("rexp1");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		rexp2();
		rexp1Prime();
		System.out.println("rexp1 ENDS");
	}
	
	// rexpPrime -> UNION <rexp1> <rexpPrime> | E
	void rexpPrime() throws ParseException{
		System.out.println("rexpPrime");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		Token token;

		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_UNION){
				token = ahead;
	    		match(RegexTokenType.LEX_UNION);
	    		rexp1();
	    		rexpPrime();
	    		// Add to Tree
	    		System.out.println("Saved Here: " + token.getValue());
	    		
			}else{
				break;
			}
		}
		
		System.out.println("rexpPrime ENDS");
		match(null);
	}
	
	// <rexp1¡¯> -> <rexp2> <rexp1¡¯>  | E        
	void rexp1Prime() throws ParseException{
		System.out.println("rexp1Prime");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		rexp2();
		rexp1Prime();
		
		System.out.println("rexp1Prime ENDS");
		match(null);
	}
	
	// <rexp2> -> (<rexp>) <rexp2-tail>  | RE_CHAR <rexp2-tail> | <rexp3>
	void rexp2() throws ParseException{
		System.out.println("rexp2");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_L_PAREN){
				token = ahead;
				match(RegexTokenType.LEX_L_PAREN);
	    		rexp();
	    		match(RegexTokenType.LEX_R_PAREN);
	    		rexp2_tail();
	    		// Add to Tree
	    		System.out.println("Saved Here: " + token.getValue());
	    
			}else if(ahead.getType() == RegexTokenType.RE_CHAR){
				token = ahead;
				match((String)ahead.getValue()); // RE_CHAR
				rexp2_tail();
				// Add to Tree
				System.out.println("Saved Here: " + token.getValue());
				
			}else{
				rexp3();
				break;
			}
		}		
		
		System.out.println("rexp2 ENDS");
	}
	
	// <rexp2-tail> -> * | + | E
	void rexp2_tail() throws ParseException{
		Token token;
		
		System.out.println("rexp2_tail");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}

		if(ahead==null) {
		      throw new ParseException();
		}   
		
		if(ahead.getType() == RegexTokenType.TOKEN_ASTE || ahead.getType() == RegexTokenType.TOKEN_PLUS){
			token = ahead;
			match((String)ahead.getValue());
			// Add to Tree
			System.out.println("Saved Here: " + token.getValue());
		}
		
		System.out.println("rexp2_tail ENDS");
		match(null);		
	}
	
	// <rexp3> -> <char-class>  | E   
	void rexp3() throws ParseException{
		System.out.println("rexp3");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		char_class();
		
		System.out.println("rexp3 ENDS");
		match(null);
	}
	
	// <char-class> -> .  |  [ <char-class1>  | <defined-class>
	void char_class() throws ParseException{
		System.out.println("char_class");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		if(ahead.getType() == RegexTokenType.TOKEN_DOT){
			// Add to Tree
			System.out.println("Saved Here: " + ahead.getValue());
			match(null);
		}
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET){
				token = ahead;
				match(RegexTokenType.LEX_L_BRACKET);
				char_class1();	
				// Add to Tree
				System.out.println("Saved Here: " + token.getValue());
				
			}else { // defined_class
				token = ahead;
				match((String) ahead.getValue());		
				// Add to Tree
				System.out.println("Saved Here: " + token.getValue());
				break;
			}
		}
		System.out.println("ahead: " + ahead.getValue());
		System.out.println("char_class ENDS");
	}
	
	// <char-class1> -> <char-set-list> | <exclude-set>
	void char_class1() throws ParseException{
		System.out.println("char_class1");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		char_set_list();
		exclude_set();		
		System.out.println("char_class1 ENDS");
	}
	
	// <char-set-list> -> <char-set> <char-set-list> | E
	void char_set_list() throws ParseException{
		System.out.println("char_set_list");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		char_set();
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_R_BRACKET){
				token = ahead;
				match(RegexTokenType.LEX_R_BRACKET);
				// Add to Tree		
				System.out.println("Saved Here: " + token.getValue());
				
			}else{
				break;
			}
		}
		
		System.out.println("char_set_list ENDS");
		match(null);
	}
	
	// <char-set> -> CLS_CHAR <char-set-tail> 
	void char_set() throws ParseException{
		System.out.println("char_set");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.CLS_CHAR){
				token = ahead;
				match((String)ahead.getValue()); // CLS_CHAR
				char_set_tail();
				// Add to Tree
				System.out.println("Saved Here: " + token.getValue());
				
			}else{
				break;
			}
		}
		
		System.out.println("char_set ENDS");
	}
	
	// <char-set-tail> -> - CLS_CHAR | E
	void char_set_tail() throws ParseException{		
		System.out.println("char_set_tail");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_DASH){
				// Add to Tree
				match(RegexTokenType.LEX_DASH);
				// Add to Tree
				match((String)ahead.getValue()); // cLS_CHAR
				
			}else{
				break;
			}
			
		}
		
		System.out.println("char_set_tail ENDS");
		match(null);

	}
	
	// <exclude-set> -> ^ <char-set>] IN <exclude-set-tail>  
	void exclude_set() throws ParseException{
		System.out.println("exclude_set");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;
		
		while(ahead!=null){
			if(ahead.getType() == RegexTokenType.TOKEN_UP){
				token = ahead;
				// Add to Tree
				match(RegexTokenType.LEX_UP);
				char_set();
				
				// Add to Tree
				match(RegexTokenType.LEX_R_BRACKET);
				
				// Add to Tree
				match(RegexTokenType.LEX_IN);
				
				exclude_set_tail();	
				
			}else {
				break;
			}
		}
		
		System.out.println("exclude_set ENDS");
		
	}
	
	// <exclude-set-tail> -> [<char-set>]  | <defined-class>
	void exclude_set_tail() throws ParseException{
		System.out.println("exclude_set_tail");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		Token token;

		if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET){		
			token = ahead;
			match(RegexTokenType.LEX_L_BRACKET);			
			char_set();
			match(RegexTokenType.LEX_R_BRACKET);
				
		}else { // defined_class
			token = ahead;
			match((String) ahead.getValue()); 
			// Add to Tree
			System.out.println("Saved Here: " + token.getValue());	
		}
		
		System.out.println("exclude_set_tail ENDS");
		match(null);

	}

	public Parser() {
	    te = new Tokenizer(Arrays.asList((TokenType[])RegexTokenType.values()));
	}

	public Tree parse(String input) throws ParseException {
	    te.setInput(input); 

	    match(null);

	    if(ahead == null) {
	      throw new ParseException();
	    }

	    tokenTree = new Tree<String>((String)ahead.getValue());

	    rexp();

	    if(ahead != null) {
	      throw new ParseException();
	    }

	    return tokenTree;
	}
	
}

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

	// <rexp> -> <rexp1> <rexp¡¯>
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
	
	// <rexp1> -> <rexp2> <rexp1¡¯> | E
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
	
	// rexpPrime -> UNION <rexp1> <rexp¡¯> | E
	void rexpPrime() throws ParseException{
		System.out.println("rexpPrime");
		
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());

			if(ahead.getType() == RegexTokenType.TOKEN_UNION){
				// Add to Tree
		    	match(RegexTokenType.LEX_UNION);
		    	rexp1();
		    		
			}else{
				System.out.println("rexpPrime ENDS");
				return;
			}
		}
		
	}
	
	// <rexp1¡¯> -> <rexp2> <rexp1¡¯>  | E        
	void rexp1Prime() throws ParseException{
		System.out.println("rexp1Prime");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		rexp2();
		
		System.out.println("rexp1Prime ENDS");
		return;
	}
	
	// <rexp2> -> (<rexp>) <rexp2-tail>  | RE_CHAR <rexp2-tail> | <rexp3>
	void rexp2() throws ParseException{
		System.out.println("rexp2");
		
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		
			if(ahead.getType() == RegexTokenType.TOKEN_L_PAREN){
				// Add to Tree
				match(RegexTokenType.LEX_L_PAREN);
		    	rexp();
		    	// Add to Tree
		    	if(ahead!=null)
		    		match(RegexTokenType.LEX_R_PAREN);
		    	rexp2_tail();
		    
			}else if(ahead.getType() == RegexTokenType.RE_CHAR){
				// Add to Tree
				match((String)ahead.getValue()); // RE_CHAR
				rexp2_tail();
			
			}else{
				rexp3();
			}			
		
		}
		
		System.out.println("rexp2 ENDS");
		return;
	}
	
	// <rexp2-tail> -> * | + | E
	void rexp2_tail() throws ParseException{		
		System.out.println("rexp2_tail");
		
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		
			if(ahead.getType() == RegexTokenType.TOKEN_ASTE || ahead.getType() == RegexTokenType.TOKEN_PLUS){
				// Add to Tree
				match((String)ahead.getValue());
			}else{
				System.out.println("rexp2_tail ENDS");
				return;
			}	
		}
		
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
		return;
	}
	
	// <char-class> -> .  |  [ <char-class1>  | <defined-class>
	void char_class() throws ParseException{
		System.out.println("char_class");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		if(ahead.getType() == RegexTokenType.TOKEN_DOT){
			// Add to Tree
			match(RegexTokenType.LEX_DOT);
		
		}else if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET){
			// Add to Tree
			match(RegexTokenType.LEX_L_BRACKET);
			char_class1();	

		}else { // defined_class
			// Add to Tree
			match((String) ahead.getValue());
			
		}
		
		System.out.println("char_class ENDS");
		return;
	}
	
	// ** <char-class1> -> <char-set-list> | <exclude-set>
	void char_class1() throws ParseException{
		System.out.println("char_class1");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		if(ahead.getType() == RegexTokenType.TOKEN_UP){
			exclude_set();		
			
		}else {
			char_set_list();
		}

		System.out.println("char_class1 ENDS");
	}
	
	// <char-set-list> -> <char-set> <char-set-list> | ]
	void char_set_list() throws ParseException{
		System.out.println("char_set_list");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		if(ahead.getType() == RegexTokenType.TOKEN_R_BRACKET){
			// Add to Tree	
			match(RegexTokenType.LEX_R_BRACKET);
				
		}else{
			char_set();
		}
		
		System.out.println("char_set_list ENDS");
		return;

	}
	
	// <char-set> -> CLS_CHAR <char-set-tail> 
	void char_set() throws ParseException{
		System.out.println("char_set");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}
		
		if(ahead.getType() == RegexTokenType.CLS_CHAR){
			// Add to Tree
			match((String)ahead.getValue()); // CLS_CHAR
			char_set_tail();
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

		if(ahead.getType() == RegexTokenType.TOKEN_DASH){
			// Add to Tree
			match(RegexTokenType.LEX_DASH);
			// Add to Tree
			match((String)ahead.getValue()); // CLS_CHAR
				
		}else{
			System.out.println("char_set_tail ENDS");
			return;
		}

	}
	
	// <exclude-set> -> ^ <char-set>] IN <exclude-set-tail>  
	void exclude_set() throws ParseException{
		System.out.println("exclude_set");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}

		// Add to Tree
		match(RegexTokenType.LEX_UP);
			
		char_set();
				
		// Add to Tree
		match(RegexTokenType.LEX_R_BRACKET);
				
		// Add to Tree
		match(RegexTokenType.LEX_IN);
				
		exclude_set_tail();	
		
		System.out.println("exclude_set ENDS");		
	}
	
	// <exclude-set-tail> -> [<char-set>]  | <defined-class>
	void exclude_set_tail() throws ParseException{
		System.out.println("exclude_set_tail");
		if(ahead!=null){
			System.out.println("ahead type: " + ahead.getType());
			System.out.println("ahead value: " + ahead.getValue());
		}

		if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET){		
			// Add to Tree
			match(RegexTokenType.LEX_L_BRACKET);			
			char_set();
			match(RegexTokenType.LEX_R_BRACKET);
				
		}else { // defined_class
			// Add to Tree
			match((String) ahead.getValue()); 
		}
		
		System.out.println("exclude_set_tail ENDS");

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

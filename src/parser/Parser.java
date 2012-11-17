package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;


/**
 * 
 */
public final class Parser {

	Tokenizer te;
	transient Stack<String> tokenStack;
	transient Token ahead;
	
	boolean DEBUG = true;
	boolean char_class = true;
	boolean scope = false;
	
	void match(String token) throws ParseException {	
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
	void rexp() throws ParseException {		
		rexp1();
		rexpPrime();

	}
	
	// <rexp1> -> <rexp2> <rexp1¡¯> | E
	void rexp1() throws ParseException {		
		rexp2();
		rexp1Prime();
		
	}
	
	// rexpPrime -> UNION <rexp1> <rexp¡¯> | E
	void rexpPrime() throws ParseException {
		
		if(ahead!=null) {
			
			if(ahead.getType() == RegexTokenType.TOKEN_UNION) {				
				if(DEBUG) {
					System.out.println("UNION found. Not in char_class.");
				}
				
				char_class = false;
				if(DEBUG)
					System.out.println(RegexTokenType.LEX_UNION);
				tokenStack.push(RegexTokenType.LEX_UNION);
			    match(RegexTokenType.LEX_UNION); 
			    rexp1();
			    rexpPrime();
			    		
			}else {
				return;
			}
			
		}
		
	}
	
	// <rexp1¡¯> -> <rexp2> <rexp1¡¯>  | E        
	void rexp1Prime() throws ParseException {		
		if(ahead!=null && (ahead.getType() == RegexTokenType.TOKEN_L_PAREN || ahead.getType() == RegexTokenType.TOKEN_DOT || ahead.getType() == RegexTokenType.TOKEN_L_BRACKET || ahead.getType() == RegexTokenType.TOKEN_DEFINED)) {
			rexp2();
			rexp1Prime();		
		
		} else if(ahead!=null && ahead.getType() == RegexTokenType.TOKEN_LITERAL) {
			boolean flag = check_valid(ahead, RE_CHAR);
			if(!flag){
				return;
			}
			rexp2();
			rexp1Prime();	
		
		} else {
			return;
		}
		
	}
	
	// <rexp2> -> (<rexp>) <rexp2-tail>  | RE_CHAR <rexp2-tail> | <rexp3>
	void rexp2() throws ParseException {
		
		if(ahead!=null) {
		
			if(ahead.getType() == RegexTokenType.TOKEN_L_PAREN) {
				if(DEBUG) {
					System.out.println("L_PAREN found. Not in a char_class.");
				}
	
				char_class = false;
				if(DEBUG)
					System.out.println(RegexTokenType.LEX_L_PAREN);
				tokenStack.push(RegexTokenType.LEX_L_PAREN);
				match(RegexTokenType.LEX_L_PAREN); 
				
				scope = true;
				tokenStack.push("SCOPE_IN");
			    rexp();
			    tokenStack.push("SCOPE_OUT");
			    
			    if(DEBUG)
			    	System.out.println(RegexTokenType.LEX_R_PAREN);
			    tokenStack.push(RegexTokenType.LEX_R_PAREN);
			    match(RegexTokenType.LEX_R_PAREN); 
			    
				if(DEBUG) {
					System.out.println("PAREN successfully matched. Scope in.");
				}
			    
				rexp2_tail();
			    
			}else if(ahead.getType() == RegexTokenType.TOKEN_LITERAL) {
				if(DEBUG) {
					System.out.println("LITERAL found. Not in a char_class.");
				}
				
				char_class = false;
				
				boolean flag = check_valid(ahead, RE_CHAR);
				if(!flag) {
					throw new ParseException();
				}
				if(DEBUG)
					System.out.println((String)ahead.getValue());
				tokenStack.push((String)ahead.getValue());
				match((String)ahead.getValue()); 
				
				// ADD CONCAT HERE*
				
				rexp2_tail();
				
			} else {
				rexp3();
			}
		
		}

	}
	
	// <rexp2-tail> -> * | + | E
	void rexp2_tail() throws ParseException {		

		if(ahead.getType() == RegexTokenType.TOKEN_ASTE) {
			if(DEBUG) {
				System.out.println(" * Found. Not in a char_class.");
			}

			char_class = false;
			if(DEBUG)
				System.out.println(RegexTokenType.LEX_ASTE);
			tokenStack.push(RegexTokenType.LEX_ASTE);
			match(RegexTokenType.LEX_ASTE); 
			
			if(scope) {
				scope = false;
				// Finalize Tree
			}else {
				
			}
				
		}else if(ahead.getType() == RegexTokenType.TOKEN_PLUS) {
			if(DEBUG) {
				System.out.println(" * Found. Not in a char_class.");
			}
			
			// Add to Tree
			
			char_class = false;
			if(DEBUG)
				System.out.println(RegexTokenType.LEX_PLUS);
			tokenStack.push(RegexTokenType.LEX_PLUS);
			match(RegexTokenType.LEX_PLUS);
			
			if(scope) {
				scope = false;
				// Finalize Tree
			}else {
				
			}
			
		}else {
				return;
			
		}
	}
	
	// <rexp3> -> <char-class>  | E   
	void rexp3() throws ParseException {
		if(ahead.getType() == RegexTokenType.TOKEN_DOT || ahead.getType() == RegexTokenType.TOKEN_L_BRACKET || ahead.getType() == RegexTokenType.TOKEN_DEFINED) {
			char_class();
		
		} else {
			return;
			
		}
	}
	
	// <char-class> -> .  |  [ <char-class1>  | <defined-class>
	void char_class() throws ParseException {
		
		if(ahead.getType() == RegexTokenType.TOKEN_DOT) {
			if(DEBUG)
				System.out.println(RegexTokenType.LEX_DOT);
			tokenStack.push(RegexTokenType.LEX_DOT);
			match(RegexTokenType.LEX_DOT);
		
		}else if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET) {
			if(DEBUG)
				System.out.println(RegexTokenType.LEX_L_BRACKET);
			tokenStack.push(RegexTokenType.LEX_L_BRACKET);
			match(RegexTokenType.LEX_L_BRACKET);
			char_class1();	

		}else { // defined_class
			if(DEBUG)
				System.out.println((String) ahead.getValue());
			tokenStack.push((String) ahead.getValue());
			match((String) ahead.getValue());
			defined_class(ahead, false);
		}
		
		return;
	}
	
	// ** <char-class1> -> <char-set-list> | <exclude-set>
	void char_class1() throws ParseException {
		ArrayList<Character> range;
		
		if(ahead.getType() == RegexTokenType.TOKEN_UP) {
			range = exclude_set(new ArrayList<Character>());		
			
		}else {
			range = char_set_list(new ArrayList<Character>());
		}
	}
	
	// <char-set-list> -> <char-set> <char-set-list> | ]
	ArrayList<Character> char_set_list(ArrayList<Character> range) throws ParseException {
		if(ahead.getType() == RegexTokenType.TOKEN_LITERAL || ahead.getType() == RegexTokenType.TOKEN_DOT) {
			
			boolean flag = check_valid(ahead, CLS_CHAR);
			if(!flag) {
				return range;
			}
			range = char_set(range);
			return char_set_list(range);
			
		}else { 
			return range;
		}

	}
	
	// <char-set> -> CLS_CHAR <char-set-tail> 
	ArrayList<Character> char_set(ArrayList<Character> range) throws ParseException {	
		if(DEBUG)
			System.out.println((String)ahead.getValue());
		tokenStack.push((String) ahead.getValue());
		match((String)ahead.getValue());
		
		if(!check_valid(ahead, CLS_CHAR)) {
			throw new ParseException();
        }
               
        return char_set_tail(ahead, range);
	}
	
	// <char-set-tail> -> - CLS_CHAR | E
	ArrayList<Character> char_set_tail(Token start, ArrayList<Character> range) throws ParseException {		
		if(ahead.getType() == RegexTokenType.TOKEN_DASH) {
			if(DEBUG)
				System.out.println(RegexTokenType.LEX_DASH);
			tokenStack.push(RegexTokenType.LEX_DASH);
            match(RegexTokenType.LEX_DASH);
            match((String)ahead.getValue());
            
            Token end = ahead; // HOW TO ADD RANGE?*
            if(DEBUG)
            	System.out.println((String)end.getValue());
            
            if(!check_valid(end, CLS_CHAR)) {
                    throw new ParseException();
            }
    
            int start_index;
            int end_index;
            if(((String)start.getValue()).charAt(0) == '\\') {
            	start_index = ((int)((String)start.getValue()).charAt(1)) - 32;
            }else {
                start_index = ((int)((String)start.getValue()).charAt(0)) - 32;
            }
            
            if(((String)end.getValue()).charAt(0) == '\\') {
                end_index = ((int)((String)end.getValue()).charAt(1)) -32;
            } else {
            	end_index = ((int)((String)end.getValue()).charAt(0)) - 32;
            }
            
            int current_index = start_index;
            while(current_index <= end_index) {
            	range.add(((char)(current_index + 32)));
                current_index++;
            }
        
            return range;
		}
		else {
			if(((String)start.getValue()).charAt(0) == '\\') {
				range.add(((String)start.getValue()).charAt(1));
            }else {
                range.add(((String)start.getValue()).charAt(0));
            }
            
			return range;
		}
		
	}
	
	// <exclude-set> -> ^ <char-set>] IN <exclude-set-tail>  
	 ArrayList<Character> exclude_set(ArrayList<Character> range) throws ParseException {
		 if(DEBUG)
			 System.out.println(RegexTokenType.LEX_UP);
		 tokenStack.push(RegexTokenType.LEX_UP);
         match(RegexTokenType.LEX_UP); 
         ArrayList<Character> exclude = char_set(new ArrayList<Character>());
         
         if(DEBUG)
        	 System.out.println(RegexTokenType.LEX_R_BRACKET);
         tokenStack.push(RegexTokenType.LEX_R_BRACKET);
         match(RegexTokenType.LEX_R_BRACKET); 
         tokenStack.push("SCOPE_OUT");        
         
         if(DEBUG)
        	 System.out.println(RegexTokenType.LEX_IN);
         tokenStack.push(RegexTokenType.LEX_IN);
         match(RegexTokenType.LEX_IN);
         ArrayList<Character> in = exclude_set_tail();
         
         for(int i = 0; i < in.size(); i++) {
        	 if(!exclude.contains(in.get(i))) {
        		 range.add(in.get(i));
             }
         }
         	
         return range;
	}
	
	// <exclude-set-tail> -> [<char-set>]  | <defined-class>
	 ArrayList<Character> exclude_set_tail() throws ParseException {
		 if(ahead.getType() == RegexTokenType.TOKEN_L_BRACKET) {
			 if(DEBUG)
				 System.out.println(RegexTokenType.LEX_L_BRACKET);
			 tokenStack.push(RegexTokenType.LEX_L_BRACKET);
			 match(RegexTokenType.LEX_L_BRACKET);
			 tokenStack.push("SCOPE_OUT");
             ArrayList<Character> range = char_set(new ArrayList<Character>());
             if(DEBUG) 
            	 System.out.println(RegexTokenType.LEX_R_BRACKET);
             tokenStack.push(RegexTokenType.LEX_R_BRACKET);
             match(RegexTokenType.LEX_R_BRACKET);
             return range;
         }else {
        	 if(DEBUG)
        		 System.out.println((String)ahead.getValue());
        	 tokenStack.push((String)ahead.getValue());
        	 match((String)ahead.getValue());
             ArrayList<Character> range = defined_class(ahead, true);
             return range;
         }

	}
	 
     ArrayList<Character> defined_class(Token token, boolean exclude) throws ParseException {
         
         return null;
         
     }
     
     boolean check_valid(Token token, String[] set) {
    	 for(int i = 0; i < set.length; i++) {
    		 if(set[i].equals(token.getValue())) {
    			 return true;
             }
         }
         return false;
     }
     
     private static final String[] RE_CHAR = {
                     "\\ ", "!", "\\\"", "#", "$", "%", "&", "\\\'", "\\(", "\\)", "\\*", "\\+", ",", "-", "\\.", "/",
                     "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\?", 
                     "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
                     "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "^", "_", 
                     "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
                     "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "\\|", "}", "~"             
     };
     
     private static final String[] CLS_CHAR = {
                     " ", "!", "\"", "#", "$", "%", "&", "\'", "(", ")", "*", "+", ",", "\\-", ".", "/",
                     "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", 
                     "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
                     "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\\[", "\\\\", "\\]", "\\^", "_", 
                     "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", 
                     "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~"               
     };
	
	public Parser() {
	    te = new Tokenizer(Arrays.asList((TokenType[])RegexTokenType.values()));
	}

	public Stack<String> parse(String input) throws ParseException {
	    te.setInput(input); 

	    match(null);

	    if(ahead == null) {
	      throw new ParseException();
	    }

	    tokenStack = new Stack<String> ();

	    rexp();

	    if(ahead != null) {
	      throw new ParseException();
	    }

	    return tokenStack;
	}
	
}

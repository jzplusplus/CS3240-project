package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
//import java.util.Stack;
import java.util.Scanner;
import java.util.Stack;

public class ParserTester {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException{
		
		String filepath = "test_spec.txt";
		boolean DEBUG = true;
		ArrayList<Stack<String>> stacks = new ArrayList<Stack<String>>();
		ArrayList<String> ids = new ArrayList<String>();
		
		 System.out.println("---------- Recursive Descent Parsing Began... ");
	        
	        Tokenizer input = new Tokenizer(new Scanner(new File(filepath)));        
	        do {
	                Token id = input.getNextToken();
	                if(id.getType() != TokenType.EOL) {
	                	if(DEBUG)
	                        System.out.println("----- Identifier found: " + id.getValue() + "...");
	                    if(id.getType() != TokenType.DEFINED) {
	                    	throw new ParseException("Invalid syntax.");
	                    }                  
	                    ids.add((String) id.getValue());
	                    Parser ps = new Parser(input, DEBUG);
	                    stacks.add(ps.getTokenStack());
	                }
	        } while(input.gotoNextLine());
	        
	        System.out.println("---------- Parsing Succesfully Done...");

	        if(DEBUG) {
	        	int i = 0;
		        for(Stack<String> s: stacks){
		        	System.out.println("---------- Found a New Stack: " + ids.get(i));
		            while ( !s.empty() ) {
		            	System.out.print ( s.pop() );
		                System.out.print ( ',' );
		            }
		            System.out.println();
		            i++;
		        }		        
		       
	        }        
		
	}
	
}	

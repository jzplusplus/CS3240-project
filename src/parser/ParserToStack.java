package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class ParserToStack {
	private ArrayList<Stack<String>> stacks;
	private ArrayList<ArrayList<String>> qs;
	private ArrayList<String> ids;
	
	public ParserToStack(String filepath, boolean DEBUG) throws FileNotFoundException, ParseException{
		stacks = new ArrayList<Stack<String>>();
		qs = new ArrayList<ArrayList<String>>();
		ids = new ArrayList<String>();
		
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
                    Parser ps = new Parser(input, new Stack<String>(), new ArrayList<String>(), DEBUG);
                    stacks.add(ps.getTokenStack());
                    qs.add(ps.getTokenQ());
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
	        
	        i = 0;
	        for(ArrayList<String> q: qs){
		        System.out.println("---------- Found a New Q: " + ids.get(i));
	        	for(String str: q){
	        		System.out.print(str);
	        		System.out.print(",");
	        	}
	        	System.out.println();
	        	i++;
	        }	        
        }        
	}
	
	public ArrayList<ArrayList<String>> getQs(){
		return qs;
	}
	
	public ArrayList<Stack<String>> getStacks(){
		return stacks;
	}

	public ArrayList<String> getIds(){
		return ids;
	}
	
}

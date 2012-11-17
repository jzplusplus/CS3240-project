package parser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Stack;

public class ParserTester {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException{
			
		ParserToStack pts = new ParserToStack("sample_spec.txt", true);
		ArrayList<Stack<String>> stacks = pts.getStacks();
		ArrayList<String> ids = pts.getIds();
		
		
		
	}
	
}	

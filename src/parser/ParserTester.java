package parser;

import java.util.ArrayList;
import java.util.Stack;

public class ParserTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<Stack> stacks = new ArrayList<Stack>();
		 
		// Input from a text file
		//FileToInput fti = new FileToInput("sample_spec.txt");
		//ArrayList<String> stream = fti.getInputs();
		
		// Parsing a list of Strings
		Parser ps;		
		try {
			ps = new Parser();
			
			String[] inputs = {" [^A-Z] "};
			
			for(String expression : inputs) {
				System.out.println("----------------------------- Parsing began ");				
				stacks.add(ps.parse(expression)); // Add Tree(s)
			}
			
			Stack temp = stacks.get(0);
            while ( !temp.empty() ) {
            	System.out.print ( temp.pop() );
                System.out.print ( ", " );
            }

		} catch(ParseException e) {
			throw new RuntimeException("Parse error.", e);
		}
		
		
	}

}

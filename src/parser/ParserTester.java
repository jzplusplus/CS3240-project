package parser;

import java.util.ArrayList;

public class ParserTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<Tree<String>> trees = new ArrayList<Tree<String>>();
		 
		// Input from a text file
		//FileToInput fti = new FileToInput("sample_spec.txt");
		//ArrayList<String> stream = fti.getInputs();
		
		// Parsing a list of Strings
		Parser ps;		
		try {
			ps = new Parser();
			
			String[] inputs = {" IN * | ( )", "( )*"};
			
			for(String expression : inputs) {
				System.out.println("----------------------------- Parsing began...");				
				trees.add(ps.parse(expression)); // Add Tree(s)
			}

		} catch(ParseException e) {
			throw new RuntimeException("Parse error.", e);
		}
		
		
	}

}

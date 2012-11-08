package parser;

import java.util.LinkedList;

public final class ParserTestMain {

	/**
	 * @param args
	 */
	public static final void main(String[] args) {
		// TODO Auto-generated method stub
		
		Parser parser;
		LinkedList<Object> result;

		//if(args.length < 1) {
		//    System.exit(1);
		//}

		try {
			parser = new Parser();
			
			String[] test = {"10", "1*10 + 20"}; 
			
			for(String expression : test) {
				System.out.println("Parsing began...");
				result = parser.parse(expression);
			    System.out.println("Expression: " + expression);
			    System.out.println();
			    
			}
			
		} catch(ParseException e) {
			throw new RuntimeException("Parse error.", e);
		}
	}
}

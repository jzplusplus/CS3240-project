package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import parser.ParseTree;
import parser.RegexParser;
import exception.ParseException;

public class ParserTester {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParseException{
		
		String filepath = "sample_spec.txt";
		Map<String, ParseTree> definedClasses = new HashMap<String, ParseTree>();
		Map<String, ParseTree> tokenClasses = new HashMap<String, ParseTree>(); 
		try {
			RegexParser.populateParseMaps(filepath, definedClasses, tokenClasses, true);
		} catch (ParseException e) {
    		System.out.println("Caught exception:");
    		System.out.println(e.getMessage());
		}
//        BufferedReader reader = new BufferedReader(new FileReader(filepath));
//        String line;
//        Map<String, ParseTree> definedClasses = new HashMap<String, ParseTree>();
//        Map<String, ParseTree> tokenTrees = new HashMap<String, ParseTree>();
//
//        boolean currentlyParsingDefinedClasses = true;
//        while ((line = reader.readLine()) != null) {
//        	if (currentlyParsingDefinedClasses && (line.isEmpty() || line.replaceAll("\\s", "").isEmpty())) {
//        		System.out.println("done parsing defined classes, moving on to token definitions");
//        		currentlyParsingDefinedClasses = false; // we've moved on to the token definitions
//        	}
//        	if (line.replaceAll("\\s", "").startsWith("%")) {
//        		continue; // it's a comment line
//        	}
//        	String[] tokens = line.split("\\s+", 2);
//        	if (tokens.length != 2) {
//        		continue;
//        	}
//        	System.out.println("Looking at " + tokens[0] + " -->" + tokens[1]);
//        	try {
//        		ParseTree tree = RegexParser.parse(tokens[1], definedClasses);
//        		if (currentlyParsingDefinedClasses) {
//        			definedClasses.put(tokens[0], tree);
//        		} else {
//        			tokenTrees.put(tokens[0], tree);
//        		}
//            	tree.print();
//        	} catch (ParseException e) {
//        		System.out.println("Caught exception while parsing " + tokens[0]);
//        		System.out.println(e.getMessage());
//        	}
//        }

	}
	
}	

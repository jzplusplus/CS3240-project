package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParserTester {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		
		String filepath = "sample_spec.txt";
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line;
        Map<String, ParseTree> classes = new HashMap<String, ParseTree>();
        while ((line = reader.readLine()) != null) {
        	String[] tokens = line.split("\\s+", 2);
        	if (tokens.length != 2) {
        		continue;
        	}
        	System.out.println("Looking at " + tokens[0] + " -->" + tokens[1]);
        	try {
        		ParseTree tree = RegexParser.parse(tokens[1], classes);
            	classes.put(tokens[0], tree);
            	tree.print();
        	} catch (ParseException e) {
        		System.out.println("Caught exception while parsing " + tokens[0]);
        		System.out.println(e.getMessage());
        	}
        }
//		boolean DEBUG = true;
//		
//		ParserToStack pts = new ParserToStack(filepath, DEBUG);
//		ArrayList<Stack<String>> stacks = pts.getStacks();
//		ArrayList<String> ids = pts.getIds();
//		
//		StackToTree stt = new StackToTree(stacks, ids);
//		ArrayList<Tree<String>> trees = stt.getTrees();
//		
//		for(Tree<String> tree: trees) {
//			//stt.printTree(tree.getRoot());
//		}
	}
	
}	

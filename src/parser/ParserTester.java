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
		
		String filepath = "test_spec.txt";
		boolean DEBUG = true;
		
		ParserToStack pts = new ParserToStack(filepath, DEBUG);
		ArrayList<Stack<String>> stacks = pts.getStacks();
		ArrayList<String> ids = pts.getIds();
		
		StackToTree stt = new StackToTree(stacks, ids);
		ArrayList<Tree<String>> trees = stt.getTrees();
		
		for(Tree<String> tree: trees) {
			//stt.printTree(tree.getRoot());
		}
	}
	
}	

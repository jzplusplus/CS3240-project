package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;
import parser.LL1ParserGenerator;

public class LL1ParserGeneratorTest {

	/**
	 * @param args
	 * @throws UndefinedNonterminalException 
	 * @throws IncorrectRuleFormatException 
	 * @throws MultipleStartSymbolException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		// grammar specs
		String testG1 ="test_grammar1_ll1.txt";
		String testG2 ="test_grammar2_ll1.txt";
		String testG3 ="test_grammar3_ll1.txt";

		// token specs
		String testT1 ="test_token1_ll1.txt";
		String testT2 ="test_token2_ll1.txt";
		String testT3 ="test_token3_ll1.txt";
		
		LL1ParserGenerator test1 = new LL1ParserGenerator(testG1, testT1);
		LL1ParserGenerator test2 = new LL1ParserGenerator(testG2, testT2);
		LL1ParserGenerator test3 = new LL1ParserGenerator(testG3, testT3);
		
		System.out.println("Test Case 1:\n");
		System.out.println("First Sets");
		System.out.println(test1.printFirstSets());
		System.out.println("\nFollow Sets");
		System.out.println(test1.printFirstSets());
		System.out.println("\nParsing Table");
		System.out.println(test1.toString());
		System.out.println("\n\n");
		
		System.out.println("Test Case 2:\n");
		System.out.println("First Sets");
		System.out.println(test2.printFirstSets());
		System.out.println("\nFollow Sets");
		System.out.println(test2.printFirstSets());
		System.out.println("\nParsing Table");
		System.out.println(test2.toString());
		System.out.println("\n\n");
		
		System.out.println("Test Case 3:\n");
		System.out.println("First Sets");
		System.out.println(test3.printFirstSets());
		System.out.println("\nFollow Sets");
		System.out.println(test3.printFirstSets());
		System.out.println("\nParsing Table");
		System.out.println(test3.toString());
		System.out.println("\n\n");


		
	}

}

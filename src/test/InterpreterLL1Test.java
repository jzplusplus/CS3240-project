package test;

import java.io.IOException;

import exception.IncorrectRuleFormatException;
import exception.InputRuleMismatchException;
import exception.InvalidProgramException;
import exception.InvalidTokenException;
import exception.MultipleStartSymbolException;
import exception.ParseException;
import exception.RuleApplicabilityException;
import exception.UndefinedNonterminalException;

import base.InterpreterLL1;

public class InterpreterLL1Test {

	/**
	 * @param args
	 * @throws InvalidProgramException 
	 * @throws InvalidTokenException 
	 * @throws RuleApplicabilityException 
	 * @throws InputRuleMismatchException 
	 * @throws UndefinedNonterminalException 
	 * @throws IncorrectRuleFormatException 
	 * @throws MultipleStartSymbolException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParseException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException, InputRuleMismatchException, RuleApplicabilityException, InvalidTokenException, InvalidProgramException {
		InterpreterLL1 test1 = new InterpreterLL1("minire_test_script.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 test2 = new InterpreterLL1("minire_test_script2.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 test3 = new InterpreterLL1("minire_test_script3.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 test4 = new InterpreterLL1("minire_test_script4.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 test5 = new InterpreterLL1("minire_test_script5.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 test6 = new InterpreterLL1("minire_test_script6.txt", "grammar.txt");
		System.out.println();
		InterpreterLL1 testMaxFreq = new InterpreterLL1("maxFreqTest.txt", "grammar.txt");
				
	}

}

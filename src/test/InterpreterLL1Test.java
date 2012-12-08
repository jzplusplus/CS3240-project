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
		InterpreterLL1 miniRE = new InterpreterLL1("minire_test_script2.txt");
		

	}

}

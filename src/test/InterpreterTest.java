package test;

import java.io.IOException;

import exception.ParseException;
import base.Interpreter;

public class InterpreterTest {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		// test file: mentor whoopers filament Mentor argument argumentative tournamnt Tournament games  
		Interpreter i = new Interpreter(
				"begin" +
				"foo = find '(mentor | Mentor)' in \"file1.txt\";" +
				"bar = find '([a-z])+' in \"file1.txt\";" +
				"replace '[aeiou]' with \"oo\" in \"file1.txt\" >! \"foobar.txt\";" +
				"recursivereplace 'oo' with \"o\" in \"foobar.txt\" >! \"baz.txt\";" + 
				"end");
	} // failed on [A-Z](a-z)+. I think the problem is in DFA

}

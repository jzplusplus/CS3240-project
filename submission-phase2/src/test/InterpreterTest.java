package test;

import java.io.File;
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
		if (args.length != 2) {
			System.out.println("Usage: java test.InterpreterTest script.txt path/to/test/folder/");
			return;
		}
		
		String prefix = args[1];
		if(!prefix.endsWith("/")) {
			prefix += "/";
		}
		
		Interpreter test = new Interpreter(new File(args[0]), prefix);

		
	} // failed on [A-Z](a-z)+. I think the problem is in DFA

}

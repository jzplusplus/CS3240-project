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
		
		/*Interpreter i = new Interpreter(
				"begin" +
				"foo = find '(mentor | Mentor)' in \"file1.txt\";" +
				"bar = find '[A-Z]([a-z])+' in \"file1.txt\";" +
				"replace '[aeiou]' with \"oo\" in \"file1.txt\" >! \"foobar.txt\";" +
				"recursivereplace 'oo' with \"o\" in \"foobar.txt\" >! \"baz.txt\";" + 
				"end");*/
		
		/*Interpreter i2 = new Interpreter(
				"begin" +
				"foo = find '(mentor | Mentor)' in \"file1.txt\" union find '(filament)' in \"file1.txt\";" +
				"bar = find '(mentor | Mentor)' in \"file1.txt\" diff find '(Mentor | filament)' in \"file1.txt\";" +
				"baz = find '(mentor | Mentor)' in \"file1.txt\" inters find '(balloons)' in \"file1.txt\";" +
				"end");*/
		
		/*Interpreter i3 = new Interpreter(
				"begin" +
				"replace '[aeiou]' with \"oo\" in \"file1.txt\" >! \"foobar.txt\";" +
				"foo = find '(oon)' in \"foobar.txt\" union find '(oor)' in \"foobar.txt\" union find '(oos)' in \"foobar.txt\";" +
				"bar = maxfreqstring(foo);" +
				"end");*/
		
		// **it somehow gives "exception.ParseException: Error matching string: couldn't find find"**
		Interpreter i4 = new Interpreter( 
			"begin" + 
			"matches = find '[A-Z]' in \"long_file.txt\" inters find '[A-Z]' in \"longer_file.txt\";" + 
			"print (matches);" +
			"replace '[0-9]' with \"REPLACED\" in \"long_file.txt\" >! \"long_output2.txt\";" +    
			"end");

		
	} // failed on [A-Z](a-z)+. I think the problem is in DFA

}

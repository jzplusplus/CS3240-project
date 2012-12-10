package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import parser.MiniReParser;
import parser.ParseTree;
import parser.ParserUtils;
import exception.ParseException;

public class MiniReParserTest {
	public static void main(String[] args) throws IOException, ParseException {
		
		
//		String tester = "      a     as99df    d ";
//		PushbackReader testerReader = new PushbackReader(new StringReader(tester), tester.length());
//		System.out.println(tester);
//		String id = MiniReParser.peekId(testerReader);
//		System.out.println(id);
//		ParserUtils.consumeSequence(testerReader, id);
//		id = MiniReParser.peekId(testerReader);
//		System.out.println(id);

		
		String filename = "minire_test_script.txt";
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String script = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			script += " " + line;
		}
		
		
		ParseTree tree = MiniReParser.parse(script);
		
		tree.print();
	}
}

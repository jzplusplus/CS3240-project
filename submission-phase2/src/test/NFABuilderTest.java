package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.ParseTree;
import parser.RegexParser;
import parser.Token;
import util.NFA;
import base.NFABuilder;
import base.NFAScanner;
import exception.BadTokenException;
import exception.ParseException;

public class NFABuilderTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws BadTokenException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, BadTokenException, ParseException {
		String filepath = "sample_spec.txt";
        Map<String, ParseTree> classTrees = new HashMap<String, ParseTree>();
        Map<String, ParseTree> tokenTrees = new HashMap<String, ParseTree>();
        RegexParser.populateParseMaps(filepath, classTrees, tokenTrees, true);
        
        NFABuilder nfaBuilder = new NFABuilder(classTrees, true);
        Map<String, NFA> nfas = nfaBuilder.build(tokenTrees, true);
        
        
        NFAScanner scanner = new NFAScanner(nfas);
        String input = "ghghghghg    44.123123 a * b 4 + 5 a = 123";
        
        System.out.println("parsing: " + input);
        List<Token> tokens = scanner.tokenize(input);
        for (Token token : tokens) {
        	System.out.println(token.getType().toString() + " -> " + token.getValue().toString());
        }
        
        System.out.println("Looking at sample_input.txt:");
        File sampleInput = new File("sample_input.txt");
        BufferedReader sampleInputReader = new BufferedReader(new FileReader(sampleInput));
        String line;
        while ((line = sampleInputReader.readLine()) != null) {
            System.out.println("parsing: " + line);
            tokens = scanner.tokenize(line);
            for (Token token : tokens) {
            	System.out.println(token.getType().toString() + " -> " + token.getValue().toString());
            }
        }
	}

}

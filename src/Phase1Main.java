

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import parser.ParseTree;
import parser.RegexParser;
import parser.Token;
import util.DFA;
import util.NFA;
import base.NFABuilder;
import base.TableWalker;
import exception.BadTokenException;
import exception.EOFException;
import exception.ParseException;

public class Phase1Main {

	
	public static void main(String[] args) throws IOException{
		if (args.length != 2) {
			System.out.println("Usage: java base.Main spec.txt input.txt");
			return;
		}
		String specPath = args[0];
		String inputPath = args[1];
		Map<String, ParseTree> definedClasses = new HashMap<String, ParseTree>();
		Map<String, ParseTree> tokenClasses = new HashMap<String, ParseTree>(); 
		try {
			RegexParser.populateParseMaps(specPath, definedClasses, tokenClasses);
		} catch (ParseException e) {
			System.out.println("Caught exception while parsing spec:");
			System.out.println(e.getMessage());
			return;
		}
		NFABuilder nfaBuilder = new NFABuilder(definedClasses);
		Map<String, NFA> tokenNfas = nfaBuilder.build(tokenClasses);
		Map<String, DFA> tokenDfas = new HashMap<String, DFA>();
		for (String tokenName : tokenNfas.keySet()) {
			NFA tokenNfa = tokenNfas.get(tokenName);
			DFA tokenDfa = new DFA(tokenNfa, tokenNfa.getStartState());
			tokenDfas.put(tokenName, tokenDfa);
		}
		TableWalker walker = new TableWalker(inputPath, tokenDfas);
		BufferedWriter writer = new BufferedWriter(new FileWriter("output-" + System.currentTimeMillis() + ".txt"));
		try {
			while (true) {
				try {
					Token nextToken = walker.nextToken();
					writer.write(nextToken.getType());
					writer.write(" ");
					writer.write(nextToken.getValue());
					writer.newLine();
					System.out.println(nextToken.getType() + " " + nextToken.getValue());
				} catch (BadTokenException e) {
					System.out.println("Found invalid token! Exiting.");
					return;
				}
			}
		} catch (EOFException e) {
			System.out.println("Reached end of file.");
		} finally {
			writer.close();
		}
	}
}

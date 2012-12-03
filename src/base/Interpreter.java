package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.MiniReParser;
import parser.ParseTree;
import parser.Symbol.NonterminalMiniReSymbol;
import parser.Symbol.ReservedWord;
import exception.ParseException;

public class Interpreter {

	private ParseTree scriptRoot;
	
	// the "memory" we'll be using to store our variables:
	// maps from an identifier to a value.
	private Map<String, List<String>> stringListVars;
	private Map<String, Integer> intVars;
	
	public Interpreter(File scriptHandle) throws IOException, ParseException {
		this(readFile(scriptHandle));
	}
	
	public Interpreter(String script) throws IOException, ParseException {
		stringListVars = new HashMap<String, List<String>>();
		intVars = new HashMap<String, Integer>();
		
		scriptRoot = MiniReParser.parse(script);
		run(scriptRoot);
	}
	
	private void assignStringList(String id, List<String> value) {
		// first make sure it's not in the integer table:
		if (intVars.containsKey(id)) {
			// remove it
			intVars.remove(id);
		}
		stringListVars.put(id, value);
	}
	
	private List<String> getStringList(String id) { // TODO maybe throw exception on invalid key
		return stringListVars.get(id);
	}
	
	private void assignInteger(String id, Integer value) {
		// first make sure it's not in the string list table:
		if (stringListVars.containsKey(id)) {
			// remove it
			stringListVars.remove(id);
		}
		intVars.put(id, value);
	}
	
	private Integer getInteger(String id) { // TODO see above
		return intVars.get(id);
	}
	
	private void run(ParseTree node) {
		if (node.getValue().isTerminal()) {
			throw new RuntimeException("Interpreter: should be handling terminal nodes elsewhere!");
		}
		// otherwise, assume the value is one of the MiniRE nonterminals.
		NonterminalMiniReSymbol symbol = (NonterminalMiniReSymbol) node.getValue();
		switch (symbol) {
		// the only thing we'll want to parse and run are STATEMENTs.
		// for everything else, either recurse or handle as part of STATEMENT.

		case MINIRE_PROGRAM:
			// begin <statement-list> end: recurse on children[1]
			run(node.getChild(1));
			break;
		case STATEMENT_LIST:
		case STATEMENT_LIST_TAIL: // we'll handle these both the same way
			// <statement><statement-list-tail>: recurse on both. tail might be null.
			// <statement>
			run(node.getChild(0));
			if (node.getChildren().size() == 2) {
				// <statement-list-tail>
				run(node.getChild(1));
			}
			break;
			
		case STATEMENT:
			// there are only a few kinds of statements:
			// string list assignment
			if (isStringListAssignment(node)) {
				// ID = <exp> ;
				// first, evaluate EXP and then call assignStringList(id, expReturnValue);
				throw new UnsupportedOperationException("TODO: string assignment");

			} else if (isIntegerAssignment(node)) {
				// integer assignment
				// ID = # <exp> ;
				// first, evaluate EXP, then call assignInteger(id, expReturnValue.size())
				throw new UnsupportedOperationException("TODO: integer assignment");
				
			} else if (isMaxFreqAssignment(node)) {
				// maxfreqstring assignment
				// ID1 = maxfreqstring ( ID2 ) ;
				// first, find the maxfreqstring(s) of ID2, then call assignStringList(id1, maxFreqString);
				throw new UnsupportedOperationException("TODO: max freq string assignment");

				
			} else if (isReplace(node)) {
				// replace
				// replace REGEX with ASCII-STR in <file-names> ;
				throw new UnsupportedOperationException("TODO: replace statement");
				
			} else if (isRecursiveReplace(node)) {
				// recursivereplace
				// recursivereplace REGEX with ASCII-STR in <file-names> ;
				throw new UnsupportedOperationException("TODO: recursive replace statement");
			
			} else if (isPrint(node)) {
				// print
				// print ( <exp-list> ) ;
				//  print each of the <exp>s that make up <exp-list>
				printExpList(node.getChild(2));
			}
			
			break;
		
		}
		
	}

	private List<String> evaluateExp(ParseTree expNode) {
		throw new UnsupportedOperationException("TODO: evaluation of exp");
	}
	
	private void printExpList(ParseTree expListNode) {
		throw new UnsupportedOperationException("TODO: print exp list");

	}

	
	private static boolean isStringListAssignment(ParseTree statementNode) {
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		// string list assignments:
		// ID = <exp> ;
		if (statementNode.getChildren().size() != 4) {
			return false;
		}
		return (statementNode.getChild(0).getValue().isTerminal() 
				&& statementNode.getChild(1).getValue() == ReservedWord.ASSIGN
				&& statementNode.getChild(2).getValue() == NonterminalMiniReSymbol.EXP
				&& statementNode.getChild(3).getValue() == ReservedWord.SEMICOLON);
	}
	
	
	private boolean isIntegerAssignment(ParseTree statementNode) {
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		// int assignments:
		// ID = # <exp> ;
		if (statementNode.getChildren().size() != 5) {
			return false;
		}
		return (statementNode.getChild(0).getValue().isTerminal() 
				&& statementNode.getChild(1).getValue() == ReservedWord.ASSIGN
				&& statementNode.getChild(1).getValue() == ReservedWord.INT
				&& statementNode.getChild(3).getValue() == NonterminalMiniReSymbol.EXP
				&& statementNode.getChild(4).getValue() == ReservedWord.SEMICOLON);
	}
	
	private boolean isMaxFreqAssignment(ParseTree statementNode) {
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		// maxfreq assignments:
		// ID = maxfreqstring ( ID ) ;
		if (statementNode.getChildren().size() != 7) {
			return false;
		}
		return (statementNode.getChild(0).getValue().isTerminal() 
				&& statementNode.getChild(1).getValue() == ReservedWord.ASSIGN
				&& statementNode.getChild(1).getValue() == ReservedWord.MAXFREQSTRING
				&& statementNode.getChild(3).getValue().getValue().equals("(")
				&& statementNode.getChild(4).getValue().isTerminal()
				&& statementNode.getChild(5).getValue().getValue().equals(")")
				&& statementNode.getChild(6).getValue() == ReservedWord.SEMICOLON);
	}
	

	private boolean isReplace(ParseTree statementNode) {
		// replace REGEX with ASCII-STR in <file-names> ;
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		return statementNode.getChild(0).getValue() == ReservedWord.REPLACE;
	}
	
	private boolean isRecursiveReplace(ParseTree statementNode) {
		// recursivereplace REGEX with ASCII-STR in <file-names> ;
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		return statementNode.getChild(0).getValue() == ReservedWord.RECURSIVE_REPLACE;
	}
	
	private boolean isPrint(ParseTree statementNode) {
		// print ( <exp-list> ) ;
		if (statementNode.getValue() != NonterminalMiniReSymbol.STATEMENT) {
			return false;
		}
		return statementNode.getChild(0).getValue() == ReservedWord.PRINT;
	}
	
	private static String readFile(File handle) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(handle));
		String script = "";
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			script += " " + line;
		}
		return script;
	}
}

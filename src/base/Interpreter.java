package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
	// DON'T access these directly: instead, use assignStringList / assignInteger, etc.
	private Map<String, List<StringMatch>> stringListVars;
	private Map<String, Integer> intVars;
	
	public Interpreter(File scriptHandle) throws IOException, ParseException {
		this(readFile(scriptHandle));
	}
	
	public Interpreter(String script) throws IOException, ParseException {
		stringListVars = new HashMap<String, List<StringMatch>>();
		intVars = new HashMap<String, Integer>();
		
		scriptRoot = MiniReParser.parse(script);
		run(scriptRoot);
	}
	
	private void assignStringList(String id, List<StringMatch> value) {
		// first make sure it's not in the integer table:
		if (intVars.containsKey(id)) {
			// remove it
			intVars.remove(id);
		}
		stringListVars.put(id, value);
	}
	
	private List<StringMatch> getStringList(String id) { // TODO maybe throw exception on invalid key
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
				String id = node.getChild(0).getValue().getValue();
				List<StringMatch> exp = evaluateExp(node.getChild(2));
				assignStringList(id, exp);

			} else if (isIntegerAssignment(node)) {
				// integer assignment
				// ID = # <exp> ;
				// first, evaluate EXP, then call assignInteger(id, expReturnValue.size())
				String id = node.getChild(0).getValue().getValue();
				List<StringMatch> exp = evaluateExp(node.getChild(3));
				Integer size = exp.size();
				assignInteger(id, size);
				
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

	private List<StringMatch> evaluateExp(ParseTree expNode) {
		if (expNode.getValue() != NonterminalMiniReSymbol.EXP) {
			throw new RuntimeException("Tried to call evaluateExp on a non-EXP node");
		}
		// possible production rules:
		// <exp> -> ID
		// <exp> -> ( <exp> )
		// <exp> -> <term> <exp-tail>
		
		if (expNode.getChildren().size() == 1 && expNode.getChild(0).getValue().isTerminal()) {
			// must be <exp> -> ID. get the id, look it up, then return its value.
			String id = expNode.getChild(0).getValue().getValue();
			return getStringList(id);
		} else if (expNode.getChildren().size() == 3) {
			// must be <exp> -> ( <exp> ). recurse on the middle child, which is another <exp>
			return evaluateExp(expNode.getChild(1));
		} else if (expNode.getChild(0).getValue() == NonterminalMiniReSymbol.TERM) {
			// must be <exp> -> <term> <exp-tail>.
			List<StringMatch> toReturn = evaluateTerm(expNode.getChild(0));
			// <exp-tail> might be null, in which case we don't need to worry about binary operators.
			if (expNode.getChildren().size() == 1) {
				// there's no exp-tail node, so we can just return this
				return toReturn;
			}
			// otherwise, we need to traverse all the way down until exp-tail is null!
			// remember, binary ops are left-associative.
			ParseTree expTailNode = expNode.getChild(1);
			while (true) {
				// <exp-tail> -> <bin-op> <term> <exp-tail>
				// first, let's get the second argument to the binary operator.
				List<StringMatch> term = evaluateTerm(expTailNode.getChild(1));
				// then, let's figure out which operator we're using and call it.
				ParseTree binaryOperatorNode = expTailNode.getChild(0);
				ReservedWord binaryOperator = (ReservedWord) binaryOperatorNode.getChild(0).getValue();
				switch (binaryOperator) {
				case DIFF:
					toReturn = diff(toReturn, term);
					break;
				case UNION:
					toReturn = union(toReturn, term);
					break;
				case INTERS:
					toReturn = inters(toReturn, term);
					break;
				default:
					throw new RuntimeException("Found invalid binary operator: " + binaryOperator.getValue());
				}
				// now, update expTailNode, or break if we're at the bottom of the tree.
				if (expTailNode.getChildren().size() == 3) {
					expTailNode = expTailNode.getChild(2);
				} else {
					break;
				}
			}
			return toReturn;
		} else {
			throw new RuntimeException("Couldn't evaluate exp");
		}
	}
	
	private List<StringMatch> evaluateTerm(ParseTree termNode) {
		// find REGEX in <file-name>
		
		// first, make sure it's a TERM node.
		// then, get a DFA for the regular expression by using project 1 code:
		// 		regexparser, parse tree -> nfa, nfa -> dfa.
		// then, scan the filename for strings that match the regex.
		// store them all in a list of stringmatch objects, then return that list.
		
		throw new UnsupportedOperationException("TODO: find");

		
	}
	
	private List<StringMatch> diff(List<StringMatch> strings1, List<StringMatch> strings2) {
		List<StringMatch> differenceList = new LinkedList<StringMatch>();
		
		for(StringMatch string1: strings1)
		{
			if(!strings2.contains(string1))
			{
				differenceList.add(string1);
			}
		}
		return differenceList;
	}
	
	private List<StringMatch> union(List<StringMatch> strings1, List<StringMatch> strings2) {
		// TODO
		throw new UnsupportedOperationException("TODO: union");
	}
	private List<StringMatch> inters(List<StringMatch> strings1, List<StringMatch> strings2) {
		// TODO
		throw new UnsupportedOperationException("TODO: inters");
	}
	
	private void printExpList(ParseTree expListNode) {
		throw new UnsupportedOperationException("TODO: print exp list");
		
		// watch out: variables will be either stringmatch lists or integers!
		// handle accordingly!

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
	
	// a little data wrapper representing a string and its metadata
	private static class StringMatch {
		// <file-name, line, start-index, end-index>
		String value, fileName;
		Integer line, startIndex, endIndex;
		public StringMatch(String value	, String fileName, int line, int startIndex, int endIndex) {
			this.value = value;
			this.fileName = fileName;
			this.line = line;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		
		public boolean equals(Object compare){
			return value.equals(((StringMatch)compare).value);
		}
	}
}

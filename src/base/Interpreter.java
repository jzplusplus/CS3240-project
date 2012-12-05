package base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import parser.MiniReParser;
import parser.ParseTree;
import parser.RegexParser;
import parser.Symbol.NonterminalMiniReSymbol;
import parser.Symbol.ReservedWord;
import util.DFA;
import util.NFA;
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
	
	private void run(ParseTree node) throws IOException {
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

				
			} else if (isReplace(node) || isRecursiveReplace(node)) {
				// replace REGEX with ASCII-STR in <file-names> ;
				// recursivereplace REGEX with ASCII-STR in <file-names> ;
				
				String regex = node.getChild(1).getValue().getValue();
				String substitution = node.getChild(3).getValue().getValue();
				// <filenames> -> <source-file> >! <destination-file>
				// <source-file> -> ASCII-STR
				ParseTree filenamesNode = node.getChild(5);
				String sourceFile = filenamesNode.getChild(0).getChild(0).getValue().getValue();
				String destFile = filenamesNode.getChild(2).getChild(0).getValue().getValue();
				
				if (isReplace(node)) {
					replace(regex, substitution, sourceFile, destFile);
				} else {
					recursiveReplace(regex, substitution, sourceFile, destFile);
				}
				
			} else if (isPrint(node)) {
				// print
				// print ( <exp-list> ) ;
				//  print each of the <exp>s that make up <exp-list>
				printExpList(node.getChild(2));
			}
			
			break;
		
		}
		
	}

	private void recursiveReplace(String regex, String substitution,
			String sourceFile, String destFile) throws IOException {
		// Additionally a parser error should occur if REGEX and STR are the same.
		if (substitution.equals('"' + regex + '"')) {
			// TODO have the parser check for this...?
			throw new RuntimeException("recursiveReplace: regex cannot equal substitution string");
		}
		boolean stringsReplaced = replace(regex, substitution, sourceFile, destFile);
		if (!stringsReplaced) { // we're done! let's exit!
			return;
		}
		// otherwise, we need to keep trying to replace things.
		
		// Since sourceFile and destFile must be different, we need a temporary dest file:
		File temp = File.createTempFile("temp-1-" + System.currentTimeMillis(), ".txt");
		while (stringsReplaced) {
			// as long as we're still changing the file, keep trying to replace things
			copy(new File(destFile), temp.getPath());
			stringsReplaced = replace(regex, substitution, temp.getPath(), destFile);
			
		}
		// finally, copy the temporary results into the ultimate destination.
		//copy(temp, destFile);
		temp.deleteOnExit();
	}
	
	private void copy(File source, String destName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(source));
		BufferedWriter writer = new BufferedWriter(new FileWriter(destName));
		String line = reader.readLine();
		writer.write(line);
		while ((line = reader.readLine()) != null) {
			writer.newLine();
			writer.write(line);
		}
		reader.close();
		writer.close();
	}

	/**
	 * Replace all strings in sourceFile that match regex with substitution.
	 * Write the output to destFile.
	 * @param regex
	 * @param substitution
	 * @param sourceFile
	 * @param destFile
	 * @return true if any strings were replaced
	 * @throws IOException 
	 */
	private boolean replace(String regex, String substitution, String sourceFile,
			String destFile) throws IOException {
		/*
		 * A parser error occurs if filename1 = filename2. 
		 * A runtime error occurs if filename1 doesn’t exist or filename2 can’t be written to.
		 */
		if (sourceFile.equals(destFile)) { // TODO make parser catch this?
			throw new RuntimeException("replace: Source file may not equal destination file");
		}
		List<StringMatch> matches = find(makeDfa(regex), sourceFile);
		if (matches.isEmpty()) {
			copy(new File(sourceFile), destFile);
			return false;
		}

		LineNumberReader reader = new LineNumberReader(new FileReader(sourceFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
		String line;
		while ((line = reader.readLine()) != null) {
			List<StringMatch> matchesToReplace = getMatchesForLine(reader.getLineNumber(), matches);
			String replacedLine = "";
			int i = 0;
			while (i < line.length()) {
				List<StringMatch> indexMatches = getMatchesForStartIndex(i, matchesToReplace);
				if (indexMatches.isEmpty()) {
					replacedLine += line.charAt(i);
					i++;
				} else {
					// there's probably only one match per start index, right?
					// regardless, let's loop over them, I guess
					for (StringMatch toReplace : indexMatches) {
						replacedLine += substitution;
						i = toReplace.endIndex - 1;
					}
				}
			}
			writer.write(replacedLine);
			writer.newLine(); // TODO don't write final newline
		}
		reader.close();
		writer.close();
		return true;
	}
	
	private List<StringMatch> getMatchesForLine(int lineNumber, List<StringMatch> matches) {
		List<StringMatch> lineMatches = new ArrayList<StringMatch>();
		for (StringMatch m : matches) {
			if (m.line.equals(lineNumber)) {
				lineMatches.add(m);
			}
		}
		return lineMatches;
	}
	
	private List<StringMatch> getMatchesForStartIndex(int startIndex, List<StringMatch> matches) {
		List<StringMatch> indexMatches = new ArrayList<StringMatch>();
		for (StringMatch m : matches) {
			if (m.startIndex.equals(startIndex)) {
				indexMatches.add(m);
			}
		}
		return indexMatches;
	}

	private List<StringMatch> evaluateExp(ParseTree expNode) throws FileNotFoundException {
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
	
	private List<StringMatch> evaluateTerm(ParseTree termNode) throws FileNotFoundException {
		// find REGEX in <file-name>
		// first, make sure it's a TERM node.
		if (termNode.getValue() != NonterminalMiniReSymbol.TERM) {
			throw new RuntimeException("Called evaluateTerm on a " + termNode.getValue().getValue() + " node");
		}
		
		// then, get a DFA for the regular expression by using project 1 code:
		// 		regexparser, parse tree -> nfa, nfa -> dfa.
		String regex = termNode.getChild(1).getValue().getValue();
		DFA regexDfa = makeDfa(regex);
		
		// then, scan the filename for strings that match the regex.
		
		// <file-name> -> "foo.txt"
		String fileName = termNode.getChild(3).getChild(0).getValue().getValue();

		return find(regexDfa, fileName);
		
	}
	
	private DFA makeDfa(String regex) {
		ParseTree regexTree;
		try {
			regexTree = RegexParser.parse(regex, new HashMap<String, ParseTree>());
		} catch (ParseException e) {
			throw new RuntimeException("Invalid regular expression: " + regex);
		} catch (IOException e) {
			throw new RuntimeException("Got IO error parsing the regular expression: " + regex);
		}
		NFA regexNfa = new NFABuilder(new HashMap<String, ParseTree>()).build(regexTree);
		DFA regexDfa = new DFA(regexNfa, regexNfa.getStartState());
		return regexDfa;
	}
	
	private List<StringMatch> find(DFA regex, String filename) {
		try {

			LineNumberReader fileReader = new LineNumberReader(new FileReader(filename));
			List<StringMatch> matches = new ArrayList<StringMatch>();
			String line;
			while ((line = fileReader.readLine()) != null) {
				// time to get a new token: let's reset all our bookkeeping objects
				// make a pushback reader for this line, so we can unread leftover characters
				PushbackReader lineReader = new PushbackReader(new StringReader(line), line.length());
				Stack<Character> leftovers = new Stack<Character>();
				int startIndex = 0;
				int endIndex = 0;
				while (true) {
					StringMatch match = null;
					StringBuilder matchingString = new StringBuilder();
					regex.reset();
					int nextInt;
					endIndex = startIndex;
					
					while ((nextInt = lineReader.read()) != -1) {
						// iterate over all the characters left in this line, so we get the longest match possible
						Character next = (char) nextInt;
						leftovers.push(next);
						matchingString.append(next);
						endIndex++;
						// advance the DFA
						regex.doTransition(next);
						if (regex.isInAcceptState()) {
							match = new StringMatch(matchingString.toString(), 
													filename, 
													fileReader.getLineNumber(), 
													startIndex, 
													endIndex + 1);
							leftovers.clear();
						}
					}
					if (!leftovers.isEmpty()) {
						// push the leftovers back into the line reader, so we can search through them again
						while (!leftovers.isEmpty()) {
							lineReader.unread(leftovers.pop());
							endIndex--;
						}
					} else {
						break; // no more characters to read: get out of the while(true)
					}
					if (match == null) {
						// we didn't find a match on this sequence.
						// so, let's skip the next character, so we don't just scan the same sequence again!
						lineReader.read();
						startIndex = endIndex + 1;
					} else {
						matches.add(match);
						startIndex = endIndex;
					}
				}
			}
			return matches;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	private static class StringMatch implements Comparable<StringMatch> {
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
		
		public boolean equals(Object compare){ // this is... not correct. make another method that just compares values, if that's what you care about.
			return value.equals(((StringMatch)compare).value);
		}

		@Override
		public int compareTo(StringMatch o) {
			// order by:
			// filename, line, start index, then value.
			if (!this.fileName.equals(o.fileName)) {
				return this.fileName.compareTo(o.fileName);
			}
			if (!this.line.equals(o.line)) {
				return this.line.compareTo(o.line);
			}
			if (!this.startIndex.equals(o.startIndex)) {
				return this.startIndex.compareTo(o.startIndex);
			}
			return this.value.compareTo(o.value);
		}
		
		@Override
		public String toString() {
			return '"' + value + "\" <" + fileName + ", " + line + ", " + startIndex + ", " + endIndex + ">";
		}
	}
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;



import parser.LL1AST;
import parser.MiniReLL1Parser;
import parser.Nonterminal;
import parser.ParseTree;
import parser.RegexParser;
import util.DFA;
import util.NFA;
import exception.IncorrectRuleFormatException;
import exception.InputRuleMismatchException;
import exception.InvalidProgramException;
import exception.InvalidTokenException;
import exception.MultipleStartSymbolException;
import exception.ParseException;
import exception.RuleApplicabilityException;
import exception.UndefinedNonterminalException;

public class InterpreterLL1 {

	private LL1AST scriptRoot;

	// private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private HashMap<String,Nonterminal> nonterminalMap;

	// the "memory" we'll be using to store our variables:
	// maps from an identifier to a value.
	// DON'T access these directly: instead, use assignStringList / assignInteger, etc.
	private Map<String, List<StringMatch>> stringListVars;
	private Map<String, Integer> intVars;

	private static String MINI_RE_PROGRAM = "<MiniRE-program>"; 
	private static String STATEMENT_LIST = "<statement-list>";
	private static String STATEMENT_LIST_TAIL = "<statement-list-tail>";
	private static String STATEMENT = "<statement>";
	private static String STATEMENT_RIGHTHAND = "<statement-righthand>";
	private static String FILE_NAMES = "<file-names>";
	private static String SOURCE_FILE = "<source-file>";
	private static String DESTINATION_FILE = "<destination-file>";
	private static String EXP_LIST = "<exp-list>";
	private static String EXP_LIST_TAIL = "<exp-list-tail>";
	private static String EXP = "<exp>";
	private static String EXP_TAIL = "<exp-tail>";
	private static String TERM = "<term>";
	private static String FILE_NAME = "<file-name>";
	private static String BIN_OP = "<bin-op>";
	private static String EPSILON = "<epsilon>";

	private static String WRITE_TO = ">!";
	private static String HASH = "#";

	private static String BEGIN = "begin";
	private static String END = "end";
	private static String COMMA = ",";
	private static String OPEN_PAR = "(";
	private static String CLOSE_PAR = ")";
	private static String SEMICOLON = ";";
	private static String EQUAL = "=";
	private static String WITH = "with";
	private static String IN = "in";

	// functions
	private static String DIFF = "diff";
	private static String UNION = "union";
	private static String INTERS = "inters";
	private static String PRINT = "print";
	private static String FIND = "find";
	private static String REPLACE = "replace";
	private static String RECURSIVEREPLACE = "recursivereplace";
	private static String MAXFREQSTRING = "maxfreqstring";

	private static String ASCII_STR = "ASCII-STR";
	private static String REGEX = "REGEX";
	private static String ID = "ID";
	
	private static String prefix;

	public InterpreterLL1(String script, String grammar, String filePrefix) throws IOException, ParseException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException, InputRuleMismatchException, RuleApplicabilityException, InvalidTokenException, InvalidProgramException {
		this.prefix = filePrefix;
		stringListVars = new HashMap<String, List<StringMatch>>();
		intVars = new HashMap<String, Integer>();
		MiniReLL1Parser miniRE = new MiniReLL1Parser(script, grammar);
		scriptRoot = miniRE.getRoot();
		nonterminalMap = miniRE.getNonterminalMap();
		run(scriptRoot);
	}

	private void run(LL1AST node) throws IOException {
		if (!nonterminalMap.containsKey(node.getValue()) && !node.getValue().equals(EPSILON)) { // root is terminal
			throw new RuntimeException("Interpreter: should be handling terminal nodes elsewhere!");
		}
		
		if (node.getValue().equals(EPSILON)) { return; }
		
		// otherwise, assume the value is one of the MiniRE nonterminals.

		Nonterminal nt = nonterminalMap.get(node.getValue());
		String value = nt.getValue();
		if ("<MiniRE-program>".equals(nt.getValue())) {
			// begin <statement-list> end: recurse on children[1]
			run(node.getChild(1));
		} else if ("<statement-list>".equals(nt.getValue()) || "<statement-list-tail>".equals(nt.getValue())) {
			// <statement><statement-list-tail>: recurse on both. tail might be null.
			// <statement>
			run(node.getChild(0));
			if (node.getChildren().size() == 2) {
				// <statement-list-tail>
				run(node.getChild(1));
			}
		} else if ("<statement>".equals(nt.getValue())) {
			// there are only a few kinds of statements:
			// string list assignment
			if (isStringListAssignment(node)) {
				// ID = <exp> ;
				// first, evaluate EXP and then call assignStringList(id, expReturnValue);
				String id = node.getChild(0).getChild(0).getValue();
				List<StringMatch> exp = evaluateExp(node.getChild(2).getChild(0));
				assignStringList(id, exp);

			} else if (isIntegerAssignment(node)) {
				// integer assignment
				// ID = <statement-righthand> ;
				// <statement-righthand> -> # <exp>
				// first, evaluate EXP, then call assignInteger(id, expReturnValue.size())
				String id = node.getChild(0).getChild(0).getValue();
				List<StringMatch> exp = evaluateExp(node.getChild(2).getChild(1));
				Integer size = exp.size();
				assignInteger(id, size);

			} else if (isMaxFreqAssignment(node)) {
				// maxfreqstring assignment
				// ID1 = maxfreqstring ( ID2 ) ;
				// first, find the maxfreqstring(s) of ID2, then call assignStringList(id1, maxFreqString);
				String id1 = node.getChild(0).getChild(0).getValue();
				String id2 = node.getChild(2).getChild(2).getChild(0).getValue();
								
				List<StringMatch> maxFreqString = getMaxFreqString(getStringList(id2));
				assignStringList(id1, maxFreqString);


			} else if (isReplace(node) || isRecursiveReplace(node)) {
				// replace REGEX with ASCII-STR in <file-names> ;
				// recursivereplace REGEX with ASCII-STR in <file-names> ;

				String regex = node.getChild(1).getChild(0).getValue();
				if (regex.startsWith("\'")) regex = regex.substring(1);
				if (regex.endsWith("\'")) regex = regex.substring(0, regex.length()-1);

				String substitution = node.getChild(3).getChild(0).getValue();
				if (substitution.startsWith("\"")) substitution = substitution.substring(1);
				if (substitution.endsWith("\"")) substitution = substitution.substring(0, substitution.length()-1);

				// <filenames> -> <source-file> >! <destination-file>
				// <source-file> -> ASCII-STR
				LL1AST filenamesNode = node.getChild(5);
				String sourceFile = filenamesNode.getChild(0).getChild(0).getChild(0).getValue();
				if (sourceFile.startsWith("\"")) sourceFile = sourceFile.substring(1);
				if (sourceFile.endsWith("\"")) sourceFile = sourceFile.substring(0, sourceFile.length()-1);
				String destFile = filenamesNode.getChild(2).getChild(0).getChild(0).getValue();
				if (destFile.startsWith("\"")) destFile = destFile.substring(1);
				if (destFile.endsWith("\"")) destFile = destFile.substring(0, destFile.length()-1);

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
		}

	}

	private void assignStringList(String id, List<StringMatch> value) {
		// first make sure it's not in the integer table:
		if (intVars.containsKey(id)) {
			// remove it
			intVars.remove(id);
		}
		stringListVars.put(id, value);
	}

	
	private List<StringMatch> getMaxFreqString(List<StringMatch> stringList) {		
		if(stringList.size() == 0) return new ArrayList<StringMatch>();
		
		StringMatch maxFreqString = null;
		int maxFreq = 0;
		
		for(StringMatch s: stringList) {
			int freq = getMatchesForValue(s.value, stringList).size();
			if(freq > maxFreq) {
				maxFreq = freq;
				maxFreqString = s;
			}
		}

		//return ALL matches of the String with the most frequency
		List<StringMatch> list = getMatchesForValue(maxFreqString.value, stringList);
		return list;
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
		if(!source.getPath().startsWith("/var")) source = new File(prefix+source.getPath());
		
		BufferedReader reader = new BufferedReader(new FileReader(source));
		if(!destName.startsWith("/var")) destName = prefix+destName;
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
		 * A runtime error occurs if filename1 does not exist or filename2 can be written to.
		 */
		if (sourceFile.equals(destFile)) { // TODO make parser catch this?
			throw new RuntimeException("replace: Source file may not equal destination file");
		}
		List<StringMatch> matches = find(makeDfa(regex), sourceFile);
		if (matches.isEmpty()) {
			copy(new File(sourceFile), destFile);
			return false;
		}

		LineNumberReader reader = new LineNumberReader(new FileReader(prefix+sourceFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(prefix+destFile));
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

	private List<StringMatch> getMatchesForValue(String value, List<StringMatch> matches) {
		List<StringMatch> valueMatches = new ArrayList<StringMatch>();
		for (StringMatch m : matches) {
			if (m.value.equals(value)) {
				valueMatches.add(m);
			}
		}
		return valueMatches;
	}

	private List<StringMatch> evaluateExp(LL1AST expNode) throws FileNotFoundException {
		if (!expNode.getValue().equals(EXP)) throw new RuntimeException("Tried to call evaluateExp on a non-EXP node");

		// possible production rules:
		// <exp> -> ID
		// <exp> -> ( <exp> )
		// <exp> -> <term> <exp-tail>

		if (expNode.getChildren().size() == 1 && expNode.getChild(0).getValue().equals(ID)) {
			// must be <exp> -> ID. get the id, look it up, then return its value.
			String id = expNode.getChild(0).getChild(0).getValue();
			return getStringList(id);
		} else if (expNode.getChildren().size() == 3) {
			// must be <exp> -> ( <exp> ). recurse on the middle child, which is another <exp>
			return evaluateExp(expNode.getChild(1));
		} else if (expNode.getChild(0).getValue().equals(TERM)) {
			// must be <exp> -> <term> <exp-tail>.
			List<StringMatch> toReturn = evaluateTerm(expNode.getChild(0));
			// <exp-tail> might be null, in which case we don't need to worry about binary operators.
			if (expNode.getChildren().size() == 1) {
				// there's no exp-tail node, so we can just return this
				return toReturn;
			}
			// otherwise, we need to traverse all the way down until exp-tail is null!
			// remember, binary ops are left-associative.
			LL1AST expTailNode = expNode.getChild(1);
			while (true) {

				if (expTailNode.getChild(0).getValue().equals(EPSILON)) break;

				// <exp-tail> -> <bin-op> <term> <exp-tail>
				// first, let's get the second argument to the binary operator.
				List<StringMatch> term = evaluateTerm(expTailNode.getChild(1));
				// then, let's figure out which operator we're using and call it.
				LL1AST binaryOperatorNode = expTailNode.getChild(0).getChild(0);

				if ("diff".equals(binaryOperatorNode.getValue())) {
					toReturn = diff(toReturn, term);
				} else if ("union".equals(binaryOperatorNode.getValue())) {
					toReturn = union(toReturn, term);
				} else if ("inters".equals(binaryOperatorNode.getValue())) {
					toReturn = inters(toReturn, term);
				} else {
					throw new RuntimeException("Found invalid binary operator: " + binaryOperatorNode.getValue());
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

	private List<StringMatch> evaluateTerm(LL1AST termNode) throws FileNotFoundException {
		// find REGEX in <file-name>
		// first, make sure it's a TERM node.
		if (!termNode.getValue().equals(TERM)) throw new RuntimeException("Called evaluateTerm on a " + termNode.getValue() + " node");


		// then, get a DFA for the regular expression by using project 1 code:
		// 		regexparser, parse tree -> nfa, nfa -> dfa.
		String regex = termNode.getChild(1).getChild(0).getValue();
		if (regex.startsWith("\'")) regex = regex.substring(1);
		if (regex.endsWith("\'")) regex = regex.substring(0, regex.length()-1);
		System.out.println("regex being parsed:" + regex);
		DFA regexDfa = makeDfa(regex);

		// then, scan the filename for strings that match the regex.

		// <file-name> -> "foo.txt"
		String fileName = termNode.getChild(3).getChild(0).getChild(0).getValue();
		if (fileName.startsWith("\"")) fileName = fileName.substring(1);
		if (fileName.endsWith("\"")) fileName = fileName.substring(0, fileName.length()-1);

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
			if(!filename.startsWith("/var")) filename = prefix+filename;
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
		ArrayList<StringMatch> differenceList = new ArrayList<StringMatch>();

		for(StringMatch s1: strings1)
		{
			if(getMatchesForValue(s1.value, strings2).size() == 0)
			{
				differenceList.add(s1);
			}
		}
		return differenceList;
	}

	private List<StringMatch> union(List<StringMatch> strings1, List<StringMatch> strings2) {
		List<StringMatch> unionList = new ArrayList<StringMatch>();

		for(StringMatch s1: strings1)
		{
			unionList.add(s1);
		}

		for(StringMatch s2: strings2)
		{
			if(!unionList.contains(s2)) unionList.add(s2);
		}

		return unionList;
	}

	private List<StringMatch> inters(List<StringMatch> strings1, List<StringMatch> strings2) {
		List<StringMatch> intersList = new ArrayList<StringMatch>();

		//only look for matches with a certain value once
		List<String> valuesInList = new ArrayList<String>();

		for(StringMatch s1: strings1)
		{
			//Already added all of strings1 and strings2 that matches this value
			if(valuesInList.contains(s1.value)) continue;

			List<StringMatch> matches2 = getMatchesForValue(s1.value, strings2);
			if(matches2.size() > 0) //If we find the value in both lists
			{
				List<StringMatch> matches1 = getMatchesForValue(s1.value, strings1);

				intersList.addAll(union(matches1, matches2)); //union all StringMatches for value from strings1 and strings2
			}
		}

		return intersList;
	}

	private void printExpList(LL1AST expListNode) throws FileNotFoundException  {
		// if <exp> is an integer
		// 	prints out the integer value + a new line
		// else if <exp> is a string-match
		// 	prints out the elements in order including the matched string, the filename, and the index
		// if the current node has children
		// 	pass each child to the current function 

		System.out.println(" ----- Come into printExpList ----- ");

		if(expListNode!=null) {				
			String id = null; // the only case <exp> meets an ID is "<exp> ::= ID  | ( <exp> )" which has an ID as the first child. 			

			if (expListNode.getChild(0).getChild(0).getValue().equals(ID)) {
				id = expListNode.getChild(0).getChild(0).getChild(0).getValue();
			}

			if (id!=null && expListNode.getChild(1).getChild(0).getValue().equals(EPSILON)) {
				if(intVars.containsKey(id)) { // if the first child was an ID
					System.out.println("ID: " + id + " // Integer Value: " + getInteger(id));

				} else if(stringListVars.containsKey(id)) { // if the first child was an ID
					List<StringMatch> strMList = getStringList(id);

					for(int i=0; i<strMList.size(); i++) { 
						System.out.println("ID: " + id + " // Index: " + i + " // StringMatch Value: " + strMList.get(i).toString());
					}					
				} else { 
					throw new RuntimeException("Error: ID does not exist.");
				}
			} else {
				throw new RuntimeException("Error: It was neither StringMatch nor Integer.");
			}				
		}
		
		LL1AST expListTailNode = expListNode.getChild(1);

		if( !expListTailNode.getChild(0).getValue().equals(EPSILON) ) {
			for(LL1AST node: expListTailNode.getChildren()) // since we do not exactly know the number of children of the current node (<exp> or <exp-list> or <exp-list-tail>)
				if(node != null)
					if(node.getValue().equals(EXP)) // only passes <exp> children to save time
						printExpList(node);
		}
				
	}

	private static boolean isStringListAssignment(LL1AST statementNode) {
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		// string list assignments:
		// ID = <exp> ;
		// <statement> -> ID = <statement-righthand> ;
		// <statement-righthand> -> <exp>
		if (statementNode.getChildren().size() != 4) return false;
		if (statementNode.getChild(2).getChildren().size() != 1) return false;

		return (statementNode.getChild(0).getValue().equals(ID) 
				&& statementNode.getChild(1).getValue().equals(EQUAL)
				&& statementNode.getChild(2).getChild(0).getValue().equals(EXP)
				&& statementNode.getChild(3).getValue().equals(SEMICOLON));
	}


	private boolean isIntegerAssignment(LL1AST statementNode) {
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		// int assignments:
		// ID = # <exp> ;
		// <statement> -> ID = <statement-righthand> ;
		// <statement-righthand> -> # <exp>
		if (statementNode.getChildren().size() != 4) return false;
		if (!statementNode.getChild(2).getValue().equals(STATEMENT_RIGHTHAND)) return false;
		if (statementNode.getChild(2).getChildren().size()!=2) return false;

		return (statementNode.getChild(0).getValue().equals(ID) 
				&& statementNode.getChild(1).getValue().equals(EQUAL)
				&& statementNode.getChild(2).getChild(0).getValue().equals(HASH)
				&& statementNode.getChild(2).getChild(1).getValue().equals(EXP)
				&& statementNode.getChild(3).getValue().equals(SEMICOLON));
	}

	private boolean isMaxFreqAssignment(LL1AST statementNode) {
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		// maxfreq assignments:
		// ID = maxfreqstring ( ID ) ;
		// <statement> -> ID = <statement-righthand> ;
		// <statement-righthand> -> maxfreqstring ( ID )
		if (statementNode.getChildren().size() != 4) return false;
		if (!statementNode.getChild(2).getValue().equals(STATEMENT_RIGHTHAND)) return false;
		if (statementNode.getChild(2).getChildren().size()!=4) return false;


		return (statementNode.getChild(0).getValue().equals(ID) 
				&& statementNode.getChild(1).getValue().equals(EQUAL)
				&& statementNode.getChild(2).getChild(0).getValue().equals(MAXFREQSTRING)
				&& statementNode.getChild(2).getChild(1).getValue().equals("(")
				&& statementNode.getChild(2).getChild(2).getValue().equals(ID)
				&& statementNode.getChild(2).getChild(3).getValue().equals(")")
				&& statementNode.getChild(3).getValue().equals(SEMICOLON));
	}


	private boolean isReplace(LL1AST statementNode) {
		// replace REGEX with ASCII-STR in <file-names> ;
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		return statementNode.getChild(0).getValue().equals(REPLACE);
	}

	private boolean isRecursiveReplace(LL1AST statementNode) {
		// recursivereplace REGEX with ASCII-STR in <file-names> ;
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		return statementNode.getChild(0).getValue().equals(RECURSIVEREPLACE);
	}

	private boolean isPrint(LL1AST statementNode) {
		// print ( <exp-list> ) ;
		if (!statementNode.getValue().equals(STATEMENT)) return false;

		return statementNode.getChild(0).getValue().equals(PRINT);
	}

	private static String readFile(File handle) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(prefix + handle));
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

		public boolean equals(Object o){
			if(!(o instanceof StringMatch)) return false;
			return (this.compareTo((StringMatch)o) == 0);
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

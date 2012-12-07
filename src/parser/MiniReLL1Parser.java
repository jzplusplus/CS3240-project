package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import parser.Symbol.NonterminalMiniReSymbol;

import exception.IncorrectRuleFormatException;
import exception.InputRuleMismatchException;
import exception.InvalidProgramException;
import exception.InvalidTokenException;
import exception.MultipleStartSymbolException;
import exception.RuleApplicabilityException;
import exception.UndefinedNonterminalException;

public class MiniReLL1Parser {

	private static String MINI_RE_SPEC = "MiniRE_Grammar3.txt";

	private static String EPSILON = "epsilon";
	private static String ASCII_STR = "ASCII-STR";
	private static String REGEX = "REGEX";
	private static String ID = "ID";

	// MiniRE tokens
	private static String WRITE_TO = ">!";
	private static String INTEGER_VAR = "#";

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

	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private ArrayList<String> tokens;	
	private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private HashMap<String,Nonterminal> nonterminalMap;
	private LL1ParsingTable table;
	private ArrayList<String> program;

	private Stack<String> inputStack;
	private Stack<String> parsingStack;
	private String curr;
	private String currType;
	private String top;
	private Nonterminal currNT;

	private ArrayList<String> leftDUI;
	private ArrayList<String> rightDUI;
	private String asciiStrTemp;

	private LL1AST root;
	private LL1AST currNode;

	public MiniReLL1Parser(String script) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException, InputRuleMismatchException, RuleApplicabilityException, InvalidTokenException, InvalidProgramException {
		LL1ParserGenerator miniRE = new LL1ParserGenerator(MINI_RE_SPEC);
		table = miniRE.getParsingTable();
		nonterminals = miniRE.getNonterminals();
		start = miniRE.getStart();
		tokens = miniRE.getTokens();
		ruleMap = miniRE.getRuleMap();
		nonterminalMap = miniRE.getNonterminalMap();
		System.out.println(miniRE.printFirstSets());
		System.out.println(miniRE.printFollowSets());
		System.out.println(miniRE.toString());
		parse(script);
		if (!isValid(program)) throw new InvalidProgramException();
		System.out.println(isValid(program));
		constructAST();
		
	}
	
	
	
	public LL1AST getRoot() { return root; }
	public LL1ParsingTable getParsingTable() { return table; }
	public ArrayList<Nonterminal> getNonterminals() { return nonterminals; }
	public HashMap<Nonterminal,ArrayList<String[]>> getRuleMap() { return ruleMap; }
	public ArrayList<String> getTokens() { return tokens; }
	public Nonterminal getStart() {return start;}
	public HashMap<String,Nonterminal> getNonterminalMap() { return nonterminalMap; }

	
	private void parse(String inputProgram) throws IOException, InvalidProgramException {
		BufferedReader br = new BufferedReader(new FileReader(new File(inputProgram)));
		String curr;
		String pStr = "";
		// program = new ArrayList<String>();
		while( (curr=br.readLine()) != null ) pStr += curr + " ";

		pStr = pStr.trim();

		if (pStr.length()==0) throw new InvalidProgramException();

		if (!pStr.startsWith("begin") || !pStr.endsWith("end")) throw new InvalidProgramException();

		program = split(pStr); 

	}

	private void constructAST() throws RuleApplicabilityException, InputRuleMismatchException, InvalidTokenException {

		root = new LL1AST(start.getValue());

		inputStack = new Stack<String>();
		parsingStack = new Stack<String>();

		for (int i=program.size()-1; i>=0; i--) inputStack.push(program.get(i));

		curr = null; // = determineInputType(inputStack.pop());
		parsingStack.push(start.getValue());


		currNT = start;
		String[] rule = null;
		LL1AST currNode = root;
		// List<LL1AST> children = constructAST(root, curr);

		while (!parsingStack.isEmpty()) {
			curr = inputStack.pop();
			// top = parsingStack.pop();
			currNT = nonterminalMap.get(start.getValue());
			constructAST(currNode);
			convertASTtoParseTree();
		}
	}

	private LL1AST constructAST(LL1AST node) throws RuleApplicabilityException { // assume nonterminal

		String[] rule = null;
		boolean skip = false;

		if (node.getValue().equals(start.getValue())) { // if start		
			currType = determineInputType(curr);
			rule = table.getRule(curr, currNT);
			if (rule==null) throw new RuleApplicabilityException();
			parsingStack.pop();
			pushRule(rule);
			List<LL1AST> children = new ArrayList<LL1AST>();
			for (int i=0; i<rule.length; i++) {
				children.add(new LL1AST(rule[i]));
				// parsingStack.pop();
			}
			node.setChildren(children);
			for (LL1AST child : children) {
				child.addChild(constructAST(child));
				// input = inputStack.pop();
			}
			return node;

		}

		// curr = inputStack.pop();
		// top = parsingStack.pop();

		if (nonterminalMap.containsKey(node.getValue())) {
			currNT = nonterminalMap.get(node.getValue());
			currType = determineInputType(curr);
			rule = table.getRule(currType, currNT);
			// if (rule==null) throw new RuleApplicabilityException();
			if (rule!=null) {
				parsingStack.pop();
				pushRule(rule);
				List<LL1AST> children = new ArrayList<LL1AST>();
				for (int i=0; i<rule.length; i++) {
					children.add(new LL1AST(rule[i]));
					// parsingStack.pop();
				}
				node.setChildren(children);
				for (LL1AST child : children) {
					child.addChild(constructAST(child));
					// input = inputStack.pop();
				}
				return node;
			} else if (currNT.getValue().equals(parsingStack.peek())) { 
				List<LL1AST> children = new ArrayList<LL1AST>();
				for (String[] r : currNT.getRules()) {
					if (r[0].equals(currType)) {
						parsingStack.pop();
						pushRule(r);
						for (int i=0; i<r.length; i++) {
							children.add(new LL1AST(r[i]));
							// parsingStack.pop();
						}
						node.setChildren(children);
						for (LL1AST child : children) {
							child.addChild(constructAST(child));
							// input = inputStack.pop();
						}
						return node;
					}
				}
				
			}
		}


		if (isID(curr) && !skip) {
			// LL1AST temp = new LL1AST(ID);
			// temp.addChild(new LL1AST(curr,true));
			// node.addChild(temp);
			currType = determineInputType(curr);
			if (parsingStack.peek().equals(currType)) parsingStack.pop();
			node.addChild(new LL1AST(curr,true));
			if (!inputStack.isEmpty()) curr = inputStack.pop();
			return node;
		}

		if (isRegEx(curr) && !skip) {
			// LL1AST temp = new LL1AST(REGEX);
			// temp.addChild(new LL1AST(curr,true));
			// node.addChild(temp);
			currType = determineInputType(curr);
			if (parsingStack.peek().equals(currType)) parsingStack.pop();
			node.addChild(new LL1AST(curr,true));
			if (!inputStack.isEmpty()) curr = inputStack.pop();
			return node;
		}
		
		if (isASCII(curr) && !skip) {
			// LL1AST temp = new LL1AST(REGEX);
			// temp.addChild(new LL1AST(curr,true));
			// node.addChild(temp);
			currType = determineInputType(curr);
			if (parsingStack.peek().equals(currType)) parsingStack.pop();
			node.addChild(new LL1AST(curr,true));
			if (!inputStack.isEmpty()) curr = inputStack.pop();
			return node;
		}
		
		if (parsingStack.peek().equals(EPSILON)) {
			node.addChild(new LL1AST(curr, true));
			parsingStack.pop();
			return node;
		}

		if (tokens.contains(curr) && !skip) {
			node.addChild(new LL1AST(curr, true));
			if (parsingStack.peek().equals(curr)) parsingStack.pop();
			if (!inputStack.isEmpty()) curr = inputStack.pop();
			return node;
		}





		return node;
	}

	private void convertASTtoParseTree() {
		ParseTree ptRoot = convertASTtoParseTree(root);
	}
	
	private ParseTree convertASTtoParseTree(LL1AST astNode) {
		ParseTree ptNode = null;
		Symbol s;
		if (nonterminalMap.containsKey(astNode.getValue())) {
			s = matchNonterminalSymbol(astNode.getValue());
		} else if (isRegEx(astNode.getValue())) {
			
		} else if (isID(astNode.getValue())) {
			
		} else if (isASCII(astNode.getValue())) {
			
		} else if (tokens.contains(astNode.getValue())) {
			s = matchReservedWordSymbol(astNode.getValue());
		}
		
		return ptNode;
		
	}
	
	private List<LL1AST> addRuleAsChild(String[] rule) {
		List<LL1AST> children = new ArrayList<LL1AST>();
		for (int i=0; i<rule.length; i++) {

			children.add(new LL1AST(rule[i]));
		}
		return children;
	}

	private Symbol.NonterminalMiniReSymbol matchNonterminalSymbol(String nt) {
		if (nt.equals("<MiniRE-program>")) return Symbol.NonterminalMiniReSymbol.MINIRE_PROGRAM;
		if (nt.equals("<statement-list>")) return Symbol.NonterminalMiniReSymbol.STATEMENT_LIST;
		if (nt.equals("<statement-list-tail>")) return Symbol.NonterminalMiniReSymbol.STATEMENT_LIST_TAIL;
		if (nt.equals("<statement>")) return Symbol.NonterminalMiniReSymbol.STATEMENT;
		if (nt.equals("<statement-tail>")) return Symbol.NonterminalMiniReSymbol.STATEMENT_TAIL;
		if (nt.equals("<file-names>")) return Symbol.NonterminalMiniReSymbol.FILE_NAMES;
		if (nt.equals("<source-file>")) return Symbol.NonterminalMiniReSymbol.SOURCE_FILE;
		if (nt.equals("<destination-file>")) return Symbol.NonterminalMiniReSymbol.DESTINATION_FILE;
		if (nt.equals("<exp-list>")) return Symbol.NonterminalMiniReSymbol.EXP_LIST;
		if (nt.equals("<exp-list-tail>")) return Symbol.NonterminalMiniReSymbol.EXP_LIST_TAIL;
		if (nt.equals("<exp>")) return Symbol.NonterminalMiniReSymbol.EXP;
		if (nt.equals("<exp-tail>")) return Symbol.NonterminalMiniReSymbol.EXP_TAIL;
		if (nt.equals("<term>")) return Symbol.NonterminalMiniReSymbol.TERM;
		if (nt.equals("<file-name>")) return Symbol.NonterminalMiniReSymbol.FILE_NAME;
		if (nt.equals("<bin-op>")) return Symbol.NonterminalMiniReSymbol.BIN_OP;
		return null;
	}

	private Symbol.ReservedWord matchReservedWordSymbol(String rw) {
		if (rw.equals(BEGIN)) return Symbol.ReservedWord.BEGIN;
		if (rw.equals(END)) return Symbol.ReservedWord.END;
		if (rw.equals(EQUAL)) return Symbol.ReservedWord.ASSIGN;
		if (rw.equals(REPLACE)) return Symbol.ReservedWord.REPLACE;
		if (rw.equals(WITH)) return Symbol.ReservedWord.WITH;
		if (rw.equals(IN)) return Symbol.ReservedWord.IN;
		if (rw.equals(SEMICOLON)) return Symbol.ReservedWord.SEMICOLON;
		if (rw.equals(RECURSIVEREPLACE)) return Symbol.ReservedWord.RECURSIVE_REPLACE;
		if (rw.equals(WRITE_TO)) return Symbol.ReservedWord.WRITE_TO;
		if (rw.equals(PRINT)) return Symbol.ReservedWord.PRINT;
		if (rw.equals(INTEGER_VAR)) return Symbol.ReservedWord.INT;
		if (rw.equals(FIND)) return Symbol.ReservedWord.FIND;
		if (rw.equals(DIFF)) return Symbol.ReservedWord.DIFF;
		if (rw.equals(UNION)) return Symbol.ReservedWord.UNION;
		if (rw.equals(INTERS)) return Symbol.ReservedWord.INTERS;
		if (rw.equals(MAXFREQSTRING)) return Symbol.ReservedWord.MAXFREQSTRING;
		return null;
	}

	public boolean isValid(ArrayList<String> program) throws InputRuleMismatchException, RuleApplicabilityException, InvalidTokenException {
		inputStack = new Stack<String>();
		for (int i=program.size()-1; i>=0; i--) inputStack.push(program.get(i));

		String curr; 
		boolean hasBegun = false;

		while (!inputStack.isEmpty()) {
			curr = determineInputType(inputStack.pop());
			if (curr.equals(BEGIN)) {
				hasBegun = true;
				if (!curr.equals(BEGIN)) throw new InvalidTokenException();

			} else if (isASCII(curr)) {

			} else if (isID(curr)) {

			} else if (isRegEx(curr)) {

			} else if (tokens.contains(curr)) {

			} else throw new RuleApplicabilityException();

			if (curr.equals(END)) {
				if (hasBegun) {
					if(inputStack.isEmpty()) return true; 
				}
			}
		}

		return false;

	} // end valid

	private String determineInputType(String in) {
		if (tokens.contains(in)) return in;
		if (isASCII(in)) return ASCII_STR;
		if (isRegEx(in)) return REGEX;
		if (isID(in)) return ID;
		return null;
	}

	private void pushRule(String[] rule) {
		for (int i=rule.length-1; i>=0; i--) parsingStack.push(rule[i]);
	}

	private boolean isToken(String symbol) {
		for (String t : tokens) {
			if (t.equals(symbol)) return true;
		}
		return false;
	}

	private ArrayList<String> split(String p) {
		ArrayList<String> out = new ArrayList<String>();
		String temp = "";
		boolean regexDetected = false;
		boolean strDetected = false;
		for (int i=0; i<p.length(); i++) {
			char c = p.charAt(i);
			if (c!=' ') {
				if (c=='\'') {
					temp += c;
					if (regexDetected) {
						regexDetected = false;
						out.add(temp);
						temp = "";
					}
					else regexDetected = true;
				} else if (c=='\"') {
					temp += c;
					if (strDetected) {
						strDetected = false;
						out.add(temp);
						temp = "";
					}
					else strDetected = true;
				} else if (c==';') {
					if (!regexDetected && !strDetected) {
						temp = temp.trim();
						if (temp.length()>0) {
							out.add(temp);
							temp = "";
						}
						out.add(";");
					}
				} else if (c=='#') {
					if (!regexDetected && !strDetected) {
						temp = temp.trim();
						if (temp.length()>0) {
							out.add(temp);
							temp = "";
						}
						out.add("#");
					}
				} else if (c=='(' || c==')') { 
					if (!regexDetected && !strDetected) {
						temp = temp.trim();
						if (temp.length()>0) {
							out.add(temp);
							temp = "";
						}
						if (c=='(') out.add("(");
						else out.add(")");
					}
				} else {
					temp += c;
				}
			} else {
				if (!regexDetected && !strDetected) {
					temp = temp.trim();
					if (temp.length()>0) {
						out.add(temp);
						temp = "";
					}
				} else {
					temp += c;
				}

			}
		}

		if (temp.equals(END)) out.add(temp);

		if (regexDetected || strDetected) {
			// error
		}

		return out;
	}

	private void handleID() {

	}

	private void handleRegEx() {

	}

	private void handleASCII() {

	}

	private void handleReplace() throws InputRuleMismatchException {
		String[] params = replaceHelper();
		doReplace(params[0],params[1],params[2],params[3]);
	}

	private void handleRecursivereplace() throws InputRuleMismatchException {
		String[] params = replaceHelper();
		doRecursivereplace(params[0],params[1],params[2],params[3]);
	}

	private String[] replaceHelper() throws InputRuleMismatchException {
		String[] out = new String[4];
		if (!top.equals(REGEX) || !isRegEx(curr)) throw new InputRuleMismatchException();
		out[0] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(WITH) || !curr.equals(WITH)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(ASCII_STR) || !isASCII(curr)) throw new InputRuleMismatchException();
		out[1] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(IN) || !curr.equals(IN)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(ASCII_STR) || !isASCII(curr)) throw new InputRuleMismatchException();
		out[2] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(WRITE_TO) || !curr.equals(WRITE_TO)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(ASCII_STR) || !isASCII(curr)) throw new InputRuleMismatchException();
		out[3] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(SEMICOLON) || !curr.equals(SEMICOLON)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		return out;
	}

	private void doReplace(String regex, String replacement, String fileIn, String fileOut) {

	}

	private void doRecursivereplace(String regex, String replacement, String fileIn, String fileOut) {

	}

	private void handleFind() throws InputRuleMismatchException {
		ArrayList<ArrayList<String>> found = new ArrayList<ArrayList<String>>();

		String[] params = findHelper();
		found.add(doFind(params[0],params[1]));

		if (top.equals(SEMICOLON)) {
			if(!curr.equals(SEMICOLON)) throw new InputRuleMismatchException();
			inputStack.pop();
			parsingStack.pop();
			top = parsingStack.peek();
			curr = inputStack.peek();
			// handle output
		} else if (top.equals(DIFF) || top.equals(INTERS) || top.equals(UNION)) {
			if(!curr.equals(DIFF) || !curr.equals(INTERS) || !curr.equals(UNION)) throw new InputRuleMismatchException();
			inputStack.pop();
			parsingStack.pop();
			top = parsingStack.peek();
			if (curr.equals(DIFF)) {
				curr = inputStack.peek();
				String[] params2 = findHelper();
			} else if (curr.equals(INTERS)) {
				curr = inputStack.peek();
			} else {
				curr = inputStack.peek();
			}


		}

	}

	private String[] findHelper() throws InputRuleMismatchException {
		String[] out = new String[2];
		if (!top.equals(REGEX) || !isRegEx(curr)) throw new InputRuleMismatchException();
		out[0] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(IN) || !curr.equals(IN)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(ASCII_STR) || !isASCII(curr)) throw new InputRuleMismatchException();
		out[1] = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		return out;
	}

	private ArrayList<String> doFind(String regex, String fileIn) {
		return null;
	}

	private void handleMaxfreqstring() throws InputRuleMismatchException {
		String idStr;
		if (!top.equals(OPEN_PAR) || !isRegEx(OPEN_PAR)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(ID) || !isID(curr)) throw new InputRuleMismatchException();
		idStr = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(CLOSE_PAR) || !isRegEx(CLOSE_PAR)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(SEMICOLON) || !curr.equals(SEMICOLON)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		doMaxfreqstring(idStr);
	}

	private void doMaxfreqstring(String ideStr) {

	}

	private void handlePrint() throws InputRuleMismatchException {
		String idStr;
		if (!top.equals(OPEN_PAR) || !isRegEx(OPEN_PAR)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!nonterminalMap.containsKey(top)) throw new InputRuleMismatchException();



		if (!top.equals(ID) || !isID(curr)) throw new InputRuleMismatchException();
		idStr = curr.substring(1, curr.length()-1);
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(CLOSE_PAR) || !isRegEx(CLOSE_PAR)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();

		if (!top.equals(SEMICOLON) || !curr.equals(SEMICOLON)) throw new InputRuleMismatchException();
		inputStack.pop();
		parsingStack.pop();
		top = parsingStack.peek();
		curr = inputStack.peek();
		doPrint();
	}

	private void doPrint() {

	}

	private boolean isID(int i) {
		return tokens.contains(program.get(i)) && !isRegEx(i) && !isASCII(i);
	}

	private boolean isRegEx(int i) {
		return program.get(i).startsWith("\'") && program.get(i).endsWith("\'"); 

	}

	private boolean isASCII(int i) {
		return program.get(i).startsWith("\"") && program.get(i).endsWith("\"");
	}

	private boolean isID(String s) {
		return !tokens.contains(s) && !isRegEx(s) && !isASCII(s);
	}

	private boolean isRegEx(String s) {
		return s.startsWith("\'") && s.endsWith("\'"); 

	}

	private boolean isASCII(String s) {
		return s.startsWith("\"") && s.endsWith("\"");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException, InputRuleMismatchException, RuleApplicabilityException, InvalidTokenException, InvalidProgramException {
		MiniReLL1Parser miniRE = new MiniReLL1Parser("minire_test_script.txt");
		// miniRE.parse("minire_test_script.txt");
	}

}

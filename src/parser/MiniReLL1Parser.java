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

	private LL1AST root;

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

	public String toString() {
		String ast = "";
		LL1AST curr = root;
		ast += root.getValue() + "\n";
		for (LL1AST child : curr.getChildren()) {
			if (child!= null) {
				ast += child.getValue() + "   ";
			}
		}
		ast += "\n";
		for (LL1AST child : curr.getChild(1).getChildren()) {
			if (child!= null) {
				ast += child.getValue() + "   ";
			}
		}
		curr = curr.getChild(1).getChild(0);
		ast += "\n";
		for (LL1AST child : curr.getChildren()) {
			if (child!= null) {
				ast += child.getValue() + "   ";
			}
		}
		return ast;
	}


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
			}
			node.setChildren(children);
			
			for (LL1AST child : children) {
				LL1AST grandchild = constructAST(child);
				if (grandchild.getValue().equals(child.getValue())) {
					boolean matchFound = false;
					Nonterminal nt = nonterminalMap.get(child.getValue());
					if (nt!=null) {
					for (String[] r : nt.getRules()) { // no left factor
						if (r[0].equals(child.getChild(0).getValue())) {
							matchFound = true;
						}
					}
					}
					if (!matchFound) {
						child.addChild(grandchild);
					}
				} else child.addChild(grandchild);
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
				List<LL1AST> children = new ArrayList<LL1AST>();
				if (rule.length==1 && rule[0].equals(EPSILON)) {
					node.addChild(new LL1AST(EPSILON, true));
				} else {
					pushRule(rule);
					// children = new ArrayList<LL1AST>();
					for (int i=0; i<rule.length; i++) {
						children.add(new LL1AST(rule[i]));
						// parsingStack.pop();
					}
					node.setChildren(children);
					/*
					for (LL1AST child : children) {
						child.addChild(constructAST(child));
						// input = inputStack.pop();
					} 
					*/
					for (LL1AST child : children) {
						LL1AST grandchild = constructAST(child);
						if (grandchild.getValue().equals(child.getValue())) {
							boolean matchFound = false;
							Nonterminal nt = nonterminalMap.get(child.getValue());
							if (nt!=null) {
							for (String[] r : nt.getRules()) { // no left factor
								if (r[0].equals(child.getChild(0).getValue())) {
									matchFound = true;
								}
							}
							}
							if (!matchFound) {
								child.addChild(grandchild);
							}
						} else child.addChild(grandchild);
					}
					
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
							LL1AST grandchild = constructAST(child); 
							child.addChild(grandchild);
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
		System.out.println(miniRE.toString());
	}

}

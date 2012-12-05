package parser.ll1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

import parser.ll1.ds.LL1ParsingTable;
import parser.ll1.ds.Nonterminal;

public class LL1Parser4MiniRE {

	private static String MINI_RE_SPEC = "MiniRE_Grammar3.txt";
	private static String EPSILON = "epsilon";
	private static String ASCII_STR = "ASCII-STR";


	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private ArrayList<String> tokens;	
	private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private HashMap<String,Nonterminal> nonterminalMap;
	private LL1ParsingTable table;
	private ArrayList<String> program;
	private Stack<String> inputStack;
	private Stack<String> parsingStack;

	public LL1Parser4MiniRE(String script) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		SimpleParserGenerator miniRE = new SimpleParserGenerator(MINI_RE_SPEC);
		table = miniRE.getParsingTable();
		nonterminals = miniRE.getNonterminals();
		start = miniRE.getStart();
		tokens = miniRE.getTokens();
		ruleMap = miniRE.getRuleMap();
		nonterminalMap = miniRE.getNonterminalMap();
		parse(script);
		run(program);

	}

	private void parse(String inputProgram) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(inputProgram)));
		String curr;
		String pStr = "";
		// program = new ArrayList<String>();
		while( (curr=br.readLine()) != null ) pStr += curr + " ";

		pStr = pStr.trim();

		if (pStr.length()==0) { 
			// error 
		}

		if (!pStr.startsWith("begin") && !pStr.endsWith("end")) {
			// error
		}

		program = split(pStr); 

	}

	private void run(ArrayList<String> program) {
		inputStack = new Stack<String>();
		parsingStack = new Stack<String>();

		for (int i=program.size()-1; i>=0; i--) inputStack.push(program.get(i));
		
		Nonterminal currNT = start;
		String curr = inputStack.peek();
		String[] rule = table.getRule(curr, currNT);
		while (!inputStack.isEmpty()) {

			if (rule==null) {
				// error
			}
			pushRule(rule);
			
			while (!parsingStack.isEmpty()) {
				String top = parsingStack.peek();
				
				if (top.equals("ep")) {
					
				} else if (nonterminalMap.containsKey(top)) { // nonterminal
					
				} else if (tokens.contains(top)) { // token
					
				} else {
					
				}
				// token
				// nonterminal
				// REGEX
				// epsilon
				// ASCII-STR
				// ID
			}
			
			if (isToken(curr)) {
				
				

				if (curr.equals(parsingStack.peek())) {
					inputStack.pop();
					parsingStack.pop();
					curr = inputStack.peek();
					if (isToken(curr)) continue;
				} else if (nonterminalMap.containsKey(parsingStack.peek())) {
					currNT = nonterminalMap.get(parsingStack.peek());
					rule = table.getRule(curr, currNT);
					parsingStack.pop();
					pushRule(rule);
				} else {
					// error
				}

			} else if (isRegEx(program.indexOf(curr))) {

			} else if (isASCII(program.indexOf(curr))) {

			} else if (isID(program.indexOf(curr))) {

			} else {
				// error
			}


		}

		for (int i=0; i<program.size(); i++) {
			if (i==0) {
				if (isToken(program.get(i))) {
					rule = table.getRule(program.get(i), start);
					if (rule==null) {
						// error
					}


				}
			} else if (i==program.size()-1) {
				if (program.get(i).equals("end")) {
					// terminate
				}
			} else {
				if (isToken(program.get(i))) {

				} else {
					if (isID(i)) {

					} else if (isRegEx(i)) {

					} else if (isASCII(i)) {

					} else {
						// error
					}

				}
			}
		}
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

	private Nonterminal findCorrespondingNonterminal(String ntStr) {
		for (Nonterminal nt : nonterminals) {
			if (nt.getValue().equals(ntStr)) return nt;
		}
		return null;
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

	private boolean isID(int i) {
		return tokens.contains(program.get(i)) && !isRegEx(i) && !isASCII(i);
	}

	private boolean isRegEx(int i) {
		return program.get(i).startsWith("\'") && program.get(i).endsWith("\'"); 

	}

	private boolean isASCII(int i) {
		return program.get(i).startsWith("\"") && program.get(i).endsWith("\"");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		LL1Parser4MiniRE miniRE = new LL1Parser4MiniRE("minire_test_script.txt");
		// miniRE.parse("minire_test_script.txt");
	}

}

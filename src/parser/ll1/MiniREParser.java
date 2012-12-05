package parser.ll1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

public class MiniREParser {

	private static String start = "<MiniRE-program>"; 

	private Stack inputStack;
	private LL1ParsingTable pt;
	private ArrayList<Terminal> terminals;
	private ArrayList<Nonterminal> nonterminals;
	private ArrayList<String> nonterminalsInStr;
	private ArrayList<String> terminalsInStr;
	private ArrayList<String> program;

	public MiniREParser(String miniRESpec) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		LL1ParserGenerator ll1 = new LL1ParserGenerator(miniRESpec);
		pt = ll1.getParsingTable();
		terminals = ll1.getTerminals();
		terminalsInStr = ll1.getTerminalsInString();
		nonterminals = ll1.getNonterminals();
		nonterminalsInStr = ll1.getNonterminalsInString();
		inputStack = new Stack();
	}

	public void parse(String inputProgram) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(inputProgram)));
		String curr;
		String pStr = "";
		// program = new ArrayList<String>();
		while( (curr=br.readLine()) != null ) { 
			pStr += curr + " ";
		}

		pStr = pStr.trim();

		if (pStr.length()==0) { 
			// error 
		}

		if (!pStr.startsWith("begin") && !pStr.endsWith("end")) {
			// error
		}

		program = split(pStr); 

		for (int i=0; i<program.size(); i++) {
			if (i==0) {
				if (isTerminal(program.get(i))) {
					String[] rule = pt.getRule(findCorrespondingTerminal(program.get(i)), findCorrespondingNonterminal(start));
					if (rule==null) {
						// error
					}
				}
			} else if (i==program.size()-1) {
				if (program.get(i).equals("end")) {
					// terminate
				}
			} else {
				if (isTerminal(program.get(i))) {

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

	private boolean isTerminal(String symbol) {
		for (Terminal t : terminals) {
			if (t.getValue().equals(symbol)) return true;
		}
		return false;
	}

	private Terminal findCorrespondingTerminal(String tStr) {
		for (Terminal t : terminals) {
			if (t.getValue().equals(tStr)) return t;
		}
		return null;
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
		return terminalsInStr.contains(program.get(i)) && !isRegEx(i) && !isASCII(i);
	}

	private boolean isRegEx(int i) {
		return program.get(i).startsWith("\'") && program.get(i).endsWith("\'"); 

	}

	private boolean isASCII(int i) {
		return program.get(i).startsWith("\"") && program.get(i).endsWith("\"");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		MiniREParser miniRE = new MiniREParser("MiniRE_Grammar2.txt");
		miniRE.parse("minire_test_script.txt");
	}

}

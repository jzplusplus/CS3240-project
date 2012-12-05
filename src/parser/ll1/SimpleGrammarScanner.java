package parser.ll1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

import parser.ll1.ds.Nonterminal;

/**
 * Scan grammar given in the follow form:
 * %% Tokens
 * 
 * @author Chris
 *
 */
public class SimpleGrammarScanner {

	private static final String SPEC_TYPE = "%%";
	private static final String TOKEN_SPEC = "Tokens";
	private static final String START_SPEC = "Start";
	private static final String RULE_SPEC = "Rules";

	// BNF form
	private static final String IS_DEFINED_AS = "::=";
	private static final String OR = "\\|";

	private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private ArrayList<String> tokens;
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private HashMap<String,Nonterminal> nonterminalMap;
	
	
	public SimpleGrammarScanner(String GrammarSpec) throws IOException, FileNotFoundException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		BufferedReader br = new BufferedReader(new FileReader(new File(GrammarSpec)));
		scanGrammar(br);
		constructRuleMap();
		constructNonterminalMap();
	}

	private void scanGrammar(BufferedReader spec) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException {
		String curr;
		tokens = new ArrayList<String>();
		nonterminals = new ArrayList<Nonterminal>();
		
		boolean readTokens = false;
		boolean readStart = false;
		boolean readRules = false;
		while( (curr=spec.readLine()) != null ) {
			if (curr.trim().length()!=0) {
				if(curr.startsWith(SPEC_TYPE)) {
					curr = curr.substring(SPEC_TYPE.length()).trim();
				}
				if (curr.startsWith(TOKEN_SPEC)) {
					readTokens = true;
				} else if (curr.startsWith(START_SPEC) ) {
					readTokens = false;
					readStart = true;
				} else if (curr.startsWith(RULE_SPEC) ) {
					readTokens = false;
					readStart = false;
					readRules = true;
				}

				if (readStart && start!=null) readStart = false;

				if (readTokens) readTokens(curr);

				if (readStart) readStartSymbol(curr);

				if (readRules) readRules(curr);
			}
		}
	}

	private void readTokens(String tokenStr) {
		// String tokenStr;
		if (tokenStr.startsWith(TOKEN_SPEC)) {
			tokenStr = tokenStr.substring(TOKEN_SPEC.length()).trim();

		} else {
			tokenStr = tokenStr.trim();
		}

		String[] toks = removeWhiteSpace(tokenStr);

		for (int i=0; i<toks.length; i++) {
			if (toks[i].trim().length()>0) tokens.add(toks[i].trim());
		}
	}

	/**
	 * Reads start symbol of the grammar
	 * 
	 * @param startSymbol
	 * @throws MultipleStartSymbolException
	 */
	private void readStartSymbol(String startSymbol) throws MultipleStartSymbolException {
		if (startSymbol.startsWith(START_SPEC)) {
			startSymbol = startSymbol.substring(START_SPEC.length()).trim();
		} 

		startSymbol = startSymbol.trim();
		if (startSymbol.length()>0) {
			if (isNonterminal(startSymbol) && start==null) {
				start = new Nonterminal(startSymbol);
			} else if (isNonterminal(startSymbol) && start!=null) {
				throw new MultipleStartSymbolException();
			}
		}

	}

	/**
	 * Reads rules assuming that given string is a line of rule
	 * 
	 * Can take care of |
	 * 
	 * @param rule
	 * @throws IncorrectRuleFormatException 
	 */
	private void readRules(String rule) throws IncorrectRuleFormatException {
		if (rule.startsWith(RULE_SPEC) ) rule = rule.substring(RULE_SPEC.length()).trim();

		rule = rule.trim();

		if (rule.length()>0) {
			String[] ruleStrArr = rule.split(IS_DEFINED_AS);
			
			// if there are multiple ->
			if (ruleStrArr.length!=2) throw new IncorrectRuleFormatException(); 

			// if the right hand side of the rule is not nonterminal
			if (!isNonterminal(ruleStrArr[0].trim())) throw new IncorrectRuleFormatException();

			Nonterminal currNT = new Nonterminal((ruleStrArr[0].trim()));
			
			String[] separatedRules = separateRules(ruleStrArr[1].trim());
			ArrayList<String[]> parsedRules = parseRules(separatedRules);
			currNT.addRules(parsedRules);
			if (!nonterminals.contains(currNT)) nonterminals.add(currNT);
			else {
				if (!nonterminals.get(nonterminals.indexOf(currNT)).getRules().contains(parsedRules)) {
					nonterminals.get(nonterminals.indexOf(currNT)).addRules(parsedRules);
				}
			}
		}
	} // end method readRules

	private ArrayList<String[]> parseRules(String[] rules) {
		ArrayList<String[]> parsed = new ArrayList<String[]>();
		for (int i=0; i<rules.length; i++) {
			String[] temp = parseRule(rules[i]);
			if (temp!=null) parsed.add(temp);
		}
		return parsed;
	}
	
	private String[] parseRule(String rule) {
		String temp = "";
		ArrayList<String> parsedTemp = new ArrayList<String>();
		boolean isNonterminal = false;
		if (rule!=null){
			for (Character c : rule.toCharArray()) {
				if (c!=' ') {
					if (c=='<') {
						if (temp.length()>0) parsedTemp.add(temp);
						isNonterminal = true;
						temp = "";
						temp += c;
					} else if (c=='>' && isNonterminal) {
						temp += c;
						isNonterminal = false;
						parsedTemp.add(temp);
						temp = "";
					} else { temp += c; }
				} else {
					if (temp.length()>0) {
						parsedTemp.add(temp);
						temp = "";
					}
				}
			}
			if (!parsedTemp.contains(temp) && temp.length()>0) parsedTemp.add(temp);
			return parsedTemp.toArray(new String[parsedTemp.size()]);
		}
		return null;
	}
	
	private void constructRuleMap() {
		ruleMap = new HashMap<Nonterminal,ArrayList<String[]>>();
		for (Nonterminal nt : nonterminals) {
			ruleMap.put(nt, nt.getRules());
		}
	}
	
	private void constructNonterminalMap() {
		nonterminalMap = new HashMap<String,Nonterminal>();
		for (Nonterminal nt : nonterminals) nonterminalMap.put(nt.getValue(), nt);
	}
	
	public String printRuleMap() {
		String rmStr = "";
		
		for (Nonterminal nt : nonterminals) {
			rmStr += nt.getValue() + "\n";
			ArrayList<String[]> rules = ruleMap.get(nt);
			for (String[] r : rules) {
				rmStr += "\t";
				for (int i=0; i<r.length; i++) {
					rmStr += r[i] + "  "; 
				}
				rmStr += "\n";
			}
			rmStr += "\n";
		}
		
		return rmStr;
	}
		
	public String printTokens() {
		String tStr = "tokens = {  ";
		
		for (String t : tokens) {
			tStr += t + "  ";
		}
		tStr += "}";
		return tStr;
	}
	
	public String printNonterminals() {
		String ntStr = "start: " + start.getValue() + "\nnonterminals = {  ";
		
		for (Nonterminal nt : nonterminals) {
			ntStr += nt.getValue() + "  ";
		}
		ntStr += "}";
		return ntStr;
	}
	
	private String[] separateRules(String rule) { return rule.split(OR); }

	private String[] removeWhiteSpace(String in) { return in.split("\\s"); }

	private boolean isNonterminal(String symbol) { return symbol.startsWith("<") && symbol.endsWith(">"); }
	
	// getters
	public ArrayList<Nonterminal> getNonterminals() { return nonterminals; }
	public ArrayList<String> getTokens() { return tokens; }
	public HashMap<Nonterminal,ArrayList<String[]>> getRuleMap() { return ruleMap; } 
	public Nonterminal getStartSymbol() { return start; }
	public HashMap<String,Nonterminal> getNonterminalMap() { return nonterminalMap; }
		
	public static void main(String[] arg) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		String filename = "MiniRE_Grammar3.txt";

		SimpleGrammarScanner scanner = new SimpleGrammarScanner(filename);
		System.out.println(scanner.printTokens());
		System.out.println(scanner.printNonterminals());
		System.out.println(scanner.printRuleMap());
		
	}
	
}

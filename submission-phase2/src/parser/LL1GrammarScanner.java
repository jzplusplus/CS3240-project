package parser;

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

/**
 * Scan grammar given in the follow form:
 * %% Tokens
 * 
 * @author Chris
 *
 */
public class LL1GrammarScanner {
	
	// special types
	private static final String EPSILON = "<epsilon>";
	private static final String REGEX = "REGEX";
	private static final String ASCII = "ASCII-STR";
	
	
	// BNF form
	private static final String IS_DEFINED_AS = "::=";
	private static final String OR = "\\|";

	private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private ArrayList<String> tokens;
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private HashMap<String,Nonterminal> nonterminalMap;
		
	private ArrayList<Character> singleCharacterTokens;
	
	public LL1GrammarScanner(String grammarSpec, String tokenSpec) throws IOException, FileNotFoundException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		BufferedReader gbr = new BufferedReader(new FileReader(new File(grammarSpec)));
		BufferedReader tbr = new BufferedReader(new FileReader(new File(tokenSpec)));
		scanToken(tbr);
		scanGrammar(gbr);
		constructRuleMap();
		constructNonterminalMap();
	}

	private void scanToken(BufferedReader spec) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException {
		String curr;
		tokens = new ArrayList<String>();
		while( (curr=spec.readLine()) != null ) {
			if (curr.trim().length()!=0) {
				readTokens(curr);
			}
		}
		tokens.add(ASCII);
		tokens.add(REGEX);
	}
	
	private void scanGrammar(BufferedReader spec) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException {
		String curr;
		nonterminals = new ArrayList<Nonterminal>();
		
		while( (curr=spec.readLine()) != null ) {
			if (curr.trim().length()!=0) {
				readRules(curr);
			}
		}

	}

	
	private void readTokens(String tokenStr) {
		tokenStr = tokenStr.trim();
		String[] toks = removeWhiteSpace(tokenStr);

		for (int i=0; i<toks.length; i++) {
			if (toks[i].trim().length()>0) {
				tokens.add(toks[i].trim());
				if (toks[i].trim().length()==1) {
					if (singleCharacterTokens==null) singleCharacterTokens = new ArrayList<Character>();
					singleCharacterTokens.add(toks[i].charAt(0));
				}
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
		rule = rule.trim();

		if (rule.length()>0) {
						
			String[] ruleStrArr = rule.split(IS_DEFINED_AS);
			
			if (nonterminals.isEmpty()) {
				start = new Nonterminal(ruleStrArr[0].trim());
			}
			
			// if there are multiple ::=
			if (ruleStrArr.length!=2) throw new IncorrectRuleFormatException(); 

			// if the right hand side of the rule is not nonterminal
			if (!isNonterminal(ruleStrArr[0].trim())) throw new IncorrectRuleFormatException();

			Nonterminal currNT = new Nonterminal((ruleStrArr[0].trim()));
			
			String[] separatedRules = separateRules(ruleStrArr[1].trim());
			ArrayList<String[]> parsedRules = parseRules(separatedRules);
			currNT.addRules(parsedRules);
			boolean rulesAdded = false;
			for (Nonterminal nt : nonterminals) {
				if (nt.getValue().equals(currNT.getValue())) {
					if (!nt.getRules().containsAll(currNT.getRules())) {
						nt.addRules(parsedRules);
						rulesAdded =true;
					}
				}
			}
			if (!rulesAdded && !nonterminals.contains(currNT)) nonterminals.add(currNT);
			
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
						if (temp.trim().length()>0) parsedTemp.add(temp.trim());
						isNonterminal = true;
						temp = "";
						temp += c;
					} else if (c=='>' && isNonterminal) {
						temp += c;
						isNonterminal = false;
						if (temp.trim().length()>0)	parsedTemp.add(temp.trim());
						temp = "";
					} else {
						if (singleCharacterTokens!=null) {
							if (singleCharacterTokens.contains(c)) {
								if (temp.trim().length()>0)	parsedTemp.add(temp.trim());
								parsedTemp.add(c.toString());
								temp = "";
							} else {
								temp += c;
							}
						} else {						
							temp += c;
						} 
					}
				} else {
					if (temp.length()>0) {
						if (temp.trim().length()>0)	parsedTemp.add(temp.trim());
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
	
	/*
	public static void main(String[] arg) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		// String filename = "MiniRE_Grammar3.txt";

		LL1GrammarScanner scanner = new LL1GrammarScanner("grammar.txt","token_spec.txt");
		System.out.println(scanner.printTokens());
		System.out.println(scanner.printNonterminals());
		System.out.println(scanner.printRuleMap());
		
	}
	*/
	
}

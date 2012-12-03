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

/**
 * Scan grammar given in the follow form:
 * %% Tokens
 * 
 * @author Chris
 *
 */
public class GrammarScanner {

	private static final String SPEC_TYPE = "%%";
	private static final String TOKEN_SPEC = "Tokens";
	private static final String START_SPEC = "Start";
	private static final String RULE_SPEC = "Rules";

	// BNF form
	private static final String IS_DEFINED_AS = "::=";
	private static final String OR = "\\|";

	private HashMap<Nonterminal,ArrayList<Rule[]>> ruleMapTemp;
	private HashMap<Nonterminal,Rule[]> ruleMap;
	private ArrayList<String> tokens;
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	
	public GrammarScanner(String GrammarSpec) throws IOException, FileNotFoundException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		BufferedReader br = new BufferedReader(new FileReader(new File(GrammarSpec)));
		scanGrammar(br);
		validateGrammar();
	}

	private void scanGrammar(BufferedReader spec) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException {
		String curr;
		tokens = new ArrayList<String>();
		nonterminals = new ArrayList<Nonterminal>();
		ruleMapTemp = new HashMap<Nonterminal,ArrayList<Rule[]>>();

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
			if (!nonterminals.contains(currNT)) {
				nonterminals.add(currNT);
			}
			
			if (ruleMapTemp.containsKey(currNT)) {
				if (ruleMapTemp.get(currNT)==null) ruleMapTemp.put(currNT, new ArrayList<Rule[]>());
			} else {
				ruleMapTemp.put(currNT, new ArrayList<Rule[]>());
			}
			
			
			Rule[] definedAs = convertToRules(separateRules(ruleStrArr[1].trim()));			
			if (!ruleMapTemp.get(currNT).contains(definedAs)) ruleMapTemp.get(currNT).add(definedAs);
		}
	} // end method readRules

	private void validateGrammar() throws UndefinedNonterminalException {
		if (!nonterminals.containsAll(ruleMapTemp.keySet())) throw new UndefinedNonterminalException();
		reconstructruleMapTemp();
		updateNonterminals();
	}
	
	private void reconstructruleMapTemp() {
		ruleMap = new HashMap<Nonterminal,Rule[]>();
		for (Nonterminal nt : ruleMapTemp.keySet()) {
			ArrayList<Rule> temp = new ArrayList<Rule>();
			for (Rule[] rules : ruleMapTemp.get(nt)) {
				for (int i = 0; i<rules.length; i++) temp.add(rules[i]);
			}
			ruleMap.put(nt, temp.toArray(new Rule[temp.size()]));
			
		}
	}
	
	private void updateNonterminals() {
		for (Nonterminal nt : ruleMap.keySet()) {
			System.out.println(nt.getValue());
			Rule[] rules = ruleMap.get(nt);
			for (int i = 0; i<rules.length; i++) System.out.println(rules[i].getValue());
			System.out.println();
			nt.setRules(ruleMap.get(nt));
		}
	}
	
	private String[] separateRules(String rule) { return rule.split(OR); }

	private String[] removeWhiteSpace(String in) { return in.split("\\s"); }

	private boolean isNonterminal(String symbol) { return symbol.startsWith("<") && symbol.endsWith(">"); }
	
	private Rule[] convertToRules(String[] ruleStrArr) {
		Rule[] ruleArr = new Rule[ruleStrArr.length];
		for (int i=0; i<ruleStrArr.length; i++) {
			ruleArr[i] = new Rule(ruleStrArr[i].trim());
		}
		return ruleArr;
	}
	
	// getters
	public ArrayList<Nonterminal> getNonterminals() { return nonterminals; }
	public ArrayList<String> getTokens() { return tokens; }
	public HashMap<Nonterminal,Rule[]> getRuleMap() { return ruleMap; } 
	public Nonterminal getStartSymbol() { return start; }
		
	


	
	public static void main(String[] arg) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		String filename = "MiniRE_Grammar2.txt";

		@SuppressWarnings("unused")
		GrammarScanner scanner = new GrammarScanner(filename);
	}
	
}

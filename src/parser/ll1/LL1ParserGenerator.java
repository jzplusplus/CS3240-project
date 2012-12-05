package parser.ll1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

/**
 * CS 3240 Project Phase II
 * 
 * LL(1) Parser generator for 
 * 
 */
public class LL1ParserGenerator {

	// BNF form
	private static final Rule EPSILON = new Rule(null);

	private static final String OR = "\\|";

	private static final String rest = "-rest>";

	private static final Rule ASCII_STR = new Rule("ASCII-STR");
	private static final Rule REGEX = new Rule("REGEX");

	private LL1ParsingTable parsingTable;

	private HashMap<Nonterminal,Rule[]> ruleMap;
	private ArrayList<String> tokens;
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private ArrayList<Terminal> terminals;

	public LL1ParserGenerator(GrammarScanner grammar) {
		ruleMap = grammar.getRuleMap();
		tokens = grammar.getTokens();
		start = grammar.getStartSymbol();
		nonterminals = grammar.getNonterminals();
		terminals = new ArrayList<Terminal>();
		generateTerminals();
		generateParser();
	}
	
	public LL1ParserGenerator(String grammarSpec) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		this(new GrammarScanner(grammarSpec));
	}
	
	
	public LL1ParsingTable getParsingTable() { return parsingTable; }
	
	public ArrayList<Nonterminal> getNonterminals() { return nonterminals; }

	public ArrayList<Terminal> getTerminals() { return terminals; }
	
	public ArrayList<String> getNonterminalsInString() { 
		ArrayList<String> ntStrAL = new ArrayList<String>();
		for (Nonterminal nt : nonterminals) ntStrAL.add(nt.getValue());
		return ntStrAL; 
	}

	public ArrayList<String> getTerminalsInString() {
		ArrayList<String> tStrAL = new ArrayList<String>();
		for (Terminal t : terminals) tStrAL.add(t.getValue());
		return tStrAL; 
	}
	
	private void generateTerminals() {
		terminals.add(new Terminal("ID"));
		terminals.add(new Terminal("ASCII-STR"));
		terminals.add(new Terminal("REGEX"));
		for (String t : tokens) {
			terminals.add(new Terminal(t));
		}
	}

	private void generateParser() {
		// leftFactor();
		generateFirst();
		generateFollow();
		generateParsingTable();
		System.out.println(parsingTable.toString());
	}

	private void generateFirst() {
		for (Nonterminal nt : nonterminals) {
			Rule[] rules = nt.getRules();
			for (int i = 0; i< rules.length; i++) {
				String[] parsedRules = rules[i].getParsedValues();
				if (isNonterminal(parsedRules[0])) {
					generateFirst(nt, nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[0]))).getRules());
				} else nt.addFirst(parsedRules[0]);
			}
		}
	}

	private void generateFirst(Nonterminal nt, Rule[] rules) {
		for (int i = 0; i< rules.length; i++) {
			String[] parsedRules = rules[i].getParsedValues();
			if (isNonterminal(parsedRules[0])) {
				generateFirst(nt, nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[0]))).getRules());
			} else nt.addFirst(parsedRules[0]);
		}
	}

	private void generateFollow() {
		for (Nonterminal nt : nonterminals) {
			if (start.equals(nt)) nt.addFollow("$");
			Rule[] rules = nt.getRules();
			for (int i=0; i<rules.length;i++) {
				String[] parsedRules = rules[i].getParsedValues();
				for (int j=0; j<parsedRules.length; j++) {
					if (j==parsedRules.length-1) {
						if (isNonterminal(parsedRules[j])) {
							if (!nt.getValue().equals(parsedRules[j]) ) {
								if (isNullable(nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))))) {
									if (nt.getFollow()!=null) {
										for (String s : nt.getFollow()) {
											nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(s);
										}
									}
								}
							}

						}
					} else {
						if (isNonterminal(parsedRules[j])) {
							if (isNonterminal(parsedRules[j+1])) {
								for (String s : nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j+1]))).getFirst()) {
									if (!s.equals("epsilon")) {
										nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(s);
									}
								}
							} else {
								nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(parsedRules[j+1]);
							}
						}
					}
				}
			}
		} // end for
		generateFollowPass2();
	}

	private void generateFollowPass2() {
		for (Nonterminal nt : nonterminals) {
			Rule[] rules = nt.getRules();
			for (int i=0; i<rules.length;i++) {
				String[] parsedRules = rules[i].getParsedValues();
				for (int j=0; j<parsedRules.length; j++) {
					if (j==parsedRules.length-1) {
						if (isNonterminal(parsedRules[j])) {
							if (!nt.getValue().equals(parsedRules[j]) ) {
								if (isNullable(nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))))) {
									if (nt.getFollow()!=null) {
										for (String s : nt.getFollow()) {
											nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(s);
										}
									}
								}
							}

						}
					} else {
						if (isNonterminal(parsedRules[j])) {
							if (isNonterminal(parsedRules[j+1])) {
								for (String s : nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j+1]))).getFirst()) {
									if (!s.equals("epsilon")) {
										nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(s);
									}
								}
							} else {
								nonterminals.get(nonterminals.indexOf(new Nonterminal(parsedRules[j]))).addFollow(parsedRules[j+1]);
							}
						}
					}
				}
			}
		} // end for
	}

	
	public String printFirst() {
		String first2str = "";
		for (Nonterminal nt : nonterminals) {
			first2str += "First(" + nt.getValue() + ") = {   ";
			for (String f : nt.getFirst()) first2str += f + "   ";
			first2str += "}\n";
		}
		return first2str;
	}
	
	public String printFollow() {
		String follow2str = "";
		for (Nonterminal nt : nonterminals) {
			follow2str += "Follow(" + nt.getValue() + ") = {   ";
			for (String f : nt.getFollow()) follow2str += f + "   ";
			follow2str += "}\n";
		}
		
		return follow2str;
	}
	
	private void generateParsingTable() {
		parsingTable = new LL1ParsingTable(terminals, nonterminals);
		for (Nonterminal nt : nonterminals) {
			for (String f : nt.getFirst()) {
				for (Terminal t : terminals) {
					if (t.getValue().equals(f)) {
						for (int i=0; i<nt.getRules().length; i++) {
							if (nt.getRules()[i].getParsedValues()[0].startsWith(f)) parsingTable.setRule(t, nt, nt.getRules()[i].getParsedValues());
						}
					}
				}
			}
		}
	}
	
		
	private boolean isNullable(Nonterminal nt) {
		for (int i=0; i<nt.getRules().length; i++) {
			for (int j=0; j<nt.getRules()[i].getParsedValues().length; j++) if (nt.getRules()[i].getParsedValues()[j].equals("epsilon")) return true;
		}
		return false;
	}

	private void parseRule(Nonterminal nt, Rule[] rules) {
		// removeImmediateLeftRecursion(nt,rules);
		for (Rule r : rules) {

		}

	}

	/**
	 * for i := 1 to m do
	 * for j := 1 to i-1 do
	 * replace each grammar rule choice of the form A_i -> A_jbeta by the rule
	 * 
	 * Assume that that rule contains immediate left recursion
	 */
	private void removeImmediateLeftRecursion(Nonterminal nt, Rule[] rules) {
		Nonterminal ntRest = createRest(nt);
		ArrayList<Rule> nonrecursiveRules = new ArrayList<Rule>();
		ArrayList<Rule> recursiveRules = new ArrayList<Rule>();
		recursiveRules.add(EPSILON);
		for (Rule r : rules) {
			Rule newRule = new Rule(r.getValue().substring(nt.getValue().length()).trim()+ntRest.getValue());
			if(isLeftRecursive(nt, r)) recursiveRules.add(newRule);
			else nonrecursiveRules.add(newRule);
		}

		ruleMap.put(ntRest, recursiveRules.toArray(new Rule[recursiveRules.size()]));
		ruleMap.remove(nt); 
		ruleMap.put(nt, nonrecursiveRules.toArray(new Rule[nonrecursiveRules.size()]));
	}

	/**
	 * Checks for immediate left recursion
	 * 
	 * @param nt
	 * @param rule
	 * @return
	 */
	private boolean isLeftRecursive(Nonterminal nt, Rule rule) { 
		if (!rule.getValue().startsWith("<")) return false;
		if (rule.getValue().startsWith(nt.getValue())) return true;
		return false; 
	}




	// private Nonterminal getFirstNonterminal(Rule rule) {}


	/**
	 * Removes common prefix if exists
	 */
	private void leftFactor() {
		for (Nonterminal nt : nonterminals) {
			ArrayList<String> firstSymbols = new ArrayList<String>();
			for (Rule r : nt.getRules()) {
				firstSymbols.add(r.getParsedValues()[0]);
			}
			String factor = null;
			int commonFactorCount = 0;
			int jCount = 0;
			for (int i = 0; i<firstSymbols.size() ; i++) {
				for (int j=(i+1)%firstSymbols.size(); jCount<firstSymbols.size() ; j = j%firstSymbols.size()) {
					jCount++;
					if (i!=j) {
						if (firstSymbols.get(i).equals(firstSymbols.get(j))) {
							factor = firstSymbols.get(i);
							commonFactorCount++;
						}
					}
				}
			}
			if (factor!=null) {
				String commonFactor = factor + "-factor";
				String[] factoredRuleStr = new String[2];
				factoredRuleStr[0] = factor;
				factoredRuleStr[1] = commonFactor;
				Rule[] unfactoredRules = nt.getRules();
				ArrayList<Rule> newRules = new ArrayList<Rule>();
				for (int i=0; i<unfactoredRules.length; i++) {
					if (!unfactoredRules[i].getParsedValues()[0].equals(factor)) newRules.add(unfactoredRules[i]);
					else {
						Rule commonFactorRemoved = removeCommonFactor(unfactoredRules[i],factor);
						newRules.add(commonFactorRemoved);
					}
				}
				nt.setRules(newRules.toArray(new Rule[newRules.size()]));				
			}
		}
	}
	
	private Rule removeCommonFactor(Rule rule, String commonFactor) {
		String[] parsedRule = rule.getParsedValues();
		Rule commonFactorRemoved = null;
		if (parsedRule.length==1) {
			if (parsedRule[0].equals(commonFactor)) commonFactorRemoved = new Rule("epsilon");
		} else {
			String[] newParsedRule = new String[parsedRule.length-1];
			for (int i=1; i<parsedRule.length; i++) {
				newParsedRule[i-1] = parsedRule[i];
			}
		}
		return commonFactorRemoved;
	}


	private String[] removeWhiteSpace(String in) { return in.split("\\s"); }

	private boolean isNonterminal(String symbol) { return symbol.startsWith("<") && symbol.endsWith(">"); }

	private boolean isASCIISTR(String symbol) { return symbol.equals(ASCII_STR.getValue()); }

	private boolean isREGEX(String symbol) { return symbol.equals(REGEX.getValue()); }

	private String[] separateRules(String rule) { return rule.split(OR); }

	/**
	 * Creates tail for left recursion
	 * 
	 * Note: -rest is suffixed instead of -tail
	 * 
	 * @param nt
	 * @return
	 */
	private Nonterminal createRest(Nonterminal nt) { return new Nonterminal(nt.getValue().substring(0, nt.getValue().length()-1) + rest); }

	public static void main(String[] arg) throws IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		// String filename = "MiniRE_Grammar2.txt";
		String filename = "test_grammar.txt";

		LL1ParserGenerator ll1 = new LL1ParserGenerator(new GrammarScanner(filename));
		System.out.println(ll1.printFirst());
		System.out.println();
		System.out.println(ll1.printFollow());
	}

}

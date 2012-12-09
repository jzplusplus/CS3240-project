package parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

public class LL1ParserGenerator {

	private static String EPSILON = "<epsilon>";
	
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals;
	private ArrayList<String> tokens;	
	private HashMap<Nonterminal,ArrayList<String[]>> ruleMap;
	private HashMap<String,Nonterminal> nonterminalMap;
	private LL1ParsingTable table;

	private HashMap<Nonterminal,ArrayList<String>> tempFollowMap;

	public LL1ParserGenerator(String grammarSpec, String tokenSpec) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		LL1GrammarScanner sgs = new LL1GrammarScanner(grammarSpec, tokenSpec);
		start = sgs.getStartSymbol();
		nonterminals = sgs.getNonterminals();
		tokens = sgs.getTokens();
		ruleMap = sgs.getRuleMap();
		nonterminalMap = sgs.getNonterminalMap();
		constructFirstSet();
		constructFollowSet();
		constructParsingTable();
	}

	// getters
	public LL1ParsingTable getParsingTable() { return table; }
	public ArrayList<Nonterminal> getNonterminals() { return nonterminals; }
	public HashMap<Nonterminal,ArrayList<String[]>> getRuleMap() { return ruleMap; }
	public ArrayList<String> getTokens() { return tokens; }
	public Nonterminal getStart() {return start;}
	public HashMap<String,Nonterminal> getNonterminalMap() { return nonterminalMap; }

	private void constructParsingTable() {
		table = new LL1ParsingTable(tokens, nonterminals);
		for (Nonterminal nt : nonterminals) {
			for (String tok : nt.getFirst()) {
				if (tok.equals(EPSILON)) {
					for (String follow : nt.getFollow()) {
						if (!follow.equals("$")) {
							for (String[] rule : nt.getRules()) {
								if (rule[0].equals(EPSILON)) {
									table.setRule(follow, nt, rule);
								}
							}
						}
					}
				} else {
					for (String t : tokens) {
						if (t.equals(tok)) {
							for (String[] rule : nt.getRules()) {
								if (isNonterminal(rule[0])) {
									for (String f : nonterminalMap.get(rule[0]).getFirst()) {
										if (f.equals(tok)) {
											table.setRule(t, nt, rule);
											break;
										}
									}
									// break;
								} else if (rule[0].equals(tok)) {
									table.setRule(t, nt, rule);
									break;
								}

							}
							break;
						}
					} // end for 
				}
			}
		}

	}

	private void constructFirstSet() {
		ArrayList<String> temp = new ArrayList<String>();
		for (Nonterminal nt : nonterminals) {
			ArrayList<String[]> rules = ruleMap.get(nt);
			for (String[] rule : rules) {
				if (rule.length>0) temp.add(rule[0]);
			}
			nt.setFirst(temp);
			temp = new ArrayList<String>();
		}
		constructFirstSetHelper();
	}

	private void constructFirstSetHelper() {
		while(!isFirstSetComplete()) {
			ArrayList<String> temp = new ArrayList<String>();
			for (Nonterminal nt : nonterminals) {
				for (String f : nt.getFirst()) {
					if (isNonterminal(f)) {
						Nonterminal nt2 = nonterminalMap.get(f);
						temp.addAll(nt2.getFirst());
					} else temp.add(f);
				}
				nt.setFirst(temp);
				temp = new ArrayList<String>();
			}
		}

	}

	private boolean isFirstSetComplete() {
		for (Nonterminal nt : nonterminals) {
			for (String f : nt.getFirst()) {
				if (isNonterminal(f)) return false;
			}
		}
		return true;
	}

	private void constructFollowSet() {
		for (Nonterminal nt : nonterminals) {
			if (nt.getValue().equals(start.getValue())) nt.addFollow("$");
			for (String[] rule : nt.getRules()) {
				for (int i=0; i<rule.length; i++) {
					if (isNonterminal(rule[i])) {
						if ((i+1)<rule.length) {
							if (isNonterminal(rule[i+1])) nonterminalMap.get(rule[i]).addFollows(nonterminalMap.get(rule[i+1]).getFirst());
							else nonterminalMap.get(rule[i]).addFollow(rule[i+1]);
						} else {
							if (isNonterminal(rule[i])) {
								if (nt.getFollow()!=null) {
									nonterminalMap.get(rule[i]).addFollows(nt.getFollow());
								}
							}
						}
					}
				}
			}
		}
		constructFollowSetHelper();
	}

	private void constructFollowSetHelper() {
		do {
			tempFollowMap = new HashMap<Nonterminal,ArrayList<String>>();
			for (Nonterminal nt : nonterminals) tempFollowMap.put(nt, nt.getFollow());

			for (Nonterminal nt : nonterminals) {
				for (String[] rule : nt.getRules()) {
					for (int i=0; i<rule.length; i++) {
						if (isNonterminal(rule[i])) {
							if ((i+1)<rule.length) {
								for (int j=i+1; j<rule.length; j++) {
									if (j == rule.length-1) {
										if (isNonterminal(rule[j])) {
											if (isNullable(nonterminalMap.get(rule[j]))) {
												nonterminalMap.get(rule[i]).addFollows(nt.getFollow());
											}
										}
									} else if (isNonterminal(rule[j])) {
										if (isNullable(nonterminalMap.get(rule[j]))) {
											nonterminalMap.get(rule[i]).addFollows(nonterminalMap.get(rule[j]).getFirst());
										} else {
											break;
										}
									}
									if (isNonterminal(rule[i+1])) nonterminalMap.get(rule[i]).addFollows(nonterminalMap.get(rule[i+1]).getFirst());
									else nonterminalMap.get(rule[i]).addFollow(rule[i+1]);
								}
							} else {
								if (isNonterminal(rule[i])) {
									if (nt.getFollow()!=null) {
										nonterminalMap.get(rule[i]).addFollows(nt.getFollow());
									}
								}
							}
						}
					}
				}
			}

		} while (!isFollowSetComplete());

	}

	private boolean isFollowSetComplete() {
		for (Nonterminal nt : nonterminals) {
			if (!nt.getFollow().equals(tempFollowMap.get(nt))) return false;
		}
		return true;
	}

	private String printFirstSet(Nonterminal nt) { return nt.printFirst();	}

	private String printFollowSet(Nonterminal nt) { return nt.printFollow(); } 

	private boolean isNonterminal(String symbol) { return symbol.startsWith("<") && symbol.endsWith(">") && !symbol.equals(EPSILON); }

	private boolean isNullable(Nonterminal nt) { return nt.getFirst().contains("epsilon"); }

	public String printFirstSets() {

		String fStr = "";
		for (Nonterminal nt : nonterminals) {
			fStr += printFirstSet(nt) + "\n";
		}
		return fStr;
	}

	public String printFollowSets() {

		String fStr = "";
		for (Nonterminal nt : nonterminals) {
			fStr += printFollowSet(nt) + "\n";
		}
		return fStr;
	}

	public String toString() { return table.toString(); }

	
	public static void main(String[] args) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		LL1ParserGenerator spg = new LL1ParserGenerator("grammar.txt","token_spec.txt");
		System.out.println(spg.printFirstSets());
		System.out.println(spg.printFollowSets());
		System.out.println(spg.toString());

	}
	

}

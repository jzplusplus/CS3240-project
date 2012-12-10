package parser;

import java.util.ArrayList;
import java.util.HashMap;

public class LL1ParsingTable {

	private ArrayList<Pair> pairs; 
	private HashMap<Pair,String[]> parsingTable; // Nonterminal terminal
	private ArrayList<String> terminals;
	private ArrayList<Nonterminal> nonterminals;

	public LL1ParsingTable(ArrayList<String> terminals, ArrayList<Nonterminal> nonterminals) {
		parsingTable = new HashMap<Pair,String[]>();
		pairs = new ArrayList<Pair>();
		this.terminals = terminals;
		this.nonterminals = nonterminals;
		initializeTable();
	}

	private void initializeTable() {
		for (String t : terminals) {
			for (Nonterminal nt : nonterminals) {
				pairs.add(new Pair(t,nt));
			}
		}
	}

	public void setRule(String t, Nonterminal nt, String[] rule) {
		for (Pair pair : pairs) {
			if (pair.getToken().equals(t) && pair.getNonterminal().equals(nt)) {
				parsingTable.put(pair, rule);
				break;
			}
		}
	}
	
	public String[] getRule(String t, Nonterminal nt) {
		for (Pair pair : pairs) {
			if (pair.getToken().equals(t) && pair.getNonterminal().equals(nt)) {
				return parsingTable.get(pair);
			}
		}
		return null;
	}

	public String toString() {
		String pt2str = "format: (<nonterminal>,terminal)\n\t<nonterminal> ::= rule\n\n";
		for (String t : terminals) {
			for (Nonterminal nt : nonterminals) {
				String[] rules = getRule(t,nt);
				if (rules!=null) {
					pt2str += "(" + nt.getValue() + "," + t + ")\n\t" + nt.getValue() + " ::= ";
					for (int i=0; i<rules.length; i++) pt2str += rules[i] + " ";
					pt2str += "\n";
				}
			}
		}
		return pt2str;
	}

	private class Pair {
		private String t;
		private Nonterminal nt;
		
		public Pair(String t, Nonterminal nt) {
			this.t = t;
			this.nt = nt;
		}

		public String getToken() { return t; }

		public Nonterminal getNonterminal() { return nt; }
		
		@Override
		public boolean equals(Object obj) {
			return nt.equals(((Pair) obj).getNonterminal()) && t.equals(((Pair) obj).getToken());
		}
	}	
}

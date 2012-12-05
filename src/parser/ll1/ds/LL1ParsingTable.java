package parser.ll1.ds;

import java.util.ArrayList;
import java.util.HashMap;

import parser.ll1.ds.Nonterminal;

public class LL1ParsingTable {

	private ArrayList<Pair> pairs; 
	private HashMap<Nonterminal,HashMap<String,String[]>> parsingTable;
	private ArrayList<String> terminals;
	private ArrayList<Nonterminal> nonterminals;

	public LL1ParsingTable(ArrayList<String> terminals, ArrayList<Nonterminal> nonterminals) {
		parsingTable = new HashMap<Nonterminal,HashMap<String,String[]>>();
		pairs = new ArrayList<Pair>();
		this.terminals = terminals;
		this.nonterminals = nonterminals;
		// initializeTable();
	}

	private void initializeTable() {
		for (String t : terminals) {
			for (Nonterminal nt : nonterminals) {
				pairs.add(new Pair(t,nt));
			}
		}
	}

	public void setRule(String t, Nonterminal nt, String[] rule) {
		parsingTable.put(nt, new HashMap<String,String[]>());
		parsingTable.get(nt).put(t, rule);
	}
	
	public String[] getRule(String t, Nonterminal nt) {
		if (parsingTable.containsKey(nt)) {
			if (parsingTable.get(nt).containsKey(t)) {
				return parsingTable.get(nt).get(t);
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
	}	
}

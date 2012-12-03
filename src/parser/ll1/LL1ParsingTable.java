package parser.ll1;

import java.util.ArrayList;

public class LL1ParsingTable {

	private ArrayList<LL1ParsingTableEntry> parsingTable;
	private ArrayList<Terminal> terminals;
	private ArrayList<Nonterminal> nonterminals;

	public LL1ParsingTable(ArrayList<Terminal> terminals, ArrayList<Nonterminal> nonterminals) {
		parsingTable = new ArrayList<LL1ParsingTableEntry>();
		this.terminals = terminals;
		this.nonterminals = nonterminals;
		initializeTable();
	}

	private void initializeTable() {
		for (Terminal t : terminals) {
			for (Nonterminal nt : nonterminals) {
				parsingTable.add(new LL1ParsingTableEntry(t,nt));
			}
		}
	}

	public String[] getRule(Terminal t, Nonterminal nt) {
		for (LL1ParsingTableEntry e : parsingTable) {
			if (e.getNonterminal().equals(nt) && e.getTerminal().equals(t)) return e.getRule();
		}
		return null;
	}

	public void setRule(Terminal t, Nonterminal nt, String[] rule) {
		for (LL1ParsingTableEntry e : parsingTable) {
			if (e.getNonterminal().equals(nt) && e.getTerminal().equals(t)) e.setRule(rule);
		}
	}

	public String toString() {
		String pt2str = "format: (<nonterminal>,terminal)\n\t<nonterminal> ::= rule\n\n";
		for (Terminal t : terminals) {
			for (Nonterminal nt : nonterminals) {
				String[] rules = getRule(t,nt);
				if (rules!=null) {
					pt2str += "(" + nt.getValue() + "," + t.getValue() + ")\n\t" + nt.getValue() + " ::= ";
					for (int i=0; i<rules.length; i++) pt2str += rules[i] + " ";
					pt2str += "\n";
				}
			}
		}

		return pt2str;
	}

	private class LL1ParsingTableEntry {
		private Terminal t;
		private Nonterminal nt;
		private String[] rule;

		public LL1ParsingTableEntry(Terminal t, Nonterminal nt, String[] rule) {
			this.t = t;
			this.nt = nt;
			this.rule = rule;
		}

		public LL1ParsingTableEntry(Terminal t, Nonterminal nt) {
			this(t, nt, null);
		}

		public void setRule(String[] rule) {
			this.rule = rule;
		}

		public Terminal getTerminal() { return t; }

		public Nonterminal getNonterminal() { return nt; }

		public String[] getRule() { return rule; }

	}
}

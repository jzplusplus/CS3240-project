package parser.ll1;

import java.util.ArrayList;

public class Nonterminal {

	private static final String EPSILON = null;

	private String value;
	private Rule[] rules;
	private ArrayList<String> first;
	private ArrayList<String> follow;

	public Nonterminal(String value) {
		this.value = value;
		first = new ArrayList<String>();
		follow = new ArrayList<String>();
	}

	public void addFirst(String f) { if (!first.contains(f)) first.add(f); }
	
	public boolean hasEpsilon() { return first.contains(EPSILON); }

	public void addFollow(String f) { if (!follow.contains(f)) follow.add(f); }

	public ArrayList<String> getFirst() { return first; }

	public ArrayList<String> getFollow() { return follow; }

	public String getValue() { return value; }
	
	public void setRules(Rule[] rules) { this.rules = rules; }

	public Rule[] getRules() { return rules; }
	
	public void setValue(String value) { this.value = value; }

	@Override
	public boolean equals(Object obj) { return ( ((Nonterminal) obj).getValue().equals(getValue()) );}


}

package parser;

import java.util.ArrayList;

public class Nonterminal {

	private static final String EPSILON = "epsilon";

	private String value;
	private ArrayList<String> first;
	private ArrayList<String> follow;
	private ArrayList<String[]> rules;

	public Nonterminal(String value) {
		this.value = value;
		first = new ArrayList<String>();
		follow = new ArrayList<String>();
		rules = new ArrayList<String[]>();
	}

	public void addRule(String[] rule) {
		if (rules.isEmpty()) rules.add(rule);
		else {
			if (!rules.contains(rule)) rules.add(rule); 
		}
	}
	
	public void addRules(ArrayList<String[]> rules) {
		if (!rules.isEmpty()) {
			for (String[] r : rules) {
				if (!this.rules.contains(r)) this.rules.add(r);
			}
		}
	}
	
	public void setRules(ArrayList<String[]> rules) { this.rules = rules; }
	
	public ArrayList<String[]> getRules() { return rules; }
	
	public void setFirst(ArrayList<String> first) { this.first = first; }
	
	public void setFollow(ArrayList<String> follow) { this.follow = follow; }

	public ArrayList<String> getFirst() { return first; }

	public ArrayList<String> getFollow() { return follow; }

	public String getValue() { return value; }
	
	public String printFirst() {
		String fStr = "First(" + value + ") = {  ";
		
		for(String f : first) {
			fStr += f + "  ";
		}
		fStr += "}";
		return fStr;
	}
	
	public String printFollow() {
		String fStr = "Follow(" + value + ") = {  ";
		
		for(String f : follow) {
			fStr += f + "  ";
		}
		fStr += "}";
		return fStr;
	}
	
	public void addFollow(String f) { 
		if (!follow.contains(f) && !f.equals(EPSILON)) follow.add(f);
	}
	
	public void removeFollow(String f) { 
		if (follow.contains(f)) follow.remove(f);
	}
	
	public void addFollows(ArrayList<String> follows) {
		for (String f : follows) {
			if (!follow.contains(f) && !f.equals(EPSILON)) follow.add(f);
		}
	}
	
	public void removeFollows(ArrayList<String> follows) { 
		for (String f : follows) {
			if (follow.contains(f)) follow.remove(f);
		}
	}
	
	public void setValue(String value) { this.value = value; }

	public boolean hasEpsilon() { return first.contains(EPSILON); }

	@Override
	public boolean equals(Object obj) {
		if ( !((Nonterminal) obj).getValue().equals(getValue()) ) return false;
		if ( !((Nonterminal) obj).getFirst().equals(getFirst()) ) return false;
		if ( !((Nonterminal) obj).getFollow().equals(getFollow()) ) return false;
		if ( !((Nonterminal) obj).getRules().equals(getRules()) ) return false;
		return true;
	}
}

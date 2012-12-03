package parser.ll1;

import java.util.ArrayList;

public class Rule {
	private String value;
	private String[] parsed;

	public Rule(String value) { 
		this.value = value; 
		parse();
	}

	/**
	 * Parse the rule into nonterminals and ones that are not nonterminals
	 */
	private void parse() {
		String temp = "";
		ArrayList<String> parsedTemp = new ArrayList<String>();
		boolean isNonterminal = false;
		if (value!=null){
			for (Character c : value.toCharArray()) {
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
			parsed = parsedTemp.toArray(new String[parsedTemp.size()]);
		}
	}

	public String getValue() { return value; }

	public String[] getParsedValues() { return parsed; }

	@Override
	public boolean equals(Object obj) { return ( ((Rule) obj).getValue().equals(getValue()) ); }

}

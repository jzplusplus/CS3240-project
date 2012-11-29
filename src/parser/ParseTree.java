package parser;

import java.util.ArrayList;
import java.util.List;

public class ParseTree {
	
	List<ParseTree> children = new ArrayList<ParseTree>();
	Symbol value;
	
	public ParseTree() {
		value = null;
	}
	public ParseTree(List<ParseTree> children) {
		this.children = children;
	}
	
	public ParseTree(Symbol value) {
		this.value = value;
	}
	
	public ParseTree(List<ParseTree> children, Symbol value) {
		this.children = children;
		this.value = value;
	}
	public List<ParseTree> getChildren() {
		return children;
	}
	public void setChildren(List<ParseTree> children) {
		this.children = children;
	}
	public Symbol getValue() {
		return value;
	}
	public void setValue(Symbol value) {
		this.value = value;
	}
	
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	
	public void addChildren(ParseTree... childrenToAdd) {
		if (children == null) {
			children = new ArrayList<ParseTree>();
		}
		for (ParseTree child : childrenToAdd) {
			children.add(child);
		}
	}
	
	public void addChild(ParseTree child) { // just a little syntactic sugar, because i keep typing this accidentally
		addChildren(child);
	}
	
	public void print() {
		print(0);
	}
	

	private void print(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("|   ");
		}
		System.out.println("+---" + getValue().getValue());
		for (int i = 0; i < this.children.size(); i++) {
			children.get(i).print(level + 1);
		}
	}
	
}

package parser;

import java.util.ArrayList;
import java.util.List;

public class LL1AST {

	List<LL1AST> children = new ArrayList<LL1AST>();
	String value;
	boolean isLeaf;
	
	public LL1AST() {
		value = null;
		isLeaf = false;
	}
	public LL1AST(List<LL1AST> children) {
		this.children = children;
		isLeaf = false;
	}
	
	public LL1AST(String value) {
		this.value = value;
		isLeaf = false;
	}
	
	public LL1AST(List<LL1AST> children, String value) {
		this.children = children;
		this.value = value;
		isLeaf = false;
	}
	
	public LL1AST(String value, boolean isLeaf) {
		this.value = value;
		this.isLeaf = isLeaf;
	}
	
	public List<LL1AST> getChildren() {
		return children;
	}
	public void setChildren(List<LL1AST> children) {
		this.children = children;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	
	public void addChildren(LL1AST... childrenToAdd) {
		if (children == null) {
			children = new ArrayList<LL1AST>();
		}
		for (LL1AST child : childrenToAdd) {
			children.add(child);
		}
	}
	
	public void addChild(LL1AST child) { // just a little syntactic sugar, because i keep typing this accidentally
		addChildren(child);
	}
	
	public LL1AST getChild(int index) {
		if (!hasChildren()) {
			return null;
		}
		return getChildren().get(index);
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
}

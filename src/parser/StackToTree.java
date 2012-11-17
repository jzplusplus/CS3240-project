package parser;

import java.util.ArrayList;
import java.util.Stack;

public class StackToTree {	
	private ArrayList<Tree<String>> trees;
	private ArrayList<String> operands;	
	
	public StackToTree(ArrayList<Stack<String>> stacks, ArrayList<String> ids) {		
		System.out.println("Stack To Tree");
		
		trees = new ArrayList<Tree<String>>();
		operands = new ArrayList<String>();
		
		operands.add("|");
		operands.add("*");
		operands.add("+");
		operands.add("-");
		operands.add("^");
		operands.add(".");
		operands.add("(");
		operands.add(")");
		operands.add("[");
		operands.add("]");
		
		for(int i=0; i<ids.size(); i++){
			trees.add(makeTree(ids.get(i), stacks.get(i)));			
		}		
	}
	
	private Tree<String> makeTree(String id, Stack<String> s) {		
		System.out.println("makeTree");
		
		Tree<String> result = new Tree<String>(id);		
		
		Node<String> current = result.getRoot();
		
		while(!s.isEmpty()) {
			String str = s.pop();
			
			if(operands.contains(str)){
				makeTreeHelper(current, str, s).setParent(current);
				System.out.println(str+ " added.");
				
			}else {
				current.addChild(new Node<String>(str, current));
				System.out.println(str+ " added.");
			}			
		}		
		
		return result;
	}
	
	private Node<String> makeTreeHelper(Node<String> parent, String id, Stack<String> s) {
		Node<String> current = parent;
		while(!s.isEmpty()) {
			String str = s.pop();
			
			if(operands.contains(str)){
				makeTreeHelper(current, id, s).setParent(parent);
				System.out.println(str+ " added.");
			}else {
				current.addChild(new Node<String>(str, parent));
				System.out.println(str+ " added.");
			}				
		}
		
		return current;			
	}
		
	public ArrayList<Tree<String>> getTrees(){
		return trees;
	}
	
	public void printTree(Node<String> head) {
		if(head.hasChild()) {
			for(Node<String> child: head.getChildren()) {
				printTree(child);
			}
		}else {
			System.out.println("Parent: " + head.getParent().getData() + " / Current Node: " + head.getData());			
		}		
	}
	
}

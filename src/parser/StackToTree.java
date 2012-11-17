package parser;

import java.util.ArrayList;

public class StackToTree {	
	private ArrayList<Tree<String>> trees;
		
	public StackToTree(ArrayList<ArrayList<String>> qs, ArrayList<String> ids) {		
		trees = new ArrayList<Tree<String>>();
		
		for(int i=0; i<ids.size(); i++){
			trees.add(makeTree(ids.get(i), qs.get(i)));			
		}		
	}
	
	private Tree<String> makeTree(String id, ArrayList<String> q) {		
		
		int index = 0;
		Tree<String> result = new Tree<String>(id);
		Node<String> current = result.getRoot();
		
		while(!q.isEmpty()) {  
			index = findNext(q);
			
			if(q.get(index).equals("(") || q.get(index).equals("[")) {
				ArrayList<String> cut = ListCut(q, index+1, findEndIndex(q.get(index), index, q)-1);
				if(current.getL()==null)
					current.setL(makeTree("PAREN", cut).getRoot());
				else if(current.getR()==null)
					current.setR(makeTree("PAREN", cut).getRoot());
				
				q.remove(index);
				q.remove(findEndIndex(q.get(index), index, q));
				
			}else {
				Node<String> nd = new Node<String>(q.get(0), current);
				Node<String> nd = new Node<String>(q.get(1), current);
				
				
			}
		}
		
		return result;
	}
	
	private ArrayList<String> ListCut(ArrayList<String> q, int start, int end) {
		ArrayList<String> temp = new ArrayList<String>();
		for(int i=start; i<=end; i++) {
			temp.add(q.get(i));
		}
		
		return temp;
	}
	
	private int findEndIndex(String s, int start, ArrayList<String> q) {
		int end = q.size() - 1; // By default
		
		for(String str: q){
			if(s.equals("(") && str.equals(")"))
				end = q.indexOf(str);
			else if(s.equals("[") && str.equals("]"))
				end = q.indexOf(str);			
		}
		
		return end;
	}
	
	private int findNext(ArrayList<String> q) {
		int index = 0;
		int highestPrec = 0;
		int currentPrec = 0;
		
		String str;
		for(int i=0; i<q.size(); i++) {
			str = q.get(i);
			
			switch(str) {
				case "(":
					currentPrec = 3;
				case "[":
					currentPrec = 3;
				case "|":
					currentPrec = 1;
				case "-":
					currentPrec = 1;
				case "*":
					currentPrec = 2;
				case "+":
					currentPrec = 1;
				case "^":
					currentPrec = 2;
				case ".":
					currentPrec = 1;
			}
			
			if(currentPrec > highestPrec){
				highestPrec = currentPrec;
				index = i;
			}			
		}
				
		return index;
	}
	
	public ArrayList<Tree<String>> getTrees(){
		return trees;
	}

	
	
	
}

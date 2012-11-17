package parser;

import java.util.ArrayList;

public class Node<T> {
	
    private T data;
    private Node<T> parent;
    private ArrayList<Node<T>> children;
    
    public Node(T data, Node<T> parent){
    	this.data = data;
    	this.parent = parent;
    	children = new ArrayList<Node<T>>();
    }
    
    public T getData(){
    	return data;
    }
    
    public Node<T> getParent(){
    	return parent;
    }
    
    public void addChild(Node<T> child){
    	children.add(child);
    }
    
    public boolean hasChild() {
    	if(children.isEmpty())
    		return false;
    	return true;
    }
    
    public ArrayList<Node<T>> getChildren(){
    	return children;        	
    }
    
    public void setData(T data){
    	this.data = data;
    }
    
    public void setParent(Node<T> parent){
    	this.parent = parent;
    }
    
    public void setChildren(ArrayList<Node<T>> children){
    	this.children = children;        	
    }
    
}

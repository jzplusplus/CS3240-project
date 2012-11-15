package parser;

import java.util.List;

public class Node<T> {
	
    private T data;
    private Node<T> parent;
    private List<Node<T>> children;
    
    public Node(T data, Node<T> parent){
    	this.data = data;
    	this.parent = parent;
    }
    
    public T getData(){
    	return data;
    }
    
    public Node<T> getParent(){
    	return parent;
    }
    
    public List<Node<T>> getChildren(){
    	return children;        	
    }
    
    public void setData(T data){
    	this.data = data;
    }
    
    public void setParent(Node<T> parent){
    	this.parent = parent;
    }
    
    public void setChildren(List<Node<T>> children){
    	this.children = children;        	
    }
    
}

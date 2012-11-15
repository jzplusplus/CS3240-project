package parser;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
	
    private Node<T> root;
    private T data;
    private List<Node<T>> children;

    public Tree(T rootData) {
        root = new Node(rootData, null);
        data = rootData;
        children = new ArrayList<Node<T>>();
    }
      
    public Node<T> getRoot(){
    	return root;    	
    }
    
    public void setRoot(Node<T> root){
    	this.root = root;
    }
    
    public T getData(){
    	return data;
    }
    
    public List<Node<T>> getChildren(){
    	return children;
    }
    
    public void setData(T data){
    	this.data = data;
    }
    
    public void setChildren(List<Node<T>> children){
    	this.children = children;
    }
    
}
package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileToList {
	
	private String filepath;
	private ArrayList<String> inputs = new ArrayList<String>();
	
	public FileToList(String filepath) {
		
		this.filepath = filepath;
		
		Scanner sc;
		try {			
			sc = new Scanner(new File(filepath));
			String chr = ""; // one character			
			String sentence = ""; // one sentence
			
			while(sc.hasNext()) {					
				chr = sc.next();
				
				if(chr.compareTo("\n")!=0) { // if it is not a new line				
					sentence = sentence + chr; // keep making a sentence				
				} else { // else				
					inputs.add(sentence); // add the sentence to a list
					sentence = ""; // empty the sentence					
				}	
			}		
			
			sc.close();
		
		} catch (FileNotFoundException e) {
			System.out.println("Filepath was not valid.");
			System.exit(0);			
		}				
	}
	
	public ArrayList<String> getInputs() {		
		return inputs;		
	}
	
	public String getFilepath() {		
		return filepath;		
	}
	
	public void setFilepath(String filepath) {		
		this.filepath = filepath;		
	}
		
}

package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileToInput {
	
	private String filepath;
	private ArrayList<String> inputs;
	
	public FileToInput(String filepath){
		this.filepath = filepath;
	
		Scanner sc;
		try {			
			sc = new Scanner(new File(filepath));
			String line;
			
			while(sc.hasNextLine()) {									
				line = sc.nextLine();
				
				if(line.length()!=0){
					inputs.add(line);				
				}			
				
			}			
			
		}catch(FileNotFoundException e){			
			System.out.println("Filepath was not valid.");
			System.exit(0);		
		}
					
	}

	public ArrayList<String> getInputs(){
		return inputs;
	}
	
	public String getFilepath() {		
		return filepath;		
	}
	
	public void setFilepath(String filepath) {		
		this.filepath = filepath;		
	}
	
}

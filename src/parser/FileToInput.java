package parser;

import java.util.Scanner;

public class FileToInput {
	
	 private Scanner input;
     private String buffer;
     private int currentpos;
     private boolean peek;
     
     /** 
      * @param String of a current line
      */
     public FileToInput(Scanner input) {
    	 this.input = input;
         this.buffer = input.nextLine();
         this.currentpos = 0;
         this.peek = false;
     }
     
     public FileToInput(Scanner input, String buffer, int currentpos, boolean peek) {
    	 this.input = input;
    	 this.currentpos = currentpos;
    	 this.buffer = buffer;
         this.peek = peek;
     }
     
     /**
      * @return the next character
      */
     public char getNext() {
         if((this.currentpos >= buffer.length()) || (buffer.length() == 0)) {
        	 return '\n';
         }
             
         if(peek) {
        	 this.peek = false;
        	 return buffer.charAt(currentpos);
         }
         else {
        	 return this.buffer.charAt(currentpos++);
         }
     }
     
     /**
      * @return the next token 
      */
     public char peekNext() {
    	 if(this.currentpos >= buffer.length()) {
    		 return '\n';
         }
         if(peek) {
        	 return buffer.charAt(currentpos);
         }
         else {
        	 peek = true;
        	 return getNext();
         }
     }
     
     /**
      */
     public boolean gotoNextLine() {
    	 peek = false;
         if(this.input.hasNextLine()) {
        	 this.buffer = input.nextLine();
             this.currentpos = 0;
             this.peek = false;
             return true;
         }
         else {
             return false;
         }
     }
     
     public int getPos() {
    	 return currentpos;
     }
     
     public boolean getPeek() {
    	 return peek;
     }
     
     public Scanner getInput() {
    	 return input;
     }
     
     public String getBuffer() {
    	 return buffer;
     }
	
}

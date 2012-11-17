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
	
}

package parser.ll1;

public class Terminal {
	  private String value;

	  public Terminal(String value) {
	    this.value = value;
	  }

	  public String getValue() { return value; }
	  
	  public boolean isTerminal() { return true; }
	  
	  @Override
	  public boolean equals(Object obj) { return ( ((Terminal) obj).getValue().equals(getValue()) ); }

}

package parser;

public class Token {
	  private Object type;
	  private Object value;

	  public Token(Object type, Object value) {
	    this.type  = type;
	    this.value = value;
	  }

	  public Object getValue() { return value; }

	  public Object getType()  { return type;  }

}

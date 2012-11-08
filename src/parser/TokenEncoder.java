package parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenEncoder {
	
	private ArrayList<TokenClassInterface> tokenList;
	private Pattern separator;
	private Matcher matcher;
	
	public static final String DEFAULT_SEPARATOR = "\\s+";
	 
	public TokenEncoder(Collection<TokenClassInterface> token_classes, String separator) {
		tokenList = new ArrayList<TokenClassInterface>(token_classes);
		this.separator = Pattern.compile(separator);
	    matcher = this.separator.matcher("");
	}
	
	public TokenEncoder(Collection<TokenClassInterface> token_types) {
		this(token_types, DEFAULT_SEPARATOR);
	}
	
	public void setInput(CharSequence input) {
		matcher.reset(input);
	}
	
	public Token nextToken() throws TokenNotFoundException {
		    
		Iterator<TokenClassInterface> iter;

		matcher.usePattern(separator); // skip separator(s)
		if(matcher.lookingAt()) {
			matcher.region(matcher.end(), matcher.regionEnd());
		}

		iter = tokenList.iterator();

		while(iter.hasNext()) {
			TokenClassInterface token_type = iter.next();

		    matcher.usePattern(token_type.getPattern());

		    if(matcher.lookingAt()) {
		    	Token token = new Token(token_type.getType(), matcher.group());
		        matcher.region(matcher.end(), matcher.regionEnd());
		        return token;
		    }
		}
		
		if(matcher.hitEnd()) {
			return null;
		}
		
		throw new TokenNotFoundException();

	 }
	
}

package parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Tokenizer {
  private ArrayList<TokenType> tokenList;
  private Pattern separator;
  private Matcher matcher;

  public static final String DEFAULT_SEPARATOR = "\\s+";

  public Tokenizer(Collection<TokenType> token_types, String separator) {
    tokenList = new ArrayList<TokenType>(token_types);
    this.separator = Pattern.compile(separator);
    matcher = this.separator.matcher("");
  }

  public Tokenizer(Collection<TokenType> token_types) {
    this(token_types, DEFAULT_SEPARATOR);
  }

  public void setInput(CharSequence input) {
    matcher.reset(input);
  }

  public Token nextToken() throws TokenNotFoundException {
    Iterator<TokenType> it;

    // Skip over any separators
    matcher.usePattern(separator);
    if(matcher.lookingAt()) {
      matcher.region(matcher.end(), matcher.regionEnd());
    }

    it = tokenList.iterator();

    while(it.hasNext()) {
      TokenType token_type = it.next();

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
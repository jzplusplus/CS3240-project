package parser;

import java.util.Scanner;

public class Tokenizer {

    private FileToInput input_stream;
    private boolean peek;
    private Token current;
    
    /**
     * setup lexer for given scanner input
     * @param input scanner to tokenize from
     */
    public Tokenizer(Scanner scanner) {
            this.input_stream = new FileToInput(scanner);
            this.peek = false;
            this.current = null;
    }

    /**
     * get the next token in the stream
     * @return next token in stream
     */
    public Token getNextToken() {
            if(peek) {
                    peek = false;
                    return current;
            }
            else {
                    current = makeNewToken();
                    return current;
            }
    }
    
    /**
     * peek at the next token in the stream
     * @return next token in the stream
     */
    public Token peekNextToken() {
            if(peek) {
                    return current;
            }
            else {
                    current = getNextToken();
                    peek = true;
                    return current;
            }
    }
    
    /**
     * make a new token from the stream
     * @return new token
     */
    public Token makeNewToken() {
            char t = input_stream.getNext();
            
            Token result = null;
            
            switch(t) {
                    //ignore comment lines
                    case '%':
                            if(input_stream.peekNext() == '%') {
                                    input_stream.gotoNextLine();
                                    return makeNewToken();
                            }
                            else {
                                    result = new Token(TokenType.LITERAL, "%");
                            }
                            break;
                    //ignore whitespace
                    case '\t':
                    case ' ':
                            return makeNewToken();
                    //handle possible line returns
                    case '\n':
                            result = new Token(TokenType.EOL, "\n");
                            break;
                    //defined name
                    case '$':
                            String name = new String();
                            while(validDefinedCharacters()) {

                                    name += input_stream.getNext();
                            }
                            //input_stream.getNext();//consume whitespace
                            result = new Token(TokenType.DEFINED, name);
                            break;
                    //alternation
                    case '|':
                            result = new Token(TokenType.UNION, "|");
                            break;
                    //repetition >= 0
                    case '*':
                            result = new Token(TokenType.KLEENE, "*");
                            break;
                    //repetition > 0
                    case '+':
                            result = new Token(TokenType.PLUS, "+");
                            break;
                    //dash (used when defining a range)
                    case '-':
                            result = new Token(TokenType.DASH, "-");
                            break;
                    //caret (exclude set)
                    case '^':
                            result = new Token(TokenType.CARET, "^");
                            break;
                    //dot (wild card)
                    case '.':
                            result = new Token(TokenType.DOT, ".");
                            break;
                    //left bracket
                    case '[':
                            result = new Token(TokenType.LBRACKET, "[");
                            break;
                    //right bracket
                    case ']':
                            result = new Token(TokenType.RBRACKET, "]");
                            break;
                    //left parentheses (scope out)
                    case '(':
                            result = new Token(TokenType.LPAREN, "(");
                            break;
                    //right parentheses (scope in)
                    case ')':
                            result = new Token(TokenType.RPAREN, ")");
                            break;
                    //IN (for defining ranges)
                    case 'I':
                            if(input_stream.peekNext() == 'N') {
                                    result = new Token(TokenType.IN, "IN");
                                    input_stream.getNext();
                            }
                            else {
                                    result = new Token(TokenType.LITERAL, "L");
                            }
                            break;
                    //escaped characters
                    case '\\':
                            String escaped = "\\" + input_stream.getNext();
                            result = new Token(TokenType.LITERAL, escaped);
                            break;
                    //everything else (character literals)
                    default:
                            result = new Token(TokenType.LITERAL, new String() + t);
            }
            
            return result;
    }
    
    /**
     * moves the stream to the next line of the file
     * @return true: there is another line, false: end of file
     */
    public boolean gotoNextLine() {
            peek = false;
            return input_stream.gotoNextLine();
    }
    
    /**
     *
     * @return 
     */
    public boolean validDefinedCharacters() {
            int t = ((int)input_stream.peekNext());
            if((t >= 48 && t <= 57) || (t >= 65 && t <= 90) || (t >= 97 && t <= 122) || (t == 45) || (t == 95)) {
                    return true;
            }
            else {
                    return false;
            }
    }
}
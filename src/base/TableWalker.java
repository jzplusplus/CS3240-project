package base;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import parser.Token;
import util.DFA;
import util.State;
import exception.BadTokenException;
import exception.EOFException;


public class TableWalker {
	
	private FileReader reader;
	private String backBuffer;
	private Map<String, DFA> types;
	
	public TableWalker(String filepath, Map<String, DFA> types) throws FileNotFoundException
	{
		reader = new FileReader(filepath);
		backBuffer = "";
		this.types = types;
	}
	
	public Token nextToken() throws IOException, BadTokenException, EOFException
	{
		String currentToken = "";
		Token longestValidToken = null;
		
		for(DFA dfa: types.values())
		{
			dfa.reset();
		}
		
		while(true)
		{
			char next;
			if(backBuffer.isEmpty()) //no unused characters from last call
			{
				int nextInt = reader.read();
				if(nextInt == -1)
				{
					if(!currentToken.isEmpty()) break;
					else
					{
						throw new EOFException();
					}
				}
				
				next = (char)nextInt;
			}
			else 
			{
				next = backBuffer.charAt(0);
				backBuffer = backBuffer.substring(1);
			}
			
			if( (next == ' ') ||
				(next == '\t') ||
				(next == '\n') ||
				(next == '\r') )
			{
				if(!currentToken.isEmpty()) break;
				else
				{
					continue;
				}
			}
			
			currentToken += next;
			for(String type: types.keySet())
			{
				DFA currentDFA = types.get(type);
				
				State state = currentDFA.doTransition(next);
					
				if(state.isAccepting())
				{
					longestValidToken = new Token(type, currentToken);
				}
			}
		}
		
		if(longestValidToken == null)
		{
			throw new BadTokenException(currentToken);
		}
		else
		{
			int tokenLength = ((String)longestValidToken.getValue()).length();
			backBuffer = currentToken.substring(tokenLength);
			return longestValidToken;
		}
	}
}

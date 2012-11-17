import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import parser.Token;

import util.DFA;
import util.State;


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
		List<String> validTypes = new ArrayList<String>(types.keySet());
		
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
			
			List<String> tempTypes = new ArrayList<String>(validTypes);
			currentToken += next;
			for(String type: tempTypes)
			{
				DFA currentDFA = types.get(type);
				try
				{
					State state = currentDFA.doTransition(next);
					
					if(state.isAccepting())
					{
						longestValidToken = new Token(type, currentToken);
					}
				}
				catch(NullPointerException e)
				{
					validTypes.remove(type);
				}
			}
			
			if(validTypes.size()==0) break;
		}
		
		for(DFA dfa: types.values())
		{
			dfa.reset();
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

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
	private Map<String, DFA> types;
	
	public TableWalker(String filepath, Map<String, DFA> types) throws FileNotFoundException
	{
		reader = new FileReader(filepath);
		this.types = types;
	}
	
	public Token nextToken() throws IOException, BadTokenException
	{
		String currentToken = "";
		Token longestValidToken = null;
		List<String> validTypes = new ArrayList<String>(types.keySet());
		
		while(true)
		{
			int nextInt = reader.read();
			if(nextInt == -1) break;
			
			char next = (char)nextInt;
			if( (next == ' ') ||
				(next == '\t') ||
				(next == '\n') ||
				(next == '\r') )
			{
				break;
			}
			
			List<String> tempTypes = new ArrayList<String>(validTypes);
			for(String type: tempTypes)
			{
				DFA currentDFA = types.get(type);
				try
				{
					State state = currentDFA.doTransition(next);
					currentToken += next;
					if(state.isAccepting())
					{
						reader.mark(255); //mark every time we find a new valid token, so we can reset to here later
											//and put the unused characters back in the stream
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
			reader.reset(); //only reset if we did find some token. Otherwise, we'd just end up looping on a
							//bad character
			return longestValidToken;
		}
	}
}

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.DFA;
import util.State;


public class TableWalker {
	
	private FileReader reader;
	private List<DFA> dfaList;
	
	public TableWalker(String filepath, List<DFA> dfaList) throws FileNotFoundException
	{
		reader = new FileReader(filepath);
		this.dfaList = dfaList;
	}
	
	public String nextToken() throws IOException, BadTokenException
	{
		String currentToken = "";
		String longestValidToken = null;
		DFA validTokenType = null;
		List<DFA> possibleTypes = new ArrayList<DFA>(dfaList);
		
		reader.mark(255); //mark where we start scanning
		
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
			
			for(int i=0; i<possibleTypes.size(); i++)
			{
				DFA currentType = possibleTypes.get(i);
				State state = currentType.doTransition(next);
				if(state == null)
				{
					possibleTypes.remove(i);
					i--;
				}
				else
				{
					currentToken += next;
					if(state.isAccepting())
					{
						reader.mark(255); //mark every time we find a new valid token, so we can reset to here later
											//and put the unused characters back in the stream
						longestValidToken = currentToken;
						validTokenType = currentType;
					}
				}
			}
			
			if(possibleTypes.size()==0) break;
		}
		
		reader.reset();
		
		if(longestValidToken == null)
		{
			throw new BadTokenException(currentToken);
		}
		else
		{
			return longestValidToken;
		}
	}
}

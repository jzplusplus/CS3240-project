import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.DFA2;
import util.State;


public class TableWalker {
	
	private FileReader reader;
	private List<DFA2> dfaList;
	
	public TableWalker(String filepath, List<DFA2> dfaList) throws FileNotFoundException
	{
		reader = new FileReader(filepath);
		this.dfaList = dfaList;
	}
	
	public String nextToken() throws IOException, BadTokenException
	{
		String currentToken = "";
		String longestValidToken = null;
		DFA2 validTokenType = null;
		List<DFA2> possibleTypes = new ArrayList<DFA2>(dfaList);
		
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
				DFA2 currentType = possibleTypes.get(i);
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
						reader.mark(255);
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

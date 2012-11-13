import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import util.DFA;


public class TableWalker {
	
	private FileReader reader;
	
	public TableWalker(String filepath) throws FileNotFoundException
	{
		reader = new FileReader(filepath);
	}
	
	public String nextToken(DFA dfa) throws IOException, BadTokenException
	{
		String token = "";
		boolean endOfToken = false;
		while(!endOfToken)
		{
			int nextInt = reader.read();
			if(nextInt == -1) endOfToken = true;
			else
			{
				char next = (char)nextInt;
				if( (next == ' ') ||
					(next == '\t') ||
					(next == '\n') ||
					(next == '\r') )
				{
					endOfToken = true;
				}
				else{
					token = token + next;
				}
			}
		}
		
		if(dfa.canAccept(token))
		{
			return token;
		}
		else throw new BadTokenException(token);
	}
}

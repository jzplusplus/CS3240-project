import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parser.Token;

import util.DFA;
import util.NFA;
import util.State;

public class TableWalkerTest {

	public static class FakeDFA extends DFA {

		private String match; //for testing purposes, just match an exact string instead of a DFA
		private int i;
		
		public FakeDFA(String match)
		{
			super(new NFA(), new State());
			this.match = match;
			i=0;
		}
		
		public State doTransition(Character input)
		{
			if(i == match.length()) i = 0;
			char current = match.charAt(i);
			i++;
			if(current == input)
			{
				if(i == match.length()) return new State(true);
				else return new State(false);
			}
			else
			{
				throw new NullPointerException();
			}
		}
		
		public void reset()
		{
			i = 0;
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DFA dfa, dfa2, dfa3, dfa4;
		
		dfa = new FakeDFA("ababbb");
		dfa2 = new FakeDFA("abab");
		dfa3 = new FakeDFA("baaa");
		dfa4 = new FakeDFA("21.3");
		
		Map<String, DFA> types = new HashMap<String, DFA>();
		types.put("Type1", dfa);
		types.put("Type2", dfa2);
		types.put("Type3", dfa3);
		types.put("Type4", dfa4);
		try {
			TableWalker tw = new TableWalker("tabletest.txt", types);
			while(true)
			{
				Token token = tw.nextToken();
				System.out.println((String)token.getType() + ": " + (String)token.getValue());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadTokenException e) {
			System.out.println("Bad Token!");
			System.out.println(e.getPartialToken());
		} catch (EOFException e) {
			System.out.println("END OF FILE");
		}
	}
}

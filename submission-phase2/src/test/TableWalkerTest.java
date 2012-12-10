package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import base.TableWalker;

import parser.Token;
import util.DFA;
import util.NFA;
import util.State;
import exception.BadTokenException;
import exception.EOFException;

public class TableWalkerTest {

	public static class FakeDFA extends DFA {

		private String match; //for testing purposes, just match an exact string instead of a DFA
		private int i;
		private boolean failed;
		
		public FakeDFA(String match)
		{
			super(new NFA(), new State());
			this.match = match;
			failed = false;
			i=0;
		}
		
		public State doTransition(Character input)
		{
			if(failed) return new State(false);
			
			i = i % match.length();
			char current = match.charAt(i);
			i++;
			
			if(current == input)
			{
				if(i == match.length()) return new State(true);
			}
			else
			{
				failed = true;
			}
			return new State(false);
		}
		
		public void reset()
		{
			i = 0;
			failed = false;
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DFA dfa, dfa2, dfa3, dfa4;
		
		//////////////////////////////////////////////////////
		//////////////////// TEST CASE 1 ////////////////////
		//////////////////////////////////////////////////////
		
//		dfa = new FakeDFA("ababbb");
//		dfa2 = new FakeDFA("abab");
//		dfa3 = new FakeDFA("baaa");
//		dfa4 = new FakeDFA("21.3");
		
		//////////////////////////////////////////////////////
		//////////////////// TEST CASE 2 ////////////////////
		//////////////////////////////////////////////////////

		NFA a = NFA.acceptCharacter('a');
		NFA b = NFA.acceptCharacter('b');

		NFA aOrb = NFA.union(a, b);
		NFA aOrbStar = NFA.kleeneStar(aOrb);

		NFA ab = NFA.concatenate(a, b);
		NFA abb = NFA.concatenate(ab, b);
		NFA nfa = NFA.concatenate(aOrbStar, abb);

		dfa = new DFA(nfa, nfa.getStartState());
		System.out.println("Type1: (a|b)*abb");
		
		NFA ba = NFA.concatenate(b, a);
		NFA baa = NFA.concatenate(ba, a);
		NFA nfa2 = NFA.concatenate(aOrbStar, baa);
		
		dfa2 = new DFA(nfa2, nfa2.getStartState());
		System.out.println("Type2: (a|b)*baa");
		
		System.out.println("----------");
		
		Map<String, DFA> types = new HashMap<String, DFA>();
		types.put("Type1", dfa);
		types.put("Type2", dfa2);
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

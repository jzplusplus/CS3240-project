package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.DFA;
import util.NFA;
import util.State;

public class NFAtoDFAtest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		State startState = new State();
		State s1 = new State();
		State s2 = new State(true);
		State s3 = new State();
		State s4 = new State(true);
		
		Set<Character> charSetWithEps = new HashSet<Character>();
		charSetWithEps.add('a');
		charSetWithEps.add('b');
		charSetWithEps.add(NFA.EPSILON);
		Set<Character> charSet = new HashSet<Character>();
		charSet.add('a');
		charSet.add('b');
		
		Set<State> fromS0eps = new HashSet<State>();
		Set<State> fromS1a = new HashSet<State>();
		Set<State> fromS2a = new HashSet<State>();
		Set<State> fromS3b = new HashSet<State>();
		Set<State> fromS4b = new HashSet<State>();
		
		fromS0eps.add(s1);
		fromS0eps.add(s3);
		
		fromS1a.add(s2);
		
		fromS2a.add(s2);
		
		fromS3b.add(s4);
		
		fromS4b.add(s4);
				
		Map<Character, Set<State>> t0 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t1 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t2 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t3 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t4 = new HashMap<Character, Set<State>>();
		
		t0.put(NFA.EPSILON, fromS0eps);
		// t0.put('a', null);
		// t0.put('b', null);
		
		// t1.put(NFA.EPSILON, null);
		t1.put('a', fromS1a);
		// t1.put('b', null);
		
		// t2.put(NFA.EPSILON, null);
		t2.put('a', fromS2a);
		// t2.put('b', null);
		
		// t3.put(NFA.EPSILON, null);
		// t3.put('a', null);
		t3.put('b', fromS3b);
		
		// t4.put(NFA.EPSILON, null);
		// t4.put('a', null);
		t4.put('b', fromS4b);
		
		Map<State,Map<Character, Set<State>>> transitMap = new HashMap<State,Map<Character, Set<State>>>();
		transitMap.put(startState, t0);
		transitMap.put(s1, t1);
		transitMap.put(s2, t2);
		transitMap.put(s3, t3);
		transitMap.put(s4, t4);
		
		NFA nfa = new NFA();
		
		nfa.withStartState(startState);
		nfa.withTransitions(transitMap);
		
		DFA dfa = new DFA(nfa, startState);
		// dfa.printTransitionTable();
		System.out.println("DFA Test Case 1");
		System.out.println("Grammar: aa* | bb*");
		
		System.out.println();
		
		System.out.println(dfa.toString());
				
		System.out.println();
		
		System.out.println("Test Strings:");
		
		System.out.println("Is 'aaa' legal? " + dfa.canAccept("aaa"));
		System.out.println("Is 'aba' legal? " + dfa.canAccept("aba"));
		
		System.out.println();
		
		
		NFA a = NFA.acceptCharacter('a');
		NFA b = NFA.acceptCharacter('b');
		
		NFA aOrb = NFA.union(a, b);
		NFA aOrbStar = NFA.kleeneStar(aOrb);
		
		// NFA ab = NFA.concatenate(a, b);
		NFA abb = NFA.concatenate(NFA.concatenate(a, b), b);
		
		NFA nfa2 = NFA.concatenate(aOrbStar, abb);
		
		System.out.println(nfa2.toString());
		
		DFA dfa2 = new DFA(aOrbStar, aOrbStar.getStartState());
		
		DFA dfa3 = new DFA(abb, abb.getStartState());
				
		System.out.println("DFA Test Case 2");
		System.out.println("Grammar: (a|b)*abb");
		
		System.out.println();
		
		System.out.println(dfa2.toString());
				
		System.out.println();
		
		System.out.println("Test Strings:");
		
		System.out.println("Is 'abb' legal? " + dfa2.canAccept("abb"));
		// System.out.println("Is 'aba' legal? " + dfa.canAccept("aba"));
		
		System.out.println("Is 'abb' legal? " + dfa3.canAccept("abb"));
		System.out.println("Is 'abb' legal? " + dfa3.canAccept("abb"));
		
		
		
	}
}

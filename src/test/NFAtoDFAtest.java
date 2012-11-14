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
				
		// NFA nfa = createNFA(startState);
		NFA nfa = new NFA();
		nfa.withStartState(startState);
		nfa.withTransitions(transitMap);
		
		Set<State> startEps = nfa.getEpsilonClosure(startState);
		Set<State> startTrans = nfa.getTransition(startState, NFA.EPSILON);
		
		for (State s : startEps) System.out.println(s.getName());
		
		
		DFA dfa = new DFA(nfa, startState);
		dfa.printTransitionTable();
		
	}
}

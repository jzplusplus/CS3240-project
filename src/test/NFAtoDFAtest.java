package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.DFA;
import util.DFA.DFABuilder;
import util.NFA;
import util.State;

public class NFAtoDFAtest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		NFA nfa = createNFA();
		
		DFABuilder dfaBuilder = new DFABuilder(nfa);
		
		DFA dfa = dfaBuilder.build();
		
		
		
		

	}
	
	public static NFA createNFA() {
		State state0 = new State();
		State state1 = new State();
		State state2 = new State();
		State state3 = new State();
		State state4 = new State();
		State state5 = new State();
		State state6 = new State();
		State state7 = new State();
		State state8 = new State();
		State state9 = new State();
		State state10 = new State(true); // accepting state
		
		Set<Character> charSet = new HashSet<Character>();
		charSet.add('a');
		charSet.add('b');
		charSet.add(NFA.EPSILON);
		
		Map<Character, Set<State>> t0 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t1 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t2 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t3 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t4 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t5 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t6 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t7 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t8 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t9 = new HashMap<Character, Set<State>>();
		Map<Character, Set<State>> t10 = new HashMap<Character, Set<State>>();
		
		Set<State> t0eps = new HashSet<State>();
		Set<State> t1eps = new HashSet<State>();
		Set<State> t3eps = new HashSet<State>();
		Set<State> t5eps = new HashSet<State>();
		Set<State> t6eps = new HashSet<State>();
		
		Set<State> t0a = new HashSet<State>(); // 3, 8
		t0a.add(state3);
		t0a.add(state8);
		
		Set<State> t0b = new HashSet<State>(); // 5
		t0b.add(state5);
		
		Set<State> t1a = new HashSet<State>(); // 3
		Set<State> t1b = new HashSet<State>(); // 5
		t1b.add(state5);
		
		Set<State> t2a = new HashSet<State>(); // 3
		t2a.add(state3);
		t2a.add(state8);
		
		Set<State> t2b = new HashSet<State>();
		
		Set<State> t3a = new HashSet<State>(); // 3, 8
		t3a.add(state3);
		t3a.add(state8);
		
		Set<State> t3b = new HashSet<State>(); // 5
		t3b.add(state5);
		
		Set<State> t4a = new HashSet<State>(); 
		
		Set<State> t4b = new HashSet<State>(); // 5
		t4b.add(state5);
		
		Set<State> t5a = new HashSet<State>(); // 3, 8
		t5a.add(state3);
		t5a.add(state8);
		
		Set<State> t5b = new HashSet<State>(); // 5
		t5b.add(state5);
		
		Set<State> t6a = new HashSet<State>(); // 3, 8 
		t6a.add(state3);
		t6a.add(state8);
		
		Set<State> t6b = new HashSet<State>(); // 5
		t5b.add(state5);
		
		Set<State> t7a = new HashSet<State>(); // 8
		t7a.add(state8);
		
		Set<State> t7b = new HashSet<State>();
		
		Set<State> t8a = new HashSet<State>();
		
		Set<State> t8b = new HashSet<State>(); // 9
		t8a.add(state9);
		
		Set<State> t9a = new HashSet<State>(); 
		
		Set<State> t9b = new HashSet<State>(); // 10
		t9a.add(state10);
		
		Set<State> t10a = new HashSet<State>(); 
		Set<State> t10b = new HashSet<State>();
		
		t0eps.add(state0);
		t0eps.add(state1);
		t0eps.add(state2);
		t0eps.add(state4);
		t0eps.add(state7);
		
		t1eps.add(state1);
		t1eps.add(state2);
		t1eps.add(state4);
		
		t3eps.add(state1);
		t3eps.add(state2);
		t3eps.add(state3);
		t3eps.add(state4);
		t3eps.add(state6);
		t3eps.add(state7);
		
		t5eps.add(state1);
		t5eps.add(state2);
		t5eps.add(state4);
		t5eps.add(state5);
		t5eps.add(state6);
		t5eps.add(state7);
				
		t6eps.add(state1);
		t6eps.add(state2);
		t6eps.add(state4);
		t6eps.add(state6);
		t6eps.add(state7);
				
		t0.put(null, t0eps);
		t1.put(null, t1eps);
		t3.put(null, t3eps);
		t5.put(null, t5eps);
		t6.put(null, t6eps);
		
		t0.put('a', t0a);
		t0.put('b', t0b);
		
		t1.put('a', t1a);
		t1.put('b', t1b);
		
		t2.put('a', t2a);
		t2.put('b', t2b);
		
		t3.put('a', t3a);
		t3.put('b', t3b);
		
		t4.put('a', t4a);
		t4.put('b', t4b);
		
		t5.put('a', t5a);
		t5.put('b', t5b);
		
		t6.put('a', t6a);
		t6.put('b', t6b);
		
		t7.put('a', t7a);
		t7.put('b', t7b);
		
		t8.put('a', t8a);
		t8.put('b', t8b);
		
		t9.put('a', t9a);
		t9.put('b', t9b);
		
		t10.put('a', t10a);
		t10.put('b', t10b);
		
		Map<State, Map<Character, Set<State>>> stateMap = new HashMap<State, Map<Character, Set<State>>>();
		stateMap.put(state0, t0);
		stateMap.put(state1, t1);
		stateMap.put(state2, t2);
		stateMap.put(state3, t3);
		stateMap.put(state4, t4);
		stateMap.put(state5, t5);
		stateMap.put(state6, t6);
		stateMap.put(state7, t7);
		stateMap.put(state8, t8);
		stateMap.put(state9, t9);
		stateMap.put(state10, t10);
		
		
		
		return new NFA(stateMap, state0);
	}

}

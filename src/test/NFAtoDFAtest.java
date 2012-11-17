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
				
		//////////////////////////////////////////////////////
		//////////////////// TEST CASE 1 ////////////////////
		//////////////////////////////////////////////////////
		State startState1 = new State();
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
		
		t1.put('a', fromS1a);
		
		t2.put('a', fromS2a);
		
		t3.put('b', fromS3b);
		
		t4.put('b', fromS4b);
		
		Map<State,Map<Character, Set<State>>> transitMap = new HashMap<State,Map<Character, Set<State>>>();
		transitMap.put(startState1, t0);
		transitMap.put(s1, t1);
		transitMap.put(s2, t2);
		transitMap.put(s3, t3);
		transitMap.put(s4, t4);
		
		NFA nfa1 = new NFA();
		
		nfa1.withStartState(startState1);
		nfa1.withTransitions(transitMap);
		
		DFA dfa1 = new DFA(nfa1, startState1);
			
		System.out.println("DFA Test Case 1");
		System.out.println("Grammar: aa* | bb*");
		
		System.out.println();
		
		System.out.println(dfa1.toString());
				
		System.out.println();
		
		System.out.println("Test Strings:");
		
		System.out.println("Is 'aaa' legal? " + dfa1.canAccept("aaa"));
		System.out.println("Is 'aba' legal? " + dfa1.canAccept("aba"));
		System.out.println("Is 'aaaaaab' legal? " + dfa1.canAccept("aaaaaab"));
		
		System.out.println(); 
		System.out.println(); 
		
		//////////////////////////////////////////////////////
		//////////////////// TEST CASE 2 ////////////////////
		//////////////////////////////////////////////////////
		
		NFA a = NFA.acceptCharacter('a');
		NFA b = NFA.acceptCharacter('b');
		
		NFA aOrb = NFA.union(a, b);
		NFA aOrbStar = NFA.kleeneStar(aOrb);
		
		NFA ab = NFA.concatenate(a, b);
		NFA abb = NFA.concatenate(ab, b);
		NFA nfa2 = NFA.concatenate(aOrbStar, abb);
		
		DFA dfa2 = new DFA(nfa2, nfa2.getStartState());
					
		
		System.out.println("DFA Test Case 2");
		System.out.println("Grammar: (a|b)*abb");
		
		System.out.println();
		
		System.out.println(dfa2.toString());
				
		System.out.println();
		
		System.out.println("Test Strings:");
		
		System.out.println("Is 'abb' legal? " + dfa2.canAccept("abb"));
		System.out.println("Is 'aab' legal? " + dfa2.canAccept("aab"));
		System.out.println("Is 'aabb' legal? " + dfa2.canAccept("aabb"));
		System.out.println("Is 'bb' legal? " + dfa2.canAccept("bb"));
		System.out.println("Is 'ababbababababb' legal? " + dfa2.canAccept("ababbababababb"));
		System.out.println("Is 'abababababab' legal? " + dfa2.canAccept("ababababab"));
		
		System.out.println();
		
		//////////////////////////////////////////////////////
		//////////////////// TEST CASE 3 ////////////////////
		//////////////////////////////////////////////////////
		
		NFA abPlus = NFA.kleenePlus(ab);
		NFA nfa3 = NFA.concatenate(aOrbStar, abPlus);
		DFA dfa3 = new DFA(nfa3, nfa3.getStartState());
		
		System.out.println("DFA Test Case 3");
		System.out.println("Grammar: (a|b)*(ab)+");
		
		System.out.println();
		
		System.out.println(dfa3.toString());
		
		System.out.println();

		System.out.println("Test Strings:");
		
		System.out.println("Is '' (empty string) legal? " + dfa3.canAccept(""));
		System.out.println("Is 'ab' legal? " + dfa3.canAccept("ab"));
		System.out.println("Is 'abab' legal? " + dfa3.canAccept("abab"));
		System.out.println("Is 'bb' legal? " + dfa3.canAccept("bb"));
		System.out.println("Is 'aabbaabababab' legal? " + dfa3.canAccept("aabbaabababab"));
		System.out.println("Is 'abababbbabbba' legal? " + dfa3.canAccept("abababbbabbba"));
	
	}
}

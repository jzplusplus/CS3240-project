package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parser.ParseTree;
import parser.RegexParser;
import base.NFABuilder;
import exception.ParseException;


public class DFA {

	private Map<State, Map<Character, State>> transitionTable;
	private NFA nfa;
	private State currentState;
	private State startState;

	public DFA(final NFA nfa, final State startState) {
		this.nfa = nfa;
		this.startState = startState;
		transitionTable = new HashMap<State, Map<Character, State>>();
		currentState = startState;
		build();
	}

	/**
	 * Changes the current state to the next state, based on the input
	 * character.
	 */
	public State doTransition(final Character input) {
		if (transitionTable.containsKey(currentState)) {
			this.currentState = this.transitionTable.get(this.currentState).get(input);
			return this.currentState; 
		} else {
			this.currentState = null;
			return null;
		}
	}

	public void reset() {
		this.currentState = this.startState;
	}

	public boolean isInAcceptState() {
		return this.currentState != null && this.currentState.isAccepting();
	}

	public boolean canAccept(String inputString) {
		this.reset();
		for (Character inputChar : inputString.toCharArray()) {
			this.doTransition(inputChar);
		}
		return currentState.isAccepting();
	}


	private void build() {

		// initializations
		// Set<Character> characterSet = nfa.getAllowableCharacters();

		Set<State> startMembers = new HashSet<State>();
		startMembers.addAll(nfa.getEpsilonClosure(startState));

		IntermediateState dfaStartState = new IntermediateState(startMembers, startState.isAccepting());
		dfaStartState.setStartState(true);
		Set<IntermediateState> dfaStates = new HashSet<IntermediateState>();
		dfaStates.add(dfaStartState);



		IntermediateState currState = dfaStartState;
		while (containsUnmarkedState(dfaStates)) {

			currState.mark(true); // indicates that the state has been visited

			// epsilon
			Set<State> eps = getEpsilonClosure(currState);
			currState.addMembers(eps);
			
			
			Map<Character,Set<State>> nextSets = constructNextState(currState);
			for (Character c : nextSets.keySet()) {
				IntermediateState is = new IntermediateState(nextSets.get(c),isAccepting(nextSets.get(c)));
					
					boolean found = false;
					for (IntermediateState is2 : dfaStates) {
						if (is2.getMemberStates().equals(is.getMemberStates()) || is2.getMemberStates().containsAll(is.getMemberStates())) {
							found = true;
							currState.addTransition(c, is2);
						}
					}
					if (!found) {
						currState.addTransition(c, is);
						dfaStates.add(is);
					}
			}
			
			currState = getUnmarkedState(dfaStates);
			
			/*
			Map<Set<State>,Set<Character>> transitionMap = new HashMap<Set<State>,Set<Character>>();
			// transition on each char
			for (State s : currState.getMemberStates()) {
				for (Character c : nfa.getAllowableCharacters()) {
					if (c!=null) {

						Set<State> to = nfa.getTransition(s, c);
						if (to!=null && !to.isEmpty()) {
							if (transitionMap.get(to)!=null) transitionMap.get(to).add(c);
							else {
								transitionMap.put(to, new HashSet<Character>());
								transitionMap.get(to).add(c);
							}
							
						}
						
					}
				}
				for (Set<State> ss : transitionMap.keySet()) {
					IntermediateState toIS = new IntermediateState(ss, isAccepting(ss));
					for (Character c : transitionMap.get(ss)) {
						currState.addTransition(c, toIS);
						dfaStates.add(toIS);
					}
				}
				
				
			}

			currState = getUnmarkedState(dfaStates);
			*/

		}

		constructTransitionTable(dfaStates);

	} // end build
	
	private Map<Character,Set<State>> constructNextState(IntermediateState is) {
		Map<Character,Set<State>> map = new HashMap<Character,Set<State>>();
		for (Character c : nfa.getAllowableCharacters()) {
			if (c!=null) {
				map.put(c, new HashSet<State>());
				for (State ms : is.getMemberStates()) {
					map.get(c).addAll(nfa.getTransition(ms, c));
				}
			}
		}
		return map;
	}

	private void constructTransitionTable(Set<IntermediateState> dfaIS) {

		IntermediateState errorState = createErrorState(nfa.getAllowableCharacters());
		boolean errorStateAdded = false;

		for (IntermediateState is : dfaIS) {
			for (Character c : nfa.getAllowableCharacters()) {
				if (c!=null) {
					IntermediateState to = is.getTransition(c);
					if (to==null) {
						is.addTransition(c, errorState);
						errorStateAdded = true;
					}
				}
			}
		}

		if (errorStateAdded) dfaIS.add(errorState);


		ArrayList<IntermediateState> isList = new ArrayList<IntermediateState>();
		ArrayList<State> sList = new ArrayList<State>();

		HashMap<IntermediateState,State> conversionMap = new HashMap<IntermediateState,State>();

		for (IntermediateState is : dfaIS) {
			if (is.isStartState()) {
				conversionMap.put(is, startState);
				transitionTable.put(startState, new HashMap<Character,State>());
			} else {
				State newState = new State(is.isAccepting());
				conversionMap.put(is, newState);
				transitionTable.put(newState, new HashMap<Character,State>());
			}
		}

		for (IntermediateState is : dfaIS) {
			for (Character c : nfa.getAllowableCharacters()) {
				if (c!=null) {
					IntermediateState toIS = is.getTransition(c);
					State fromS = conversionMap.get(is);
					State toS = conversionMap.get(toIS);
					transitionTable.get(fromS).put(c, toS);
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		for (State s : transitionTable.keySet()) {
			sb.append(s.getName() + " { ");
			for (Character c : transitionTable.get(s).keySet()) {
				sb.append(c + ": [ " + transitionTable.get(s).get(c).getName() + " ]\n");
			}
			sb.append(" }\n");
		}
		System.out.println(sb.toString());
	}


	private Set<Character> findMultipleTransitions(IntermediateState is) {
		Set<Character> multiples = new HashSet<Character>();
		for (Character c : nfa.getAllowableCharacters()) {
			if (c!=null) {
				for (State ms : is.getMemberStates()) {
					if (nfa.getTransition(ms, c)!=null && !nfa.getTransition(ms, c).isEmpty()) {
						for (State ms2 : is.getMemberStates()) {
							if (!ms2.equals(ms)) {
								if (nfa.getTransition(ms2, c)!=null && !nfa.getTransition(ms2, c).isEmpty()) {
									multiples.add(c);
								}
							}
						}


					}
				}
			}
		}

		return multiples;
	}

	private Set<State> findOutgoingEdges(IntermediateState is, Character c) {
		Set<State> out = new HashSet<State>();
		for (State s : is.getMemberStates()) {
			Set<State> temp = nfa.getTransition(s, c);
			if (temp!=null && !temp.isEmpty()) {
				for (State to : temp) {
					if (!is.getMemberStates().contains(to)) out.add(to);
				}
			}
		}
		return out;
	}


	private IntermediateState handleLoop(IntermediateState is) {
		for (Character c : nfa.getAllowableCharacters()) {
			if (c!=null) {
				if (is.getTransition(c)!=null) {
					IntermediateState to = is.getTransition(c);
					if (is.equals(to)) is.addTransition(c, to);
				}
			}
		}
		return is;
	}

	private boolean isAccepting(Set<State> states) {
		for (State s : states) {
			if (s.isAccepting()) return true;
		}
		return false;
	}

	private Set<Character> findOutgoingTransitions(IntermediateState is) {
		Set<Character> out = new HashSet<Character>();
		Set<Character> exclude = findMultipleTransitions(is);

		for (Character c : nfa.getAllowableCharacters()) {
			if (is.getTransition(c)!=null) {
				IntermediateState to = is.getTransition(c);
				if (!to.equals(c)) out.add(c);
			}
		}

		out.removeAll(exclude);		
		return out;
	}

	private IntermediateState createIntermediateState(IntermediateState from, Character c) {
		Set<State> memberStates = new HashSet<State>();
		for (State s : from.getMemberStates()) {
			Set<State> temp = nfa.getTransition(s, c);
			if (temp!=null && !temp.isEmpty()) memberStates.addAll(temp);
			memberStates.addAll(getEpsilonClosure(memberStates));
		}
		if (!memberStates.isEmpty()) {
			return new IntermediateState(memberStates, isAccepting(memberStates));
		}
		return null;
	}

	/**
	 * Create intermediate state by combining NFA states
	 * @param nfaStates
	 * @return
	 */
	private IntermediateState convertToIntermediateState(Set<State> nfaStates) {
		boolean accepting = false;
		if (nfaStates!=null) {
			for (State s : nfaStates) {
				if (s.isAccepting()) accepting = true;
			}
			return new IntermediateState(nfaStates,accepting);
		}
		return null;
	}

	private IntermediateState findTheState(Set<IntermediateState> iStates, Set<State> states) {
		if (states!=null) {
			for (IntermediateState iState : iStates) {

				if (iState.isMember(states)) return iState;
			}
		}
		return null;
	}

	/**
	 * Finds all possible epsilon transitions from a given set of states
	 * and returns the set of all epsilon transitions
	 * 
	 * @param states a set of states
	 * @return a set of states that are reachable from the given set of states (epsilon transitions)
	 */
	private Set<State> getEpsilonClosure(Set<State> states) {
		if (states!=null) {
			Set<State> newStates = new HashSet<State>();
			boolean legitimate = false;
			for (State s : states) {
				legitimate = false;
				Set<State> epsTransitions = nfa.getEpsilonClosure(s);
				if (epsTransitions!=null) {
					for (State member : epsTransitions) { // if epsilon of epsilon does not have the input state,
						if (nfa.getEpsilonClosure(member).contains(s)) {
							if(!nfa.getTransition(member, nfa.getEpsilon()).contains(s)) {
								legitimate = true;
							}
						}

					}
				}
				if (!legitimate) epsTransitions.remove(s);
				newStates.addAll(epsTransitions);
			}
			return (newStates.isEmpty() ? null : newStates);
		}
		return null;
	}

	/**
	 * Finds all possible epsilon transitions from a given set of states
	 * and returns the set of all epsilon transitions
	 * 
	 * @param state a state
	 * @return a set of states that are reachable from the given set of states (epsilon transitions)
	 */
	private Set<State> getEpsilonClosure(IntermediateState state) {
		if (state!=null) {
			Set<State> newStates = new HashSet<State>();
			for (State ms : state.getMemberStates()) {
				newStates.addAll(nfa.getEpsilonClosure(ms));
			}
			return newStates;
		}
		return null;
	}

	private boolean containsUnmarkedState(Set<IntermediateState> iStates) {
		for (IntermediateState is : iStates) {
			if (!is.isMarked()) return true;
		}
		return false;
	}

	private IntermediateState getUnmarkedState(Set<IntermediateState> iStates) {
		for (IntermediateState is : iStates) {
			if (!is.isMarked()) return is;
		}
		return null;
	}

	/**
	 * Returns true if the set of IntermediateStates contains the given set of States
	 * 
	 * @param iStates a Set of IntermediateStates
	 * @param states a Set of States
	 * @return true if a member; otherwise, false
	 */
	private boolean containsAsMember(Set<IntermediateState> iStates, Set<State> states) {
		for (IntermediateState is : iStates) {
			if (states.equals(is.getMemberStates())) return true;
		}
		return false;
	}

	private IntermediateState createErrorState(Set<Character> characterSet) {
		IntermediateState errorState = new IntermediateState(null, false);
		for (Character ch : characterSet) {
			if (ch!=null) errorState.addTransition(ch, errorState);
		}
		errorState.setMarker(false);
		errorState.setErrorState(true);
		return errorState;
	} 

	public String toString() {
		String table2str = "DFA of the following NFA:\n" + nfa.toString() + "\n\n";

		Set<State> states = transitionTable.keySet();

		if (currentState!=startState) reset();

		for (State s : states) {
			table2str += s.getName();
			if (s.equals(startState)) table2str += "(S)";
			else if (s.isAccepting()) table2str += "(*)";
			table2str +=  "\n\t{ ";
			for (Character c : transitionTable.get(s).keySet()) {
				currentState = s;
				table2str += c.charValue() + ": ";  
				if (doTransition(c).isAccepting()) table2str += doTransition(c).getName() + "(*); ";
				else table2str += doTransition(c).getName() + ";    ";
			}
			table2str += " }\n";
		}
		return table2str;
	}

	

	public static void main(String[] args) throws ParseException, IOException {

		ParseTree t = RegexParser.parse("([a-zA-Z])*ment([a-zA-Z])*", new HashMap());
		t.print();
		NFABuilder nfaB = new NFABuilder(new HashMap(), true);
		NFA n = nfaB.build(t);
		System.out.println(n);

		DFA dfa4 = new DFA(n, n.getStartState());
		System.out.println(dfa4);

	}

} // end class DFA
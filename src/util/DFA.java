package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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
		this.currentState = this.transitionTable.get(this.currentState).get(input);
		return this.currentState; 
	}

	public void reset() {
		this.currentState = this.startState;
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
		Set<Character> characterSet = nfa.getAllowableCharacters();

		Set<State> nfaStates = nfa.getAllStates();
		if (!nfaStates.contains(startState)) nfaStates.add(startState); // might be unnecessary

		Set<State> startMembers = new HashSet<State>();
		startMembers.add(startState);

		IntermediateState dfaStartState = new IntermediateState(startMembers, startState.isAccepting());
		Set<IntermediateState> dfaStates = new HashSet<IntermediateState>();
		dfaStates.add(dfaStartState);

		IntermediateState errorState = new IntermediateState(null, false);
		for (Character ch : characterSet) {
			errorState.addTransition(ch, errorState);
		}
		errorState.setMarker(false);
		
		
		while (containsUnmarkedState(dfaStates)) {

			IntermediateState is = getUnmarkedState(dfaStates);
			is.setMarker(false); // indicates that has been added to the table and explored

			for (Character input : characterSet) {
				if (input!=null) {
					Set<State> epsTransitions = new HashSet<State>();
					Set<State> temp = null;
					for (State s : is.getMemberStates()) {

						temp = getEpsilonClosure(s);	
						if (temp!=null) epsTransitions.addAll(temp);

						Set<State> onInput = nfa.getTransition(s, input); 
						if (onInput!=null && !onInput.isEmpty()) {
							epsTransitions.addAll(getEpsilonClosure(onInput));
						}

					} // end for State
					
					if (epsTransitions!=null && !epsTransitions.isEmpty()) {
						Set<State> stInput = new HashSet<State>();
						for (State st : epsTransitions) {
							stInput.addAll(nfa.getTransition(st, input));
						}

						if (stInput!=null && !stInput.isEmpty()) {
							epsTransitions.addAll(stInput);
						}

						IntermediateState newIstate = convertToIntermediateState(epsTransitions);
						
						if (containsAsMember(dfaStates,epsTransitions)) {
							is.addTransition(input, findTheState(dfaStates, epsTransitions));
						
						} else {
							dfaStates.add(newIstate);
							is.addTransition(input, newIstate);
						}


					} else { // if null, go to error state
						is.addTransition(input, errorState);
					}

				} // end input null checking
			} // end for Character

		} // end while
		
		dfaStates.add(errorState);
		
		ArrayList<IntermediateState> isArrList = new ArrayList<IntermediateState>();
		ArrayList<State> dfaArrList = new ArrayList<State>();
		for (IntermediateState iState : dfaStates) {
			isArrList.add(iState);
			dfaArrList.add(isArrList.indexOf(iState), new State(iState.isAccepting()));
		}
		
		for (IntermediateState iState : isArrList) {
			Map<Character, State> transition = new HashMap<Character, State>();
			for (Character input : characterSet) {
				if (input!=null) {
					transition.put(input, dfaArrList.get(isArrList.indexOf(iState.getTransition(input))));
				}
			}
			transitionTable.put(dfaArrList.get(isArrList.indexOf(iState)), transition);

			if (iState.equals(dfaStartState)) startState = dfaArrList.get(isArrList.indexOf(iState));
		}
	} // end build


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
		for (IntermediateState iState : iStates) {
			if (iState.isMember(states)) return iState;
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

	/**
	 * Finds all possible epsilon transitions from a given set of states
	 * and returns the set of all epsilon transitions
	 * 
	 * @param state a state
	 * @return a set of states that are reachable from the given set of states (epsilon transitions)
	 */
	private Set<State> getEpsilonClosure(State state) {
		Set<State> newStates = new HashSet<State>();
		Set<State> epsTransitions = nfa.getEpsilonClosure(state);
		if (epsTransitions!=null) {
			if (epsTransitions.contains(state)) {
				if(!nfa.getTransition(state, nfa.getEpsilon()).contains(state)) {
					epsTransitions.remove(state);
				}
			}
		}
		newStates.addAll(epsTransitions);
		return (newStates.isEmpty() ? null : newStates);
	}

	private boolean containsUnmarkedState(Set<IntermediateState> iStates) {
		for (IntermediateState is : iStates) {
			if (is.getMarker()) return true;
		}
		return false;
	}

	private IntermediateState getUnmarkedState(Set<IntermediateState> iStates) {
		for (IntermediateState is : iStates) {
			if (is.getMarker()) return is;
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

	public void printTransitionTable() {
		System.out.println("The start state is " + startState.getName());
		Set<State> states = transitionTable.keySet();
		System.out.println("The states of the DFA are:" );
		for (State s : states) System.out.println("Name: " + s.getName() + "\nAccepting State? " + s.isAccepting());
		System.out.println();
		if (currentState!=startState) reset();
		for (State s : states) {
			System.out.println("From " + s.getName());
			currentState = s;
			for (Character c : transitionTable.get(s).keySet()) {
				System.out.println("To " + doTransition(c).getName() + " on " + c.charValue());
			}
		}
	}
	
	public String toString() {
		String table2str = "";
		
		table2str += "The start state is " + startState.getName() + "\n\n";
		Set<State> states = transitionTable.keySet();
		table2str += "The number of states is " + states.size()+ "\n\n";
		table2str += "The states are:\n\n";
		for (State s : states) table2str += "Name: " + s.getName() + "\nAccepting State? " + s.isAccepting() + "\n\n";
		
		table2str += "The followings are the transitions:\n\n";
		
		if (currentState!=startState) reset();
		for (State s : states) {
			table2str += "From: " + s.getName() + "\n";
			currentState = s;
			for (Character c : transitionTable.get(s).keySet()) {
				table2str += "Transition on " + c.charValue() + " is " + doTransition(c).getName() + "\n";
			}
			table2str += "\n";
		}
		return table2str;
	}

	/**
	 * IntermediateState is a state for combining NFA states when converting to 
	 * DFA state 
	 * 
	 *
	 */
	private class IntermediateState extends State {

		Set<State> memberStates;
		Map<Character, IntermediateState> transitionMap;
		boolean marker;

		public IntermediateState(Set<State> memberStates, boolean isAcceptingState) {
			super(isAcceptingState);
			this.memberStates = memberStates;
			transitionMap = new HashMap<Character, IntermediateState>();
			marker = true;
		}

		public Set<State> getMemberStates() {
			return memberStates;
		}

		public boolean isMember(Set<State> memberStates) {
			return this.memberStates.equals(memberStates);
		}

		public void addTransition(Character input, IntermediateState to) {
			transitionMap.put(input, to);
		}

		public IntermediateState getTransition(Character input) {
			return transitionMap.get(input);
		}

		public boolean getMarker() {
			return marker;
		}

		public void setMarker(boolean marker) {
			this.marker = marker;
		}
	} // end private class IntermediateState

} // end class DFA
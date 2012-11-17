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

		Set<State> startMembers = new HashSet<State>();
		startMembers.addAll(nfa.getEpsilonClosure(startState));

		IntermediateState dfaStartState = new IntermediateState(startMembers, startState.isAccepting());
		Set<IntermediateState> dfaStates = new HashSet<IntermediateState>();
		dfaStates.add(dfaStartState);

		IntermediateState errorState = createErrorState(characterSet);

		while (containsUnmarkedState(dfaStates)) {

			IntermediateState is = getUnmarkedState(dfaStates);
			is.setMarker(false); // indicates that has been added to the table and explored

			for (Character input : characterSet) {
				if (input!=null) {
					Set<State> epsTransitions = new HashSet<State>();
					for (State s : is.getMemberStates()) {

						// reading before finding eps
						Set<State> onInput = nfa.getTransition(s, input);
						if (onInput!=null && !onInput.isEmpty()){
							epsTransitions.addAll(onInput);
							
							if (getEpsilonClosure(onInput)!=null) {
								Set<State> epsStates = getEpsilonClosure(onInput);
								if (epsStates!=null && !epsStates.isEmpty()) epsTransitions.addAll(epsStates);
							}
						}
						
						
						

					} // end for State

					if (epsTransitions!=null && !epsTransitions.isEmpty()) {

						IntermediateState newIstate = convertToIntermediateState(epsTransitions);

						if (containsAsMember(dfaStates,epsTransitions)) {
							is.addTransition(input, findTheState(dfaStates, epsTransitions));

						} else {
							dfaStates.add(newIstate);
							is.addTransition(input, newIstate);
						}


					} else { // if null, go to error state
						is.addTransition(input, errorState);
						if (!dfaStates.contains(errorState)) dfaStates.add(errorState);
					}

				} // end input null checking
			} // end for Character

		} // end while




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
	@SuppressWarnings("unused")
	private Set<State> getEpsilonClosure(State state) {
		if (state!=null) {
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
		return null;
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
		boolean errorState;

		public IntermediateState(Set<State> memberStates, boolean isAcceptingState) {
			super(isAcceptingState);
			this.memberStates = memberStates;
			transitionMap = new HashMap<Character, IntermediateState>();
			marker = true;
			errorState = false;
		}

		public Set<State> getMemberStates() {
			return memberStates;
		}

		/**
		 * If memberStates of this state is null, then the state is the error state
		 * where all questionable transitions wind up.
		 * 
		 * @param memberStates
		 * @return true if the input set of states is same as the members of this intermediate state
		 */
		public boolean isMember(Set<State> memberStates) {
			return this.getErrorState() ?  false : this.memberStates.equals(memberStates);
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

		public boolean getErrorState() {
			return errorState;
		}

		public void setErrorState(boolean isErrorState) {
			this.errorState = isErrorState;
		}

	} // end private class IntermediateState

} // end class DFA
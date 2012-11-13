package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFA {

	private Map<State, Map<Character, State>> transitionTable;
	private State currentState;
	private State startState;

	public DFA(final Map<State, Map<Character, State>> transitionTable, final State startState) {
		this.transitionTable = transitionTable;
		this.currentState = startState;
		this.startState = startState;
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

	public static class DFABuilder {


		private NFA nfa;
		
		private State startState;
		private Map<State, Map<Character, State>> transitionTable;
		// private Set<State> epsilonTransitionStates;

		public DFABuilder(NFA nfa) {
			this.nfa = nfa;
			startState = nfa.getStartState();
			transitionTable = new HashMap<State, Map<Character, State>>();
		}

		/**
	     *      
		 * @return
		 */
		public DFA build() {
			
			// initializations
			Set<Character> characterSet = nfa.getAllowableCharacters();
			Set<State> initialEpsilonStates = nfa.getEpsilonClosure(startState);
			Set<State> nfaStates = nfa.getAllStates();
			if (!nfaStates.contains(startState)) nfaStates.add(startState);
			
			boolean accepting = false;
						
			// remove epsilon closure
			Set<IntermediateState> intermediateStates = new HashSet<IntermediateState>();
			for (State from : nfaStates) {
				accepting = false;
				Set<State> epsClosure = nfa.getEpsilonClosure(from);
				for (State to : epsClosure ) {
					if (to.isAccepting()) accepting = true; 
				}
				intermediateStates.add(new IntermediateState(epsClosure, accepting));
			}
						
			// take care of epsilon closure
			Set<IntermediateState> dfaStates = new HashSet<IntermediateState>();
			IntermediateState dfaStartState = new IntermediateState(initialEpsilonStates, containsAcceptingState(initialEpsilonStates));
			// dfaStartState.setMarker(true);
			dfaStates.add(dfaStartState);
			
			while (containsUnmarkedState(dfaStates)) {
				IntermediateState is = getUnmarkedState(dfaStates);
				is.setMarker(true);
				for (Character input : characterSet) {
					for (State s : is.getMemberStates()) {
						Set<State> epsTransitions = getEpsilonClosure(nfa.getTransition(s, input));
						IntermediateState newIstate = convertToIntermediateState(epsTransitions);
						is.addTransition(input, newIstate);
						if (!containsAsMember(dfaStates,epsTransitions)) {
							dfaStates.add(newIstate);
						}
					}
				}
			}
			
			
			return new DFA(this.transitionTable, startState);
		} // end build
		
		private IntermediateState convertToIntermediateState(Set<State> nfaStates) {
			boolean accepting = false;
			for (State s : nfaStates) {
				if (s.isAccepting()) accepting = true;
			}
			return new IntermediateState(nfaStates,accepting);
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
			// sets do not allow duplicates, so should not be a problem...
			for (State s : states) {
				newStates.addAll(nfa.getEpsilonClosure(s));
			}
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
			for (IntermediateState iState : iStates) {
				if (iState.isMember(states)) return true;
			}
			return false;
		}
		
		
		private boolean containsAcceptingState(Set<State> states) {
			for (State s : states) {
				if (s.isAccepting()) return true;
			}
			return false;
		}
		
		/**
		 * IntermediateState
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
				marker = false;
			}
			
			public Set<State> getMemberStates() {
				return memberStates;
			}
			
			public boolean isMember(Set<State> memberStates) {
				return memberStates.equals(this.memberStates);
			}
			
			public void addTransition(Character input, IntermediateState to) {
				transitionMap.put(input, to);
			}
			
			public boolean getMarker() {
				return marker;
			}
			
			public void setMarker(boolean marker) {
				this.marker = marker;
			}
		} // end private class IntermediateState


	}

}

package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class NFA {
    // Use this for epsilon transitions-- hashmaps allow a null key.
    public static final Character EPSILON = null;

    // we need two keys: a State and an input Character.
    private Map<State, Map<Character, Set<State>>> transitionTable;
    private State startState;


    public NFA() { 
        this.transitionTable = new HashMap<State, Map<Character, Set<State>>>();
    }
    public NFA(final Map<State, Map<Character, Set<State>>> transitionTable, final State startState) {
        this.transitionTable = transitionTable;
        this.startState = startState;
    }

    // Unlike DFA.doTransition, this doesn't have any side-effects:
    // It doesn't alter the current state or anything.
    public Set<State> getTransition(final State initialState, final Character input) {
        if (!this.transitionTable.containsKey(initialState) || 
            !this.transitionTable.get(initialState).containsKey(input)) {
            return new HashSet<State>();
        }
        return this.transitionTable.get(initialState).get(input);
    }

    public State getStartState() {
        return startState;
    }

    // null checking necessary
    public Set<State> getEpsilonClosure(final State state) {
        Queue<State> statesToVisit = new LinkedList<State>();
        statesToVisit.add(state);
        HashSet<State> epsilonClosure = new HashSet<State>();
        while (!statesToVisit.isEmpty()) {
            State currentState = statesToVisit.poll();
            epsilonClosure.add(currentState);
            for (State nextState : getTransition(currentState, EPSILON)) {
                if (!statesToVisit.contains(nextState) && !epsilonClosure.contains(nextState)) {
                    statesToVisit.add(nextState);
                }
            }
        } 
        return epsilonClosure;

    }
    

    
    
    public Set<Character> getAllowableCharacters() {
    	
    	Set<Character> charSet = new HashSet<Character>();
    	for (Map<Character,Set<State>> map : transitionTable.values()) {
    		charSet.addAll(map.keySet());
    	}
    	return charSet;
    }
    
    public Set<State> getAllStates() {
    	return transitionTable.keySet();
    }
    
    public Character getEpsilon() {
    	return EPSILON;
    }
    public NFA withStartState(State startState) {
        this.startState = startState;
        return this;
    }

    public NFA withTransition(State original, Character input, State transition) {
        if (!transitionTable.containsKey(original)) {
            transitionTable.put(original, new HashMap<Character, Set<State>>());
        }
        if (!transitionTable.get(original).containsKey(input)) {
            transitionTable.get(original).put(input, new HashSet<State>());
        }

        transitionTable.get(original).get(input).add(transition);
        
        if (!transitionTable.containsKey(transition)) {
            transitionTable.put(transition, new HashMap<Character, Set<State>>());
        }
            
        return this;
    }

        
    public static NFA acceptCharacter(char c) {
        State start = new State();
        State end = new State(true);

        NFA nfa = new NFA();
        nfa.startState = start;
        nfa.withTransition(start, c, end);
        return nfa;
    }

    public void replace(State oldState, State newState) {
        // first do a deep clone:
        Map<State, Map<Character, Set<State>>> clone = new HashMap<State, Map<Character, Set<State>>>();
        for (State inputState : transitionTable.keySet()) {
            clone.put(inputState, new HashMap<Character, Set<State>>());
            for (Character transition : transitionTable.get(inputState).keySet()) {
                clone.get(inputState).put(transition, new HashSet<State>());
                for (State outputState : transitionTable.get(inputState).get(transition)) {
                    clone.get(inputState).get(transition).add(outputState);
                }
            }
        }
        // now iterate over the deep clone to avoid concurrent modification errors
        for (State inState : clone.keySet()) {
            for (Entry<Character, Set<State>> trans : clone.get(inState).entrySet()) {
                for (State outState : trans.getValue()) {
                    if (outState.equals(oldState)) {
                        transitionTable.get(inState).get(trans.getKey()).remove(oldState);
                        transitionTable.get(inState).get(trans.getKey()).add(newState);
                    }
                }
            }

            if (inState.equals(oldState)) {
                transitionTable.put(newState, transitionTable.get(inState));
                transitionTable.remove(inState);
            }
        }
        if (this.startState.equals(oldState)) {
            this.startState = newState;
        }

    }
    public Set<State> getAcceptStates() {
        Set<State> acceptStates = new HashSet<State>();
        for (State state : transitionTable.keySet()) {
            if (state.isAccepting()) {
                acceptStates.add(state);
            }
        }
        return acceptStates;
    }       
    public NFA withTransitions(Map<State, Map<Character, Set<State>>> transitions) {
        for (State orig : transitions.keySet()) {
            for (Character input : transitions.get(orig).keySet()) {
                for (State fin : transitions.get(orig).get(input)) {
                    this.withTransition(new State(orig), input, new State(fin));
                }
            }
        }
        return this;
    }

    public static NFA union(NFA one, NFA two) {
        if (two == null) {
            return one;
        }
        if (one == null) {
            return two;
        }
        NFA oneClone = one.clone();
        NFA twoClone = two.clone();
        NFA union = new NFA().withTransitions(oneClone.transitionTable)
                             .withTransitions(twoClone.transitionTable);
        union.startState = new State();
        union.withTransition(union.startState, EPSILON, oneClone.startState);
        union.withTransition(union.startState, EPSILON, twoClone.startState);
        return union;
    }

    public static NFA concatenate(NFA one, NFA two) {
        if (two == null) {
            return one;
        }
        if (one == null) {
            return two;
        }
        NFA oneClone = one.clone();
        NFA twoClone = two.clone();
        NFA concat = new NFA().withTransitions(oneClone.transitionTable)
                              .withTransitions(twoClone.transitionTable);
        concat.startState = oneClone.startState;
        for (State oldAccept : oneClone.getAcceptStates()) {
            State newState = new State(oldAccept);
            newState.setAccepting(false);
            concat.replace(oldAccept, newState);
            concat.withTransition(newState, EPSILON, twoClone.startState);
        }
        return concat;
    }

    public static NFA kleeneStar(NFA one) {
        if (one == null) {
            return null;
        }
        NFA oneClone = one.clone();
        NFA star = new NFA().withTransitions(oneClone.transitionTable);
        star.startState = new State(true);
        star.withTransition(star.startState, EPSILON, oneClone.startState);
        for (State accept : oneClone.getAcceptStates()) {
            star.withTransition(accept, EPSILON, oneClone.startState);
        }
        return star;
    }

    public static NFA kleenePlus(NFA one) {
        return concatenate(one, kleeneStar(one));
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<NFA\n");

        List<State> inputStates = new ArrayList<State>();
        inputStates.addAll(transitionTable.keySet());
        Collections.sort(inputStates);
        for (State input : inputStates) {
            sb.append(input.getName());
            if (input.isAccepting()) {
                sb.append("*");
            }
            if (input.equals(startState)) {
                sb.append("(S)");
            }
            sb.append(" {");
            // TODO sort by input
            List<Character> sortedTransitionChars = new ArrayList<Character>(transitionTable.get(input).keySet());
            Collections.sort(sortedTransitionChars);
            for (Character transitionChar : sortedTransitionChars) {
            	Set<State> transitionStates = transitionTable.get(input).get(transitionChar);
                if (transitionChar == EPSILON) {
                    sb.append("epsilon");
                } else {
                    sb.append(transitionChar);
                }
                sb.append(": {");
                for (State s : transitionStates) {
                    sb.append(s.getName());
                    if (s.isAccepting()) {
                        sb.append("*");
                    }
                    if (s.equals(startState)) {
                        sb.append("(S)");
                    }
                    sb.append(", ");
                }
                sb.append("}");
            }
            sb.append("}\n");
        }
        sb.append(">");
        return sb.toString();
    }
    
    
    public static NFA acceptAll() {
    	// yes, we could just do a billion unions, but that's stupid!
    	Set<Character> allChars = new HashSet<Character>();
    	for (int i = 32; i <= 126; i++) {
    		allChars.add((char)i);
    	}
    	return acceptSet(allChars);
    }
    
    public static NFA acceptSet(Collection<Character> chars) {
    	// yes, we could just do a billion unions, but that's stupid!
    	State start = new State();
    	State end = new State(true);
    	NFA nfa = new NFA();
    	for (Character c : chars) {
    		nfa.withTransition(start, c, end);
    	}
    	return nfa.withStartState(start);
    }
    
    public NFA clone() {
        NFA clone = new NFA().withTransitions(transitionTable).withStartState(this.startState);
        List<State> sortedKeys = new ArrayList<State>(transitionTable.keySet());
        Collections.sort(sortedKeys);
        for (State orig : sortedKeys) {
            State clonedState = new State(orig.isAccepting());
            clone.replace(orig, clonedState);
        }
        return clone;
 
    }

}

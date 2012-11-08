package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class NFA {
    // Use this for epsilon transitions-- hashmaps allow a null key.
    public static final Character EPSILON = null;

    // we need two keys: a State and an input Character.
    private Map<State, Map<Character, Set<State>>> transitionTable = new HashMap<State, Map<Character, Set<State>>>();
    private State startState;
    public NFA() { }
    public NFA(final Map<State, Map<Character, Set<State>>> transitionTable, final State startState) {
        this.transitionTable = transitionTable;
        this.startState = startState;
    }

    // Unlike DFA.doTransition, this doesn't have any side-effects:
    // It doesn't alter the current state or anything.
    public Set<State> getTransition(final State initialState, final Character input) {
        return this.transitionTable.get(initialState).get(input);
    }

    public State getStartState() {
        return startState;
    }

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

    public Set<State> getAcceptStates() {
        Set<State> acceptStates = new HashSet<State>();
        for (State state : transitionTable.keySet()) {
            if (state.isAccepting()) {
                acceptStates.add(state);
            }
        }
        return acceptStates;
    }



    public NFA withTransition(State original, Character input, State transition) {
        if (!transitionTable.containsKey(original)) {
            transitionTable.put(original, new HashMap<Character, Set<State>>());
        }
        if (!transitionTable.get(original).containsKey(input)) {
            transitionTable.get(original).put(input, new HashSet<State>());
        }

        transitionTable.get(original).get(input).add(transition);
        return this;
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

    @SuppressWarnings("unchecked")
    public void replace(State oldState, State newState) {
        Map<State, Map<Character, Set<State>>> clone = 
            (Map<State, Map<Character, Set<State>>>) ((HashMap<State, Map<Character, Set<State>>>) transitionTable).clone();
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
    }
        


    public static NFA union(NFA one, NFA two) {
        if (two == null) {
            return one;
        }
        if (one == null) {
            return two;
        }
        NFA union = new NFA().withTransitions(one.transitionTable)
                             .withTransitions(two.transitionTable);
        union.startState = new State();
        union.withTransition(union.startState, EPSILON, one.startState);
        union.withTransition(union.startState, EPSILON, two.startState);
        return union;
    }

    public static NFA concatenate(NFA one, NFA two) {
        if (two == null) {
            return one;
        }
        if (one == null) {
            return two;
        }
        NFA concat = new NFA().withTransitions(one.transitionTable)
                              .withTransitions(two.transitionTable);
        concat.startState = one.startState;
        for (State oldAccept : one.getAcceptStates()) {
            State newState = new State(oldAccept);
            newState.setAccepting(false);
            concat.replace(oldAccept, newState);
            concat.withTransition(newState, EPSILON, two.startState);
        }
        return concat;
    }

    public static NFA kleeneStar(NFA one) {
        if (one == null) {
            return null;
        }
        NFA star = new NFA().withTransitions(one.transitionTable);
        star.startState = new State(true);
        star.withTransition(star.startState, EPSILON, one.startState);
        for (State accept : one.getAcceptStates()) {
            star.withTransition(accept, EPSILON, one.startState);
        }
        return star;
    }

    public static NFA kleenePlus(NFA one) {
        return concatenate(one, kleeneStar(one));
    }
}

package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
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
        transitionTable.putAll(transitions);
        return this;
    }

    public static NFA union(NFA one, NFA two) {
        NFA union = new NFA().withTransitions(one.transitionTable)
                             .withTransitions(two.transitionTable);
        union.startState = new State();
        union.withTransition(union.startState, EPSILON, one.startState);
        union.withTransition(union.startState, EPSILON, two.startState);
        return union;
    }

    public static NFA concatenate(NFA one, NFA two) {
        NFA concat = new NFA().withTransitions(one.transitionTable)
                              .withTransitions(two.transitionTable);
        concat.startState = one.startState;
        for (State accept : one.getAcceptStates()) {
            accept.setAccepting(false);
            concat.withTransition(accept, EPSILON, two.startState);
        }
        return concat;
    }

    public static NFA kleeneStar(NFA one) {
        NFA star = new NFA().withTransitions(one.transitionTable);
        star.startState = new State(true);
        star.withTransition(star.startState, EPSILON, one.startState);
        for (State accept : one.getAcceptStates()) {
            star.withTransition(accept, EPSILON, one.startState);
        }
        return star;
    }
}

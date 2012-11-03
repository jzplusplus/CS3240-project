package util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class NFA {
    // Use this for epsilon transitions-- hashmaps allow a null key.
    public static final Character EPSILON = null;

    // we need two keys: a State and an input Character.
    private Map<State, Map<Character, Set<State>>> transitionTable;
    private State startState;

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
}

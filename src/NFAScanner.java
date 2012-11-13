import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import parser.Token;

import util.NFA;
import util.State;

public class NFAScanner {

    private int transitionCounter = 0;
    private Map<String, NFA> nfas;
    public NFAScanner(Map<String, NFA> nfas) {
        // token name -> recognizing nfa
        this.nfas = nfas;
    }

    public List<Token> tokenize(File file) throws IOException, BadTokenException {
        transitionCounter = 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<Token> tokens = new ArrayList<Token>();
        while ((line = reader.readLine()) != null) {
            tokens.addAll(tokenize(line)); 
        }
        return tokens;
    }

    private Map<String, Set<RunningNfa>> initializeNfas() {
        Map<String, Set<RunningNfa>> runningNfaMap = new HashMap<String, Set<RunningNfa>>();
        for (String tokenName : nfas.keySet()) {
            NFA tokenNfa = nfas.get(tokenName);
            Set<RunningNfa> startStates = new HashSet<RunningNfa>();
            for (State startState : tokenNfa.getEpsilonClosure(tokenNfa.getStartState())) {
                startStates.add(new RunningNfa(tokenNfa, startState));
            }
            runningNfaMap.put(tokenName, startStates);
        }
        return runningNfaMap;
    }
    public List<Token> tokenize(String line) throws IOException, BadTokenException {
        List<Token> tokens = new ArrayList<Token>();
        PushbackReader lineReader = new PushbackReader(new StringReader(line), line.length());
        Stack<Character> leftovers = new Stack<Character>();
        StringBuilder tokenString = new StringBuilder();
        Map<String, Set<RunningNfa>> runningNfaMap = initializeNfas();
        int input;
        while (true) {
            Token token = null;
            while ((input = lineReader.read()) != -1) {
                char inputChar = (char) input;
                
                if (Character.isWhitespace(inputChar) && token == null) {
                    //System.out.println("looking at whitespace");
                    continue;
                }
                if (token != null) {
                    leftovers.push(inputChar);
                }
                tokenString.append(inputChar);
                Map<String, Set<RunningNfa>> newRunningNfaMap = new HashMap<String, Set<RunningNfa>>();
                for (Entry<String, Set<RunningNfa>> nfaSimPair : runningNfaMap.entrySet()) {
                    newRunningNfaMap.put(nfaSimPair.getKey(), advanceAll(nfaSimPair.getValue(), inputChar));
                    if (isAccepting(newRunningNfaMap.get(nfaSimPair.getKey()))) {
                        token = new Token(nfaSimPair.getKey(), tokenString.toString());
                        leftovers.clear();
                    }


                }
                runningNfaMap = newRunningNfaMap;
            }
            if (token == null) {
                throw new BadTokenException(tokenString.toString());
            }
            tokens.add(token);
            tokenString = new StringBuilder();
            if (leftovers.size() == 0) {
                break; // we're done with this line
            }
            if (!isAccepting(runningNfaMap.get((String) token.getType()))) {
                while (!leftovers.isEmpty()) {
                    lineReader.unread(leftovers.pop());
                }
            }
            token = null;
            runningNfaMap = initializeNfas();

        }
        return tokens;
    }
    private static boolean isAccepting(Collection<RunningNfa> runningNfas) {
        for (RunningNfa nfa : runningNfas) {
            if (nfa.isAccepting()) {
                return true;
            }
        }
        return false;

    }

    public int getTransitionCount() {
        return transitionCounter;
    }



    /*public static boolean run(NFA nfa, String input) {
        Set<RunningNfa> startStates = new HashSet<RunningNfa>();
        for (State startState : nfa.getEpsilonClosure(nfa.getStartState())) {
            startStates.add(new RunningNfa(nfa, startState));
        }
        for (int i = 0; i < input.length(); i++) {
            startStates = advanceAll(startStates, input.charAt(i));
        }
        System.out.println(startStates);
        return isAccepting(startStates);
    }*/
    private Set<RunningNfa> advanceAll(Collection<RunningNfa> runningNfas, Character input) {
        Set<RunningNfa> nextSet = new HashSet<RunningNfa>();
        for (RunningNfa nfa : runningNfas) {
            transitionCounter++;
            nextSet.addAll(nfa.doTransition(input));
        }
        return nextSet;
    }

    public static class RunningNfa {
        private NFA nfa;
        private State currentState;
        public RunningNfa(NFA nfa, State currentState) {
            this.nfa = nfa;
            this.currentState = currentState;
        }

        public Set<RunningNfa> doTransition(Character input) {
            Set<State> nextStates = nfa.getTransition(currentState, input);
            Set<State> eClosures = new HashSet<State>();
            for (State next : nextStates) {
                eClosures.addAll(nfa.getEpsilonClosure(next));
            }
            nextStates.addAll(eClosures);
            Set<RunningNfa> clones = new HashSet<RunningNfa>();
            for (State next : nextStates) {
                clones.add(new RunningNfa(nfa, next));
            }
            return clones;
        }

        public boolean isAccepting() {
            return currentState.isAccepting();
        }

        public String toString() {
            return "<NFA in state " + currentState.getName() + ">";
        }
    }
}

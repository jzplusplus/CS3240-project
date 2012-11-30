package base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import parser.ParseTree;
import parser.Symbol.NonterminalSymbol;
import util.NFA;
import util.State;



// given a parse tree, spit out an NFA.
public class NFABuilder {

	private Map<String, ParseTree> definedClasses;
	
	private Map<String, NFA> definedClassNfas;
	
	private Map<String, Set<Character>> classAcceptances;
	
	public NFABuilder(Map<String, ParseTree> definedClasses) {
		this(definedClasses, false);
	}
	
	public NFABuilder(Map<String, ParseTree> definedClasses, boolean verbose) { 
		this.definedClasses = definedClasses;
		this.definedClassNfas = new HashMap<String, NFA>();
		classAcceptances = new HashMap<String, Set<Character>>();
		// build the defined classes first
		for (Entry<String, ParseTree> entry : definedClasses.entrySet()) {
			if (verbose) {
				System.out.println("Building NFA for " + entry.getKey());
			}
			definedClassNfas.put(entry.getKey(), build(entry.getValue()));
			if (verbose) {
				System.out.println("Built: " + definedClassNfas.get(entry.getKey()));
			}
		}
	}
	
	public Map<String, NFA> build(Map<String, ParseTree> tokenTrees) {
		return build(tokenTrees, false);
	}

	
	public Map<String, NFA> build(Map<String, ParseTree> tokenTrees, boolean verbose) {
		Map<String, NFA> nfaMap = new HashMap<String, NFA>();
		for (Entry<String, ParseTree> entry : tokenTrees.entrySet()) {
			if (verbose) {
				System.out.println("Building NFA for " + entry.getKey());
			}
			nfaMap.put(entry.getKey(), build(entry.getValue()));
			if (verbose) {
				System.out.println("Built: " + nfaMap.get(entry.getKey()));
			}
		}
		return nfaMap;
	}
	
//	public Map<String, NFA> build() {
//		for (Entry<String, ParseTree> entry : definedClasses.entrySet()) {
//			definedClassNfas.put(entry.getKey(), build(entry.getValue()));
//		}
//		return definedClassNfas;
//	}

	public NFA build(ParseTree tree) {
		if (tree.getValue().isTerminal()) {
			throw new RuntimeException("this shouldn't be happening!");
		}
		NonterminalSymbol symbol = (NonterminalSymbol) tree.getValue();
		switch (symbol) {
		case CHAR_CLASS:
			// either a ., the start of a list, or a defined class
			if (tree.getChildren().size() == 1) {
				if (tree.getChild(0).getValue().isTerminal()) {
					// must be a dot: accept all characters
					return NFA.acceptAll();
				} else {
					// must be a defined class. RECURSE!
					return build(tree.getChild(0));
				}
			} else {
				// [ <char-class1>
				return build(tree.getChild(1));
			}
		case CHAR_CLASS1:
			// either charsetlist or excludeset
			return build(tree.getChild(0));
		case CHAR_SET:
			return NFA.acceptSet(getRange(tree));
		case CHAR_SET_LIST:
			if (tree.getChildren().size() == 2) {
				// char-set char-set-list
				NFA charset = build(tree.getChild(0));
				if (tree.getChild(1).getValue().isTerminal()) {
					return charset; // char-set-list was just ]
				} else {
					NFA charsetList = build(tree.getChild(1));
					return NFA.union(charset, charsetList);
				}
			} else {
				return null; // or maybe throw exception;
			}
		case CHAR_SET_TAIL:
			throw new RuntimeException("Charset-tail should be handled elsewhere!");
		case DEFINED_CLASS:
			// see if we have an NFA for this already.
			// the child of the tree should contain a node with the class name in it.
			String className = tree.getChild(0).getValue().getValue();
			if (this.definedClassNfas.keySet().contains(className)) {
				return this.definedClassNfas.get(className);
			} else {
				// otherwise we need to build it first
				NFA classNfa = build(definedClasses.get(className));
				definedClassNfas.put(className, classNfa);
				return classNfa;
			}
		case EXCLUDE_SET:
			// this is gonna be STUPID
			// if exclude_set_tail is a defined class, do some bullshit
			// otherwise
			Set<Character> exclude = getRange(tree.getChild(1));
			if (tree.getChild(4).getChildren().size() == 3) {
				Set<Character> include = getRange(tree.getChild(4).getChild(1));
				include.removeAll(exclude);
				return NFA.acceptSet(include);
			} else {
				String definedClass = tree.getChild(4).getChild(0).getChild(0).getValue().getValue();
				Set<Character> include = getAcceptingCharacters(definedClass);
				include.removeAll(exclude);
				return NFA.acceptSet(include);
			}
		case EXCLUDE_SET_TAIL:
			throw new RuntimeException("exclude-set-tail should be handled elsewhere!");
		case REGEX:
			// this is stupid
			return build(tree.getChild(0));
		case REXP:
			// if rexp' isn't null...
			if (tree.getChildren().size() == 2) {
				NFA rexp1 = build(tree.getChild(0));
				NFA rexpPrime = build(tree.getChild(1));
				return NFA.union(rexp1, rexpPrime);
			} else {
				return build(tree.getChild(0));
			}
		case REXP1:
		case REXP1_PRIME: // shared logic for these two
			if (tree.getChildren().size() == 2) {
				// rexp1' is not null
				NFA rexp2 = build(tree.getChild(0));
				NFA rexp1Prime = build(tree.getChild(1));
				return NFA.concatenate(rexp2, rexp1Prime);
			} else {
				// rexp1' is null
				return build(tree.getChild(0));
			}
		case REXP2:
			// either 4 children, 3, 2, 1, or 0.
			if (tree.getChildren().size() == 4) {
				// ( <rexp> ) <rexp2-tail> where rexp-tail is not null
				NFA inside = build(tree.getChild(1));
				String operator = tree.getChild(3).getChild(0).getValue().getValue();
				if (operator.equals("+")) {
					return NFA.kleenePlus(inside);
				} else if (operator.equals("*")) {
					return NFA.kleeneStar(inside);
				} else {
					throw new RuntimeException("Got something other than * or + for rexp2-tail");
				}
			} else if (tree.getChildren().size() == 3) {
				// rexp2-tail is null, just return the insde
				return build(tree.getChild(1));
			} else if (tree.getChildren().size() == 2) {
				// must be RE_CHAR <rexp2-tail>
				String reChar = tree.getChild(0).getValue().getValue();
				NFA reCharNfa = NFA.acceptCharacter(reChar.charAt(reChar.length()-1));
//				for (int i = 1; i < reChar.length(); i++) { // gobble them up
//					reCharNfa = NFA.concatenate(reCharNfa, NFA.acceptCharacter(reChar.charAt(i)));
//				}
				String operator = tree.getChild(1).getChild(0).getValue().getValue();
				if (operator.equals("+")) {
					return NFA.kleenePlus(reCharNfa);
				} else if (operator.equals("*")) {
					return NFA.kleeneStar(reCharNfa);
				} else {
					throw new RuntimeException("Got something other than * or + for rexp2-tail");
				}
			} else if (tree.getChildren().size() == 1){
				// could be just RE_CHAR or <rexp3>
				if (tree.getChild(0).getValue().isTerminal()) {
					String reChar = tree.getChild(0).getValue().getValue();
					NFA reCharNfa = NFA.acceptCharacter(reChar.charAt(reChar.length()-1));
//					for (int i = 1; i < reChar.length(); i++) { // gobble them up
//						reCharNfa = NFA.concatenate(reCharNfa, NFA.acceptCharacter(reChar.charAt(i)));
//					}
					return reCharNfa;
				} else { // rexp3
					return build(tree.getChild(0));
				}
			} else {
				// rexp3 is null
				return null;
			}
		case REXP2_TAIL:
			throw new RuntimeException("Rexp2-tail should be handled elsewhere!");
		case REXP3:
			return build(tree.getChild(0)); // dumb passthrough
		case REXP_PRIME:
			// should have two or three children: UNION, rexp1, and optionally REXP'
			if (tree.getChildren().size() == 3) {
				// return union of child 2 and 3
				NFA rexp1 = build(tree.getChild(1));
				NFA rexpPrime = build(tree.getChild(2));
				return NFA.union(rexp1, rexpPrime);
			} else {
				// otherwise, rexp' is empty
				return build(tree.getChild(1));
			}
			
		default:
			throw new RuntimeException("Got an unhandled symbol: " + tree.getValue().getValue());
		
		}
	}
	
	private Set<Character> getAcceptingCharacters(String definedClass) {
		if (classAcceptances.containsKey(definedClass)) {
			HashSet<Character> copy = new HashSet<Character>();
			copy.addAll(classAcceptances.get(definedClass));
			return copy;
		} else {
			if (!definedClassNfas.containsKey(definedClass)) {
				// uh, if it's not in here, we'd better build it first
				definedClassNfas.put(definedClass, build(definedClasses.get(definedClass)));
			}
			NFA classNfa = definedClassNfas.get(definedClass);
			Set<Character> accepts = new HashSet<Character>();
			for (int i = 32; i <= 126; i++) {
				Character c = (char) i;
				boolean accepted = false;
				for (State start : classNfa.getEpsilonClosure(classNfa.getStartState())) {
					// see if transition(start, c) is accepting
					Set<State> possibleCurrentStates = new HashSet<State>();
					Set<State> transitions = classNfa.getTransition(start, c);
					for (State transition : transitions) {
						possibleCurrentStates.add(transition);
						possibleCurrentStates.addAll(classNfa.getEpsilonClosure(transition));
					}
					// remove non-accepting states:
					transitions.retainAll(classNfa.getAcceptStates());
					if (!transitions.isEmpty()) { // if there are some accepting states left...
						accepted = true;
						break;
					}
				}
				if (accepted) {
					accepts.add(c);
				}
			}
			classAcceptances.put(definedClass, accepts);
			HashSet<Character> copy = new HashSet<Character>();
			copy.addAll(accepts);
			return copy;
		}
	}
	
	private Set<Character> getRange(ParseTree tree) {
		if (tree.getValue() != NonterminalSymbol.CHAR_SET) {
			throw new RuntimeException("Shouldn't be calling getRange unless symbol is charset");
		}
		// otherwise, the first child is the start symbol (possibly "escaped")
		HashSet<Character> chars = new HashSet<Character>();
		if (tree.getChildren().size() == 1) {
			String accept = tree.getChild(0).getValue().getValue();
			chars.add(accept.charAt(accept.length() - 1));
		} else {
			// if there's a second child, we need to do a range operation
			// COUNT UP!!!!!!
			String startString = tree.getChild(0).getValue().getValue();
			String endString = tree.getChild(1).getChild(1).getValue().getValue();
			// it's possible some of these are escaped-- if so, grab the last char.
			Character start = startString.charAt(startString.length() - 1);
			Character end = endString.charAt(endString.length() - 1);
			if (end < start) {
				throw new RuntimeException("Invalid range: [" + start +"-" + end + "]");
			}
			// otherwise, we're good to go
			for (int i = (int)start; i <= (int)end; i++) {
				chars.add((char)i);
			}
			
		}
		return chars;
	}


	
}

package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Stack;

import parser.Symbol.NonterminalRegexSymbol;
import parser.Symbol.TerminalSymbol;
import exception.ParseException;

public class RegexParser {

	
	public static void populateParseMaps(String filename, Map<String, ParseTree> definedClasses, Map<String, ParseTree> tokenClasses) throws ParseException, IOException {
		populateParseMaps(filename, definedClasses, tokenClasses, false);
	}

	
	public static void populateParseMaps(String filename, Map<String, ParseTree> definedClasses, Map<String, ParseTree> tokenClasses, boolean verbose) throws ParseException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        boolean currentlyParsingDefinedClasses = true;
        while ((line = reader.readLine()) != null) {
        	if (currentlyParsingDefinedClasses && (line.isEmpty() || line.replaceAll("\\s", "").isEmpty())) {
        		if (verbose) {
        			System.out.println("done parsing defined classes, moving on to token definitions");
        		}
        		currentlyParsingDefinedClasses = false; // we've moved on to the token definitions
        	}
        	if (line.replaceAll("\\s", "").startsWith("%")) {
        		continue; // it's a comment line
        	}
        	String[] tokens = line.split("\\s+", 2);
        	if (tokens.length != 2) {
        		continue;
        	}
        	if (verbose) {
        		System.out.println("Looking at " + tokens[0] + " -->" + tokens[1]);
        	}
    		ParseTree tree = RegexParser.parse(tokens[1], definedClasses);
    		if (currentlyParsingDefinedClasses) {
    			definedClasses.put(tokens[0], tree);
    		} else {
    			tokenClasses.put(tokens[0], tree);
    		}
    		if (verbose) {
    			tree.print();
    		}
        }		
	}
	
	
	// singleton
	private RegexParser() throws ParseException {

	}

	public static ParseTree parse(String regex,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		PushbackReader reader = new PushbackReader(new StringReader(regex),
				regex.length());
		ParseTree root = new ParseTree(NonterminalRegexSymbol.REGEX);
		rexp(root, reader, definedClasses);
//		if (peekAndConsumeWhitespace(reader) != null || peekAndConsumeWhitespace(reader)) {
//			throw new ParseException("Lingering characters found! This is not a valid regex!");
//		}
		return root;
	}

	// <rexp> -> <rexp1> <rexp��>
	private static void rexp(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		ParseTree rexpNode = new ParseTree(NonterminalRegexSymbol.REXP);
		rexp1(rexpNode, reader, definedClasses);
		rexpPrime(rexpNode, reader, definedClasses);
		root.addChild(rexpNode);

	}

	// <rexp1> -> <rexp2> <rexp1��> 
	private static void rexp1(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {

		ParseTree rexp1Node = new ParseTree(NonterminalRegexSymbol.REXP1);
		rexp2(rexp1Node, reader, definedClasses);
		rexp1Prime(rexp1Node, reader, definedClasses);
		root.addChild(rexp1Node);

	}

	// rexpPrime -> UNION <rexp1> <rexp��> | E
	private static void rexpPrime(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		//Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		
		if (ParserUtils.peekSequence(reader, "|")) {
			ParserUtils.consumeSequence(reader, "|");
			ParseTree rexpPrimeNode = new ParseTree(
					NonterminalRegexSymbol.REXP_PRIME);
			rexpPrimeNode.addChild(new ParseTree(new TerminalSymbol("|")));

			rexp1(rexpPrimeNode, reader, definedClasses);
			rexpPrime(rexpPrimeNode, reader, definedClasses);
			root.addChild(rexpPrimeNode);

		} else {
			return;
		}

	}

	// <rexp1��> -> <rexp2> <rexp1��> | E
	private static void rexp1Prime(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {

		if (ParserUtils.peekSequence(reader,"(") 
				|| peekReChar(reader) != null 
				|| ParserUtils.peekSequence(reader,".)")
				|| ParserUtils.peekSequence(reader,"[") 
				|| ParserUtils.peekSequence(reader, "$")) {
			ParseTree rexp1PrimeNode = new ParseTree(
					NonterminalRegexSymbol.REXP1_PRIME);
			rexp2(rexp1PrimeNode, reader, definedClasses);
			rexp1Prime(rexp1PrimeNode, reader, definedClasses);
			root.addChild(rexp1PrimeNode);
		} else {
			return;
		}

	}

	// <rexp2> -> (<rexp>) <rexp2-tail> | RE_CHAR <rexp2-tail> | <rexp3>
	private static void rexp2(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		// Token ahead = te.peekNextToken();
		//Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		ParseTree rexp2Node = new ParseTree(NonterminalRegexSymbol.REXP2);
		if (ParserUtils.peekSequence(reader, "(")) {
			ParserUtils.consumeSequence(reader, "(");
			rexp2Node.addChild(new ParseTree(new TerminalSymbol("(")));
			rexp(rexp2Node, reader, definedClasses);
			ParserUtils.consumeSequence(reader, ")");
			rexp2Node.addChild(new ParseTree(new TerminalSymbol(")")));
			rexp2_tail(rexp2Node, reader, definedClasses);
		} else if (peekReChar(reader) != null) {
			String reChar = peekReChar(reader);
			ParserUtils.consumeSequence(reader, reChar);
//			for (int i = 0; i < reChar.length(); i++) {
//				reader.read(); // consume it
//			}
			rexp2Node.addChild(new ParseTree(new TerminalSymbol(reChar)));
			rexp2_tail(rexp2Node, reader, definedClasses);
		} else {
			rexp3(rexp2Node, reader, definedClasses);
		}

		root.addChild(rexp2Node);

	}

	// <rexp2-tail> -> * | + | E
	private static void rexp2_tail(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		if (ParserUtils.peekSequence(reader, "+")
				|| ParserUtils.peekSequence(reader,"*")) {
			Character next = ParserUtils.peekAndConsumeWhitespace(reader);
			ParserUtils.consumeSequence(reader, next.toString());
			ParseTree rexp2TailNode = new ParseTree(
					NonterminalRegexSymbol.REXP2_TAIL);
			rexp2TailNode.addChild(new ParseTree(new TerminalSymbol(next
					.toString())));
			root.addChild(rexp2TailNode);
		} else {
			return;
		}
	}

	// <rexp3> -> <char-class> | E
	private static void rexp3(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
//		Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		if (ParserUtils.peekSequence(reader, ".")
				|| ParserUtils.peekSequence(reader, "[")
				|| ParserUtils.peekSequence(reader, "$")) {
			ParseTree rexp3Node = new ParseTree(NonterminalRegexSymbol.REXP3);
			char_class(rexp3Node, reader, definedClasses);
			root.addChild(rexp3Node);
		} else {
			return;
		}
	}

	// <char-class> -> . | [ <char-class1> | <defined-class>
	private static void char_class(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		//Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		ParseTree charClassNode = new ParseTree(NonterminalRegexSymbol.CHAR_CLASS);
		if (ParserUtils.peekSequence(reader, ".")) {
			ParserUtils.consumeSequence(reader, ".");
			charClassNode.addChild(new ParseTree(new TerminalSymbol(".")));
		} else if (ParserUtils.peekSequence(reader, "[")) {
			ParserUtils.consumeSequence(reader, "[");
			charClassNode.addChild(new ParseTree(new TerminalSymbol("[")));
			char_class1(charClassNode, reader, definedClasses);
		} else {
			defined_class(charClassNode, reader, definedClasses);
		}
		root.addChild(charClassNode);
	}

	// <char-class1> -> <char-set-list> | <exclude-set>
	private static void char_class1(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		// Token ahead = te.peekNextToken();
		Character next = ParserUtils.peek(reader);
		ParseTree charClass1Node = new ParseTree(NonterminalRegexSymbol.CHAR_CLASS1);
		if (Character.valueOf('^').equals(next)) {
			exclude_set(charClass1Node, reader, definedClasses);

		} else {
			char_set_list(charClass1Node, reader, definedClasses);
		}
		root.addChild(charClass1Node);

	}

	// <char-set-list> -> <char-set> <char-set-list> | ]
	private static void char_set_list(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		ParseTree charSetListNode = new ParseTree(
				NonterminalRegexSymbol.CHAR_SET_LIST);
		if (peekClsChar(reader) != null) {
			char_set(charSetListNode, reader, definedClasses);
			char_set_list(charSetListNode, reader, definedClasses);
		} else {
			ParserUtils.consumeSequence(reader, "]");
			charSetListNode.addChild(new ParseTree(new TerminalSymbol("]")));
		}
//		} else if (Character.valueOf(']').equals(ParserUtils.peekAndConsumeWhitespace(reader))) {
//			System.out.println(ParserUtils.peek(reader));
//			reader.read(); // consume it
//			System.out.println(ParserUtils.peek(reader));
//			System.out.println("YEEHAW");
//			charSetListNode.addChild(new ParseTree(new TerminalSymbol("]")));
//		} else {
//			throw new ParseException(
//					"Char-set-list: Expected cls char or ], got "
//							+ ParserUtils.peek(reader));
//		}
		root.addChild(charSetListNode);

	}

	// <char-set> -> CLS_CHAR <char-set-tail>
	private static void char_set(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
		String clsChar = peekClsChar(reader);
		if (clsChar == null) {
			throw new ParseException("Char-set: expected clsChar, got "
					+ ParserUtils.peek(reader));
		}
		ParseTree charSetNode = new ParseTree(NonterminalRegexSymbol.CHAR_SET);
		charSetNode.addChild(new ParseTree(new TerminalSymbol(clsChar)));
//		for (int i = 0; i < clsChar.length(); i++) {
//			reader.read(); // consume the characters
//		}
		ParserUtils.consumeSequence(reader, clsChar);
		char_set_tail(charSetNode, reader, definedClasses);
		root.addChild(charSetNode);
	}

	// <char-set-tail> -> - CLS_CHAR | E
	private static void char_set_tail(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws IOException,
			ParseException {
		Character next = ParserUtils.peek(reader);
		if (!ParserUtils.peekSequence(reader, "-")) {
			return;
		}
		//reader.read(); // consume
		ParserUtils.consumeSequence(reader, "-");
		ParseTree charSetTailNode = new ParseTree(
				NonterminalRegexSymbol.CHAR_SET_TAIL);
		charSetTailNode.addChild(new ParseTree(new TerminalSymbol("-")));
		String clsChar = peekClsChar(reader);
		if (clsChar == null) {
			throw new ParseException("Char-set-tail: expected clsChar, got "
					+ ParserUtils.peek(reader));
		}
		charSetTailNode.addChild(new ParseTree(new TerminalSymbol(clsChar)));
//		for (int i = 0; i < clsChar.length(); i++) {
//			reader.read(); // consume clsChar
//		}
		ParserUtils.consumeSequence(reader, clsChar);
		root.addChild(charSetTailNode);

	}

	// <exclude-set> -> ^ <char-set>] IN <exclude-set-tail>
	private static void exclude_set(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
//		Character next = ParserUtils.peekAndConsumeWhitespace(reader);
//		if (!Character.valueOf('^').equals(next)) {
//			throw new ParseException("Exclude-set: Expected ^, got " + next);
//		}
		ParseTree excludeSetNode = new ParseTree(NonterminalRegexSymbol.EXCLUDE_SET);
//		reader.read(); // consume ^
		ParserUtils.consumeSequence(reader, "^");
		excludeSetNode.addChild(new ParseTree(new TerminalSymbol("^")));
//		next = ParserUtils.peekAndConsumeWhitespace(reader);
//		if (Character.valueOf(' ').equals(next)) { // spaces are optional here,
//													// but let's not make a node
//													// for them
//			reader.read(); // consume that space!
//		}
		char_set(excludeSetNode, reader, definedClasses);
		// let's check if the next 4 chars are "] IN " :
		
//		next = ParserUtils.peekAndConsumeWhitespace(reader);
//		if (!Character.valueOf(']').equals(next)) {
//			throw new ParseException("Exclude-set: expected ], got " + next);
//		}
//		reader.read(); // eat the ]
		ParserUtils.consumeSequence(reader, "]");
		excludeSetNode.addChild(new ParseTree(new TerminalSymbol("]")));
//		next = ParserUtils.peekAndConsumeWhitespace(reader);
//		if (!Character.valueOf('I').equals(next)) {
//			throw new ParseException("Exclude-set: expected IN, got " + next);
//		}
//		reader.read(); // eat the I
//		next = ParserUtils.peek(reader);
//		if (!Character.valueOf('N').equals(next)) {
//			throw new ParseException("Exclude-set: expected IN, got " + next);
//		}
//		reader.read(); // eat the N
		ParserUtils.consumeSequence(reader, "IN");

		excludeSetNode.addChild(new ParseTree(new TerminalSymbol("IN")));
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < 5; i++) {
//			sb.append((char) reader.read());
//		}
//		if (!sb.toString().equals("] IN ")) {
//			throw new ParseException("Exclude-set: expected ] IN, got "
//					+ sb.toString());
//		}
		exclude_set_tail(excludeSetNode, reader, definedClasses);

		root.addChild(excludeSetNode);

	}

	// <exclude-set-tail> -> [<char-set>] | <defined-class>
	private static void exclude_set_tail(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws ParseException,
			IOException {
//		Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		ParseTree excludeSetTailNode = new ParseTree(
				NonterminalRegexSymbol.EXCLUDE_SET_TAIL);
		if (ParserUtils.peekSequence(reader, "[")) {

			ParserUtils.consumeSequence(reader, "[");
			excludeSetTailNode.addChild(new ParseTree(new TerminalSymbol("[")));
			char_set(excludeSetTailNode, reader, definedClasses);
			ParserUtils.consumeSequence(reader, "]");
//			next = (char) reader.read(); // try to consume ]
//			if (!Character.valueOf(']').equals(next)) {
//				throw new ParseException("Exclude-set-tail: Expected ], got "
//						+ next);
//			}
			// we already consumed ], now add to the tree
			excludeSetTailNode.addChild(new ParseTree(new TerminalSymbol("]")));
		} else {
			defined_class(excludeSetTailNode, reader, definedClasses);
		}
		root.addChild(excludeSetTailNode);

	}

	private static void defined_class(ParseTree root, PushbackReader reader,
			Map<String, ParseTree> definedClasses) throws IOException,
			ParseException {
		// find longest match of class name in reader, then just plop the whole class name in there and deal with
		// it later. ALSO, defined classes need spaces after them, probably!
		int input;
		StringBuilder nameBuilder = new StringBuilder();
		Stack<Character> leftovers = new Stack<Character>();
		String name = null;
		while ((input = reader.read()) != -1) {
			char inputChar = (char) input;
			if (name != null && Character.valueOf(' ').equals(inputChar)) {
				break; // gobble the space
			}
			//if (name == null) {
				leftovers.push(inputChar);
			//}
			nameBuilder.append(inputChar);
			if (definedClasses.keySet().contains(nameBuilder.toString())) {
				name = nameBuilder.toString();
				leftovers.clear();
			}
		}
		if (name == null) {
			throw new ParseException("Defined-class: got "
					+ nameBuilder.toString());
		}
		while (!leftovers.isEmpty()) {
			reader.unread(leftovers.pop());
		}
//		System.out.println("defined class found: " + name);
		ParseTree definedClass = new ParseTree(NonterminalRegexSymbol.DEFINED_CLASS);
		definedClass.addChild(new ParseTree(new TerminalSymbol(name)));
		root.addChild(definedClass);

	}

	private static String peekClsChar(PushbackReader reader) throws IOException {
		Character next = ParserUtils.peek(reader);
		// first check if it's ascii printable (but not \, ^, -, [ and ] )
		if (next == null || !ParserUtils.isAsciiPrintable(next)) {
			return null;
		} else if (!isForbiddenInCls(next)) {
			return next.toString();
		}

		// then check if it's one of the escape sequences:
		// \\, \^, \-, \[ and \]
		reader.read();
		int oneAfterNextInt = reader.read();
		Character oneAfterNext = (char) oneAfterNextInt;
		// we got em, now pop em back on
		reader.unread(oneAfterNext);
		reader.unread(next);

		if (oneAfterNextInt != -1 && next.equals('\\')
				&& isForbiddenInCls(oneAfterNext)) {
			return next.toString() + oneAfterNext.toString();
		}

		return null;
	}

	private static boolean isForbiddenInCls(Character next) {
		return (next.equals('\\') || next.equals('^') || next.equals('-')
				|| next.equals('[') || next.equals(']') || next.equals('$'));
	}

	private static String peekReChar(PushbackReader reader) throws IOException {
		Character next = ParserUtils.peekAndConsumeWhitespace(reader);
		// first check if it's ascii printable (but not space, \, *, +, ?, |, [,
		// ], (, ), ., ' and ")
		if (next == null) {
			return null;
		} else if (ParserUtils.isAsciiPrintable(next) && !isForbiddenInRe(next)) {
			return next.toString();
		}

		// then check if it's one of the escape sequences:
		// \ (backslash space), \\, \*, \+, \?, \|, \[, \], \(, \), \., \' and
		// \"
		next = (char) reader.read();
		int oneAfterNextInt = reader.read();
		Character oneAfterNext = (char) oneAfterNextInt;
		// we got em, now pop em back on
		reader.unread(oneAfterNext);
		reader.unread(next);

		if (oneAfterNextInt != -1 && next.equals('\\')
				&& isForbiddenInRe(oneAfterNext)) {
			return next.toString() + oneAfterNext.toString();
		}

		return null;
	}

	private static boolean isForbiddenInRe(Character c) {
		return (c.equals(' ') || c.equals('\\') || c.equals('*')
				|| c.equals('+') || c.equals('?') || c.equals('|')
				|| c.equals('[') || c.equals(']') || c.equals('(')
				|| c.equals(')') || c.equals('.') || c.equals('\'') || c
					.equals('"') || c.equals('$'));
	}

}

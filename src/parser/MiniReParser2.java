package parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.Symbol.NonterminalMiniReSymbol;
import parser.Symbol.ReservedWord;
import parser.Symbol.TerminalSymbol;
import exception.ParseException;


public class MiniReParser2 {

	
	private MiniReParser2() { }
	
	public static ParseTree parse(String program) throws IOException, ParseException {
		PushbackReader reader = new PushbackReader(new StringReader(program),
				program.length()); // this could create quite a large buffer...
		ParseTree root = new ParseTree(NonterminalMiniReSymbol.MINIRE_PROGRAM);
		try {
			ParserUtils.consumeSequence(reader, "begin");
			root.addChild(new ParseTree(ReservedWord.BEGIN));
		} catch (ParseException e) {
			throw new ParseException("MiniRE-Program: Couldn't match 'begin'");
		}
		statementList(root, reader);
		try {
			ParserUtils.consumeSequence(reader, "end");
			root.addChild(new ParseTree(ReservedWord.END));

		} catch (ParseException e) {
			throw new ParseException("MiniRE-Program: Couldn't match 'end'");
		}
		return root;
	}

	private static void statementList(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		ParseTree statementList = new ParseTree(NonterminalMiniReSymbol.STATEMENT_LIST);
		statement(statementList, reader);
		statementListTail(statementList, reader);
		root.addChild(statementList);
	}
	
	private static void statementListTail(ParseTree root,
			PushbackReader reader) throws IOException, ParseException {
		// so we need the next character to be either an ID or the following reserved words:
		// replace, recursivereplace, print
		if (peekId(reader) != null
				|| ParserUtils.peekSequence(reader, ReservedWord.REPLACE.getValue())
				|| ParserUtils.peekSequence(reader, ReservedWord.RECURSIVE_REPLACE.getValue())
				|| ParserUtils.peekSequence(reader, ReservedWord.PRINT.getValue())) {
			ParseTree statementListTail = new ParseTree(NonterminalMiniReSymbol.STATEMENT_LIST_TAIL);
			
			statement(statementListTail, reader);
			statementListTail(statementListTail, reader);
			root.addChild(statementListTail);
		} else {
			return;
		}
		
	}

	private static void statement(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		// so we need the next character to be either an ID or the following reserved words:
		// replace, recursivereplace, print
		ParseTree statement = new ParseTree(NonterminalMiniReSymbol.STATEMENT);
		String peekId = peekId(reader);
		if (peekId != null) {
			statement.addChild(new ParseTree(new TerminalSymbol(peekId)));
			ParserUtils.consumeSequence(reader, peekId);
			// then we can have = , = #, or = maxfreq.
			// but no matter what we need to gobble up a = first.
			try {
				ParserUtils.consumeSequence(reader, ReservedWord.ASSIGN.getValue());
				statement.addChild(new ParseTree(ReservedWord.ASSIGN));
			} catch (ParseException e) {
				throw new ParseException("Statement: Couldn't find =");
			}
			// if the next character is in <exp>...
			if (ParserUtils.peekSequence(reader, "(") || peekId(reader) != null 
					|| ParserUtils.peekSequence(reader, ReservedWord.FIND.getValue())) {
				//... then we should recurse into <exp>.
				exp(statement, reader);
			} else if (ParserUtils.peekSequence(reader, ReservedWord.INT.getValue())) {
				// gobble up the #, then recurse into <exp>
				ParserUtils.consumeSequence(reader, ReservedWord.INT.getValue());
				statement.addChild(new ParseTree(ReservedWord.INT));
				exp(statement, reader);
			} else { // must be maxfreqstring ( id )
				ParserUtils.consumeSequence(reader, ReservedWord.MAXFREQSTRING.getValue());
				statement.addChild(new ParseTree(ReservedWord.MAXFREQSTRING));
				ParserUtils.consumeSequence(reader, "(");
				statement.addChild(new ParseTree(new TerminalSymbol("(")));
				String id = peekId(reader);
				ParserUtils.consumeSequence(reader, id);
				statement.addChild(new ParseTree(new TerminalSymbol(id)));
				ParserUtils.consumeSequence(reader, ")");
				statement.addChild(new ParseTree(new TerminalSymbol(")")));
			}
			// regardless, gobble up a semicolon.
			ParserUtils.consumeSequence(reader, ReservedWord.SEMICOLON.getValue());
			statement.addChild(new ParseTree(ReservedWord.SEMICOLON));
			
		} else if (ParserUtils.peekSequence(reader, ReservedWord.REPLACE.getValue())) {
			// replace REGEX with ASCII-STR in <file-names> ;
			// replace
			ParserUtils.consumeSequence(reader, ReservedWord.REPLACE.getValue());
			statement.addChild(new ParseTree(ReservedWord.REPLACE));
			
			// REGEX
			String regex = consumeRegex(reader);
			statement.addChild(new ParseTree(new TerminalSymbol(regex)));
			
			// with
			ParserUtils.consumeSequence(reader, ReservedWord.WITH.getValue());
			statement.addChild(new ParseTree(ReservedWord.WITH));
			
			// ASCII
			String ascii = peekAscii(reader);
			if (ascii == null) {
				throw new ParseException("Statement (replace): found invalid ascii string");
			}
			ParserUtils.consumeSequence(reader, '"' + ascii + '"');
			statement.addChild(new ParseTree(new TerminalSymbol(ascii)));
			
			// in
			ParserUtils.consumeSequence(reader, ReservedWord.IN.getValue());
			statement.addChild(new ParseTree(ReservedWord.IN));
			
			// <file-names>
			fileNames(statement, reader);
			
			// ;
			ParserUtils.consumeSequence(reader, ReservedWord.SEMICOLON.getValue());
			statement.addChild(new ParseTree(ReservedWord.SEMICOLON));
			
		} else if (ParserUtils.peekSequence(reader, ReservedWord.RECURSIVE_REPLACE.getValue())) {
			// recursivereplace REGEX with ASCII-STR in <file-names> ;
			// recursivereplace
			ParserUtils.consumeSequence(reader, ReservedWord.RECURSIVE_REPLACE.getValue());
			statement.addChild(new ParseTree(ReservedWord.RECURSIVE_REPLACE));
			
			// REGEX
			String regex = consumeRegex(reader);
			statement.addChild(new ParseTree(new TerminalSymbol(regex)));
			
			// with
			ParserUtils.consumeSequence(reader, ReservedWord.WITH.getValue());
			statement.addChild(new ParseTree(ReservedWord.WITH));
			
			// ASCII
			String ascii = peekAscii(reader);
			if (ascii == null) {
				throw new ParseException("Statement (recursivereplace): found invalid ascii string");
			}
			ParserUtils.consumeSequence(reader, '"' + ascii + '"');
			statement.addChild(new ParseTree(new TerminalSymbol(ascii)));
			
			// in
			ParserUtils.consumeSequence(reader, ReservedWord.IN.getValue());
			statement.addChild(new ParseTree(ReservedWord.IN));

			// <file-names>
			fileNames(statement, reader);

			// ;
			ParserUtils.consumeSequence(reader, ReservedWord.SEMICOLON.getValue());
			statement.addChild(new ParseTree(ReservedWord.SEMICOLON));
			
		} else if (ParserUtils.peekSequence(reader, ReservedWord.PRINT.getValue())) {
			// print ( <exp-list> ) ;
			ParserUtils.consumeSequence(reader, ReservedWord.PRINT.getValue());
			statement.addChild(new ParseTree(ReservedWord.PRINT));
			
			// (
			ParserUtils.consumeSequence(reader, "(");
			statement.addChild(new ParseTree(new TerminalSymbol("(")));
			
			// <exp-list>
			expList(statement, reader);
			
			// );
			ParserUtils.consumeSequence(reader, ")");
			statement.addChild(new ParseTree(new TerminalSymbol(")")));
			
			// ;
			ParserUtils.consumeSequence(reader, ReservedWord.SEMICOLON.getValue());
			statement.addChild(new ParseTree(ReservedWord.SEMICOLON));
			
			
		} else {
			throw new ParseException("Statement: Got following invalid character: " + ParserUtils.peek(reader));
		}
		root.addChild(statement);
		
	}

	
	private static void fileNames(ParseTree root, PushbackReader reader) throws ParseException, IOException {
		//<source-file> >! <destination-file>
		ParseTree fileNames = new ParseTree(NonterminalMiniReSymbol.FILE_NAMES);
		sourceFile(fileNames, reader);
		ParserUtils.consumeSequence(reader, ReservedWord.WRITE_TO.getValue());
		fileNames.addChild(new ParseTree(ReservedWord.WRITE_TO));
		destinationFile(fileNames, reader);
		
		root.addChild(fileNames);
	}

	private static void sourceFile(ParseTree root, PushbackReader reader) throws ParseException, IOException {
		String filename = peekAscii(reader);
		if (filename == null) {
			throw new ParseException("Source-file: Couldn't find valid filename");
		}
		ParseTree sourceFile = new ParseTree(NonterminalMiniReSymbol.SOURCE_FILE);
		ParserUtils.consumeSequence(reader, '"' + filename + '"' );
		sourceFile.addChild(new ParseTree(new TerminalSymbol(filename)));
		root.addChild(sourceFile);
	}
	
	private static void destinationFile(ParseTree root,
			PushbackReader reader) throws IOException, ParseException {
		String filename = peekAscii(reader);
		if (filename == null) {
			throw new ParseException("Destination-file: Couldn't find valid filename");
		}
		ParserUtils.consumeSequence(reader, '"' + filename + '"');
		ParseTree sourceFile = new ParseTree(NonterminalMiniReSymbol.DESTINATION_FILE);
		sourceFile.addChild(new ParseTree(new TerminalSymbol(filename)));
		root.addChild(sourceFile);
	}

	private static void expList(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		// <exp> <exp-list-tail>
		ParseTree expList = new ParseTree(NonterminalMiniReSymbol.EXP_LIST);
		exp(expList, reader);
		expListTail(expList, reader);
		
		root.addChild(expList);
		
	}
	
	private static void expListTail(ParseTree root, PushbackReader reader) throws ParseException, IOException {
		// , <exp> <exp-list-tail> | e
		if (ParserUtils.peekSequence(reader, ",")) {
			ParseTree expListTail = new ParseTree(NonterminalMiniReSymbol.EXP_LIST_TAIL);
			ParserUtils.consumeSequence(reader, ",");
			expListTail.addChild(new ParseTree(new TerminalSymbol(",")));
			
			exp(expListTail, reader);
			expListTail(expListTail, reader);
			
			root.addChild(expListTail);
		} else {
			return;
		}
	}

	private static void exp(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		//ID | ( <exp> ) 
		// <term> <exp-tail>
		ParseTree exp = new ParseTree(NonterminalMiniReSymbol.EXP);
		String id = peekId(reader);
		if (id != null) {
			exp.addChild(new ParseTree(new TerminalSymbol(id)));
			ParserUtils.consumeSequence(reader, id);
		} else if (ParserUtils.peekSequence(reader, "(")) {
			// ( <exp> )
			ParserUtils.consumeSequence(reader, "(");
			exp.addChild(new ParseTree(new TerminalSymbol("(")));
			exp(exp, reader);
			ParserUtils.consumeSequence(reader, ")");
			exp.addChild(new ParseTree(new TerminalSymbol(")")));
		} else {
			term(exp, reader);
			expTail(exp, reader);
		}
		root.addChild(exp);
		
		
	}
	
	private static void expTail(ParseTree root, PushbackReader reader) throws IOException, ParseException {
//		<bin-op> <term> <exp-tail> | e
		// if it's not epsilon, we need the first token to be diff | union | inters
		if (ParserUtils.peekSequence(reader, ReservedWord.DIFF.getValue())
				|| ParserUtils.peekSequence(reader, ReservedWord.UNION.getValue())
				|| ParserUtils.peekSequence(reader, ReservedWord.INTERS.getValue())) {
			ParseTree expTail = new ParseTree(NonterminalMiniReSymbol.EXP_TAIL);
			binOp(expTail, reader);
			term(expTail, reader);
			expTail(expTail, reader);
			root.addChild(expTail);
		} else {
			return;
		}
		
	}
	

	private static void term(ParseTree root, PushbackReader reader) throws ParseException, IOException {
		// find REGEX in <file-name>
		ParseTree term = new ParseTree(NonterminalMiniReSymbol.TERM);
		
		// find
		ParserUtils.consumeSequence(reader, ReservedWord.FIND.getValue());
		term.addChild(new ParseTree(ReservedWord.FIND));
		
		// REGEX
		String regex = consumeRegex(reader);
		term.addChild(new ParseTree(new TerminalSymbol(regex)));
		
		// in
		ParserUtils.consumeSequence(reader, ReservedWord.IN.getValue());
		term.addChild(new ParseTree(ReservedWord.IN));
		
		// filename
		fileName(term, reader);
		
		root.addChild(term);
	}

	private static void fileName(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		ParseTree fileName = new ParseTree(NonterminalMiniReSymbol.FILE_NAME);
		String ascii = peekAscii(reader);
		if (ascii == null) {
			throw new ParseException("file-name: Couldn't find valid filename");
		}
		ParserUtils.consumeSequence(reader, '"' + ascii + '"');
		fileName.addChild(new ParseTree(new TerminalSymbol(ascii)));
		root.addChild(fileName);
	}

	private static void binOp(ParseTree root, PushbackReader reader) throws IOException, ParseException {
		ParseTree binOp = new ParseTree(NonterminalMiniReSymbol.BIN_OP);

		if (ParserUtils.peekSequence(reader, ReservedWord.DIFF.getValue())) {
			ParserUtils.consumeSequence(reader, ReservedWord.DIFF.getValue());
			binOp.addChild(new ParseTree(ReservedWord.DIFF));
		} else if (ParserUtils.peekSequence(reader, ReservedWord.UNION.getValue())) {
			ParserUtils.consumeSequence(reader, ReservedWord.UNION.getValue());
			binOp.addChild(new ParseTree(ReservedWord.UNION));
		} else if (ParserUtils.peekSequence(reader, ReservedWord.INTERS.getValue())) {
			ParserUtils.consumeSequence(reader, ReservedWord.INTERS.getValue());
			binOp.addChild(new ParseTree(ReservedWord.INTERS));
		} else {
			throw new ParseException("Bin-op: couldn't find diff, union, or inters");
		}
		root.addChild(binOp);
	}
	
	public static String peekId(PushbackReader reader) throws IOException {
		// needs a letter + up to 9 letters, digits, underscores.
		// let's do this the easy way:
		// first get any preceding whitespace out of the way:
		ParserUtils.peekAndConsumeWhitespace(reader);
		String nextTenChars = "";
		for (int i = 0; i < 10; i++) {
			int next = reader.read();
			if (next == -1) {
				break;
			} else if (Character.isWhitespace(next)) {
				reader.unread(next);
				break;
			}
			nextTenChars += (char) next;
		}
		String idRegex = "[a-zA-Z][a-zA-Z0-9_]*";
		// Now just try to match [a-zA-Z][a-zA-Z0-9_]*
		Matcher matcher = Pattern.compile(idRegex).matcher(nextTenChars);
		boolean found = matcher.find();
		
		Character nextChar = ParserUtils.peek(reader);
		
		String toReturn = null;
		// we need a match at the beginning of the string, and the next char can't be an id char
		if (found && matcher.start() == 0 && (nextChar == null || !nextChar.toString().matches(idRegex))) {
			// now make sure it's not a reserved word:
			if (!ReservedWord.reservedWordSet().contains(matcher.group())) {
				toReturn = matcher.group();
			}
		} 
		// regardless, we just want to peek, so put ten chars back into the reader
		for (int i = nextTenChars.length() - 1; i >= 0; i--) {
			reader.unread(nextTenChars.charAt(i));
		}
		return toReturn;
	}
	

	private static String consumeRegex(PushbackReader reader)
			throws IOException, ParseException {
		
		// we need a ", some ascii printables (including \"), and then "
		Character singleQuote = '\'';
		if (!ParserUtils.peekSequence(reader, singleQuote.toString())) {
			throw new ParseException("Couldn't parse regular expression: not enclosed by single quotes");
		}
		reader.read(); // consume '
		String regex = "";
		int next;
		while ((next = reader.read()) != -1) {
			if (!ParserUtils.isAsciiPrintable((char) next) || singleQuote.equals((char)next)){
				reader.unread(next);
				break;
			}
			regex += (char)	 next;
		}

		if (!ParserUtils.peekSequence(reader, singleQuote.toString())) {
			throw new ParseException("Couldn't parse regular expression: not enclosed by single quotes");
		} else {
			reader.read(); // get rid of that last single quote.
			//ascii += singleQuote;
			try {
				RegexParser.parse(regex, new HashMap<String, ParseTree>());
			} catch (ParseException e) {
				throw new ParseException("Couldn't parse regular expression: " + regex, e);
			}
//			System.out.println("Consumed " + regex);
			return regex;
		}
		
	}
	
	private static String peekAscii(PushbackReader reader) throws IOException {
		// we need a ", some ascii printables (including \"), and then "
		Character doubleQuote = '"';
		if (!ParserUtils.peekSequence(reader, doubleQuote.toString())) {
			return null; // doesn't start with "
		}
		String ascii = "";
		ascii += (char) reader.read();
		int next;
		boolean escapeReady = false;
		while ((next = reader.read()) != -1) {
			if (!ParserUtils.isAsciiPrintable((char) next) || (doubleQuote.equals((char) next) && !escapeReady)){
				reader.unread(next);
				break;
			}
			escapeReady = Character.valueOf('\\').equals(next);
			ascii += (char)	 next;
		}
		// unread all these guys
		for (int i = ascii.length() - 1; i >= 0; i--) {
			reader.unread(ascii.charAt(i));
		}
		if (!ParserUtils.peekSequence(reader, doubleQuote.toString())) {
			return null;
		} else {
			//ascii += doubleQuote;
			return ascii.substring(1);
		}
	}
	
	
}

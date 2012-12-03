package parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.Stack;

import exception.ParseException;

public class ParserUtils {

	public static Character peek(PushbackReader reader) throws IOException {
		int read = reader.read();
		if (read == -1) {
			return null;
		}
		char toReturn = (char) read;
		reader.unread(toReturn);
		return toReturn;
	}

	public static Character peekAndConsumeWhitespace(PushbackReader reader) throws IOException {
		Character c = peek(reader);
		while (c != null && Character.isWhitespace(c)) {
			reader.read();
			c = peek(reader);
		}
		return c;
	}
	
	public static void consumeSequence(PushbackReader reader, String charsToConsume) throws ParseException, IOException {
		// eat any whitespace
		peekAndConsumeWhitespace(reader);
		for (Character c : charsToConsume.toCharArray()) {
			Character nextInReader = peek(reader);
			if (!c.equals(nextInReader)) {
				throw new ParseException("Error matching string: couldn't find " + charsToConsume);
			}
			// otherwise, we matched it
			reader.read(); // so we'd better consume the character
		}
//		System.out.println("consumed: " + charsToConsume);
	}
	
	public static boolean peekSequence(PushbackReader reader, String charsToPeek) throws IOException {
		peekAndConsumeWhitespace(reader);
		Stack<Character> charStack = new Stack<Character>();
		boolean allMatch = true;
		for (Character c : charsToPeek.toCharArray()) {
			Character nextInReader = peek(reader);
			if (nextInReader == null) {
				allMatch = false;
				break;
			}
			charStack.push(nextInReader);
			reader.read(); // consume that peek
			if (!c.equals(nextInReader)) {
				allMatch = false;
				break;
			}
		}
		while (!charStack.isEmpty()) {
			reader.unread(charStack.pop());
		}
		return allMatch;
	}

	public static boolean isAsciiPrintable(char character) {
		return (int) character <= 126 && (int) character >= 32;
	}

}

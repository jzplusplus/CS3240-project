package test;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import base.NFAScanner;

import parser.Token;
import util.NFA;
import exception.BadTokenException;

public class NFAScannerTest {
    public static void main(String[] args) throws IOException, BadTokenException {
        NFA a = NFA.acceptCharacter('a');
        NFA b = NFA.acceptCharacter('b');
        NFA c = NFA.acceptCharacter('c');
        NFA abc = NFA.concatenate(NFA.concatenate(a, b), c);
        NFA abcPlus = NFA.kleenePlus(abc);
        NFA oneOrTwo = NFA.union(NFA.acceptCharacter('1'), NFA.acceptCharacter('2'));
        NFA abbAOrB = NFA.concatenate(NFA.concatenate(NFA.concatenate(a, b), b), NFA.union(a, b));

        String input = "abcabc abca abbb  abba     1 2";
        HashMap<String, NFA> tokenMap = new HashMap<String, NFA>();
        tokenMap.put("abca", NFA.concatenate(abc, NFA.acceptCharacter('a'))); 
        tokenMap.put("(abc)+", abcPlus); 
        tokenMap.put("(1|2)", oneOrTwo);
        tokenMap.put("abb(a|b)", abbAOrB);
        NFAScanner scanner =  new NFAScanner(tokenMap);
        List<Token> tokens = null;
        System.out.println("Trying to find tokens in: " + input);
        System.out.println("Using token regexes: " + tokenMap.keySet().toString());
        try  {
            tokens = scanner.tokenize(input);
        } catch (BadTokenException e) {
            System.out.println("got bad token: " + e.getPartialToken());
            return;
        }

        for (Token token : tokens) {
            System.out.println(token.getType() + "->" + token.getValue());
        }

        System.out.println("took " + scanner.getTransitionCount() + " transitions");
    }
}

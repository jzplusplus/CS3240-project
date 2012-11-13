import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import parser.Token;

import util.NFA;

public class NFAScannerTest {
    public static void main(String[] args) throws IOException, BadTokenException {
        NFA a = NFA.acceptCharacter('a');
        NFA b = NFA.acceptCharacter('b');
        NFA c = NFA.acceptCharacter('c');
        NFA abc = NFA.concatenate(NFA.concatenate(a, b), c);
        //System.out.println(abc);
        NFA abcPlus = NFA.kleenePlus(abc);
        NFA oneOrTwo = NFA.union(NFA.acceptCharacter('1'), NFA.acceptCharacter('2'));
        /*System.out.println("abc?" + NFAScanner.run(abc, "abc"));
        System.out.println("abc+?" + NFAScanner.run(abcPlus, "abcabcabc"));
        System.out.println("1|2?" + NFAScanner.run(oneOrTwo, "1"));
        */
        String input = "abcabc abca        1 2";
        HashMap<String, NFA> tokenMap = new HashMap<String, NFA>();
        tokenMap.put("abca", NFA.concatenate(abc, NFA.acceptCharacter('a'))); 
        tokenMap.put("abc+", abcPlus); 
        tokenMap.put("1|2", oneOrTwo);
        NFAScanner scanner =  new NFAScanner(tokenMap);
        List<Token> tokens = null;
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

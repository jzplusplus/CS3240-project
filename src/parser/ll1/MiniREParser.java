package parser.ll1;

import java.io.FileNotFoundException;
import java.io.IOException;

import exception.IncorrectRuleFormatException;
import exception.MultipleStartSymbolException;
import exception.UndefinedNonterminalException;

public class MiniREParser {
	
	public MiniREParser(String grammarSpec) throws FileNotFoundException, IOException, MultipleStartSymbolException, IncorrectRuleFormatException, UndefinedNonterminalException {
		LL1ParserGenerator ll1 = new LL1ParserGenerator(grammarSpec);
	}

}

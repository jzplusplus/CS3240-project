package parser.ll1;

public class Pair {
	
	private Nonterminal nonterminal;
	private Terminal terminal;
	
	public Pair(Nonterminal nonterminal, Terminal terminal) {
		this.nonterminal = nonterminal;
		this.terminal = terminal;
	}
	
	public Nonterminal getNonterminal() { return nonterminal; }
	
	public Terminal getTerminal() { return terminal; }
	
	@Override
	public boolean equals(Object obj) { 
		Pair obj2pair = (Pair) obj;
		return (obj2pair.getTerminal().getValue().equals(terminal.getValue()) && obj2pair.getNonterminal().getValue().equals(nonterminal.getValue()));
	}
}

package filtering;

import java.io.IOException;

public class Sandbox {
	public static final void main(String[] args) throws IOException {
		Grammar g = new Grammar("grammars/small_full_grammar.txt", true, true);
		g.filter("c b", true);
	}
}

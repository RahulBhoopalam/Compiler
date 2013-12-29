package cop5555fa13;


import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String msg) {
			super(msg);
			this.t = t;
		}

		public String toString() {
			return super.toString() + "\n" + t.toString();
		}
		
		public Kind getKind(){
			return t.kind;
		}
	}

	TokenStream stream;
	/* You will need additional fields */

    /** creates a simple parser.  
     * 
     * @param initialized_stream  a TokenStream that has already been initialized by the Scanner 
     */
	public SimpleParser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		/* You probably want to do more here */
	}

	/* This method parses the input from the given token stream.  If the input is correct according to the phrase
	 * structure of the language, it returns normally.  Otherwise it throws a SyntaxException containing
	 * the Token where the error was detected and an appropriate error message.  The contents of your
	 * error message will not be graded, but the "kind" of the token will be.
	 */
	public void parse() throws SyntaxException {
	    /* You definitely need to do more here */
	}
	
	/* You will need to add more methods*/

	
	
	
	/* Java hint -- Methods with a variable number of parameters may be useful.  
	 * For example, this method takes a token and variable number of "kinds", and indicates whether the
	 * kind of the given token is among them.  The Java compiler creates an array holding the given parameters.
	 */
	   private boolean isKind(Token t, Kind... kinds) {
		Kind k = t.kind;
		for (int i = 0; i != kinds.length; ++i) {
			if (k==kinds[i]) return true;
		}
		return false;
	  }

}

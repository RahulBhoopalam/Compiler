package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import cop5555fa13.SimpleParser.SyntaxException;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;



public class TestSimpleParser {

	/* Scans and parses the given program.  Use this method for tests that expect to discover an error during parsing.
	 * The second parameter is the expected kind of the erroneous token.  The test case itself should "expect SyntaxException.class"
	 */
	private void parseErrorInput (String program, Kind expectedErrorKind) throws LexicalException, SyntaxException {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		}
		catch(LexicalException e){
			System.out.println(e.toString());
			throw e;
		}
		Kind errorKind;
		try{
		SimpleParser p = new SimpleParser(stream);
		p.parse();
		}
		catch(SyntaxException e){
			System.out.println("Parsed with error: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			errorKind = e.getKind();
			assertEquals(expectedErrorKind, errorKind);
			throw e;			
		}
	}
	
	
	/* Scans and parses the given program.  Use this method for tests that expect to discover an error during scanning or 
	 * to successfully parse the input.  If an error during scanning is expected, the test case should "expect LexicalException.class".
	 */
	private void parseInput (String program) throws LexicalException, SyntaxException {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		}
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			throw e;
		}
		try{
		SimpleParser p = new SimpleParser(stream);
		p.parse();
		}
		catch(SyntaxException e){
			System.out.println(e.toString());
			throw e;			
		}
		System.out.println("Parsed without error: ");
		System.out.println(program);
		System.out.println("---------");
	}
	



/* Example testing an erroneous program. */
	@Test(expected=SyntaxException.class)
	public void emptyProg() throws LexicalException, SyntaxException  {
		String input = "";
		parseErrorInput(input,EOF);
	}
	
/* Here is an example testing a correct program. */
	@Test
	public void minimalProg() throws LexicalException, SyntaxException  {
		String input = "smallestProg{}";
		parseInput(input);
	}
	
/* Another correct program */
	@Test
	public void decs() throws LexicalException, SyntaxException  {
		String input = "decTest {\n  int a;\n  image b; boolean c; pixel p; \n}";
		parseInput(input);
	}
	
/* A program missing a ; after "int a".  The token where the error will be detected is "image"*/
	@Test(expected=SyntaxException.class)
	public void missingSemi() throws LexicalException, SyntaxException  {
		String input = "decTest {\n  int a\n  image b; boolean c; pixel p; \n}";
		parseErrorInput(input, image);		
	}
	
/* A program with a lexical error */
	@Test(expected=LexicalException.class)
	public void lexError() throws LexicalException, SyntaxException  {
		String input = "decTest {\n  int a@;\n  image b; boolean c; pixel p; \n}";
		parseInput(input);
	}
	
}

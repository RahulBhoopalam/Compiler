package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Test;

import cop5555fa13.SimpleParser.SyntaxException;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.ast.*;



public class TestParser {

	/* Scans and parses the program in the given file.  Use this method for tests that expect to discover an error during parsing.
	 * The second parameter is the expected kind of the erroneous token.  This test only handles syntax errors and
	 * will fail if there is a lexical error.
	 */
	private void parseErrorFromFile (String filename, Kind... expectedErrorKind)  {
		TokenStream stream= null;
		try {
			stream = new TokenStream(new BufferedReader(new FileReader(filename)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		Parser p = new Parser(stream);
		p.parse();	
		int numErrors = p.errorList.size();
        assertEquals(expectedErrorKind.length, numErrors);       
		}		
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(filename);
			System.out.println(e.toString());
			System.out.println("---------");
			fail();
		}
	}
	
	/* Scans and parses the program in the given string.  Use this method for tests that expect to discover an error during parsing.
	 * The second parameter is the expected kind of the erroneous token.  This test only handles syntax errors and
	 * will fail if there is a lexical error.
	 */
	private void parseErrorInput (String program, Kind... expectedErrorKind)  {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		Parser p = new Parser(stream);
		p.parse();	
		int numErrors = p.errorList.size();
        assertEquals(expectedErrorKind.length, numErrors);       
		}		
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			fail();
		}
	}
	/* Scans and parses the given string.  Use this method for input programs that should be correct.
	 * It is probably easiest to look at the output to check the AST.  (Our tests will compare with output
	 * from a reference implementation, which you won't have)
	 */
	private void parseCorrectInput (String program)  {
		TokenStream stream = new TokenStream(program);
		Scanner s = new Scanner(stream);
		Program prog = null;
		try{
		s.scan();
		Parser p = new Parser(stream);
		prog = p.parse();	
		assertTrue("expected p.getErrorList().isEmpty()", p.getErrorList().isEmpty());

			ToStringVisitor visitor= new ToStringVisitor();
			try {
				prog.visit(visitor, "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print(visitor.getString());	
		}
		
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(program);
			System.out.println(e.toString());
			System.out.println("---------");
			fail();
		}
	}
	
	private void parseCorrectFromFile (String fileName)  {
		TokenStream stream= null;
		try {
			stream = new TokenStream(new BufferedReader(new FileReader(fileName)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Scanner s = new Scanner(stream);
		Program prog = null;
		try{
		s.scan();
		Parser p = new Parser(stream);
		prog = p.parse();	
		assertTrue("expected p.getErrorList().isEmpty()", p.getErrorList().isEmpty());

			ToStringVisitor visitor= new ToStringVisitor();
			try {
				prog.visit(visitor, "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.print(visitor.getString());	
		}		
		catch(LexicalException e){
			System.out.println("Lexical error parsing program: ");
			System.out.println(fileName);
			System.out.println(e.toString());
			System.out.println("---------");
			fail();
		}
	}



	
/* Here is an example testing a correct program. */
	@Test
	public void minimalProg() throws LexicalException, SyntaxException  {
		String input = "smallestProg{}";
		parseCorrectInput(input);
	}
	
/* Another correct program */
	@Test
	public void decs() throws LexicalException, SyntaxException  {
		String input = "decTest {\n  int a;\n  image b; boolean c; pixel p; \n}";
		parseCorrectInput(input);
	}
	
/* A program missing a ; after "int a".  The token where the error will be detected is "image"*/
	@Test
	public void missingSemi() throws LexicalException, SyntaxException  {
		String input = "missingSemi {\n  int a\n  image b; boolean c; pixel p; \n}";
		parseErrorInput(input, image);		
	}

/* A program with 2 mistakes.  Both should be reported. */
	@Test
	public void missingSemisInDecs() {
		String input = "missingSemisInDecs {\n  int a\n  image b;\n boolean c\n pixel p; \n}";
		parseErrorInput(input, image, pixel);		
	}
	

/* This reads the input program from a file, which was provided.  If the file can't be found, it will throw a FileNotFoundException.
 * If you have this problem, a quick and dirty fix is to change the file name below to the complete absolute path.
 */
	@Test
	public void allStatements(){
		parseCorrectFromFile("allstatements.plp");       
	}
	
	@Test
	public void allStatementsErrors(){
		parseErrorFromFile("C:allstatementsErrors.plp",RBRACE,LSQUARE,SEMI);       
	}

	
}

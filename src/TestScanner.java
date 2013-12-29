package cop5555fa13.tests;

import static cop5555fa13.TokenStream.Kind.EOF;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import cop5555fa13.Scanner;
import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class TestScanner {

	/* creates a scanner to tokenize the input string and compares the results with the expected string
	 * You probably will not need to modify this method.
	 */
	private void compareText (String input, String expected) throws LexicalException {
		TokenStream stream = new TokenStream(input);
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		}
		catch(LexicalException e){
			System.out.println(e.toString());
			throw e;
		}
		String output = stream.tokenTextListToString();
		System.out.println(output);
		assertEquals(expected,output);	
	}
	

	
    /* You can use this test case pattern to check the output of input without errors.
     * Give the input as a string and the expected output as a string with containing the
     * text of each token terminated with a comma.  (This means you need a comma after the last one)
     * 
     * Be aware that escape characters in your strings will be handled by java before the string is 
     * given to your program.  In other words, if your expected input String is given in your test case code
     * as "abc\\def", what you Scanner will actually see is abc\def.  
     * If you read abc\\def from a file,  your Scanner will see abc\\def.
     * This is probably the most annoying for string literals.  To create test input String containing a 
     * string literal, you need to escape the quotes that should be passed to the scanner.
     * 
     * For example, if your actual test input (as it would appear in source code) is 
     * 
     *     print_string("this is a string literal")
     *     
     * your input string would need to be
     *     "print_string(\"this is a string literal\")"
     */
	@Test
	public void testScan0() throws LexicalException  {
		String input = "this is a test test ";
		String expected = "this,is,a,test,test,";  //comma separated (and terminated)
		                                           //text of tokens in input
		compareText(input,expected);
	}
	
	
	/* Use this test case pattern to test input with known errors where an exception should be thrown.  
	 * This tells Junit that the test only passes if it throws the expected exception.  
	 * 
	 * Recent versions of junit have more sophisticated features for error checking, but this 
	 * simple way should be sufficient for our purposes.
	 */
	@Test(expected=LexicalException.class)
	public void testIllegalChar() throws LexicalException {
		String input = "this is # an test \nwith an illegal char";
		String expected = "dummy";
		compareText(input,expected);
	}
	
	
	@Test
	public void testScan1() throws LexicalException  {
		String input = "this is a test test";
		String expected = "this,is,a,test,test,";
		compareText(input,expected);
	}
	
	@Test
	public void testScan2() throws LexicalException  {
		String input = "this is a \ntest \ntest";
		String expected = "this,is,a,test,test,";
		compareText(input,expected);
	}
	
	@Test
	public void testScan3() throws LexicalException {
		String input = "123+456-abc*,()[] X Y x y Z if else+";
		String expected = "123,+,456,-,abc,*,,,(,),[,],X,Y,x,y,Z,if,else,+,";
		compareText(input,expected);
	}
	
}

package cop5555fa13;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.print.DocFlavor.STRING;
import javax.swing.text.StyledEditorKit.StyledTextAction;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;
import static cop5555fa13.TokenStream.Kind.*;

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

		public Kind getKind() {
			return t.kind;
		}
	}

	TokenStream stream;

	/* You will need additional fields */
	int scannedTillThisIndex;
	Token currentToken;

	/**
	 * creates a simple parser.
	 * 
	 * @param initialized_stream
	 *            a TokenStream that has already been initialized by the Scanner
	 */
	public SimpleParser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		/* You probably want to do more here */
		scannedTillThisIndex = 0;
		currentToken = stream.getToken(scannedTillThisIndex);
	}

	/*
	 * This method parses the input from the given token stream. If the input is
	 * correct according to the phrase structure of the language, it returns
	 * normally. Otherwise it throws a SyntaxException containing the Token
	 * where the error was detected and an appropriate error message. The
	 * contents of your error message will not be graded, but the "kind" of the
	 * token will be.
	 */
	public void parse() throws SyntaxException {
		/* You definitely need to do more here */
		program();
		if (isKind(currentToken, EOF)) {
			// System.out.println("The input had " + stream.tokens.size() +
			// " tokens.");
			// System.out.println("The parser has parsed till the index : " +
			// scannedTillThisIndex + ". [Note: Index begins from zero]");
			return;
		} else
			throw new SyntaxException(currentToken, "An " + EOF
					+ " was expected here.");
	}

	/* You will need to add more methods */

	/*
	 * Java hint -- Methods with a variable number of parameters may be useful.
	 * For example, this method takes a token and variable number of "kinds",
	 * and indicates whether the kind of the given token is among them. The Java
	 * compiler creates an array holding the given parameters.
	 */
	private boolean isKind(Token t, Kind... kinds) {
		Kind k = t.kind;
		for (int i = 0; i != kinds.length; ++i) {
			if (k == kinds[i])
				return true;
		}
		return false;
	}

	private void consume() {
		currentToken = stream.getToken(++scannedTillThisIndex);
	}

	private void match(Kind... kinds) throws SyntaxException {
		if (isKind(currentToken, kinds)) {
			consume();
			return;
		}
		// This token did not match any kind in 'kinds'
		if (kinds.length > 1) {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(kinds) + " was expected here.");
		} else
			throw new SyntaxException(currentToken, "" + kinds[0]
					+ " was expected here.");
	}

	private void program() throws SyntaxException {
		match(IDENT);
		match(LBRACE);

		// dec* . So check for FIRST(dec) = {image, pixel, int, boolean}
		while (isKind(currentToken, image, pixel, _int, _boolean)) {
			dec();
		}

		// Stmt*. So check for FIRST(Stmt) = { ;, IDENT, pause, _while, _if }
		while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
			stmt();
		}

		match(RBRACE);
	}

	private void dec() throws SyntaxException {
		type();
		match(IDENT);
		match(SEMI);
	}

	private void type() throws SyntaxException {
		match(image, pixel, _int, _boolean);
	}

	private void stmt() throws SyntaxException {
		// AssignStmt. So check for FIRST(AssignStmt) = {IDENT}
		if (isKind(currentToken, IDENT)) {
			assignStmt();
		}
		// PauseStmt. So check for FIRST(PauseStmt) = {pause}
		else if (isKind(currentToken, pause)) {
			pauseStmt();
		}
		// IterationStmt. So check for FIRST(IterationStmt) = {while}
		else if (isKind(currentToken, _while)) {
			iterationStmt();
		}
		// AlternativeStmt. So check for FIRST(AlternativeStmt) = {if}
		else if (isKind(currentToken, _if)) {
			alternativeStmt();
		} else if (isKind(currentToken, SEMI)) {
			consume();
		} else {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(SEMI, IDENT, pause, _while, _if)
					+ " was expected here.");
		}
	}

	private void assignStmt() throws SyntaxException {
		if (isKind(currentToken, IDENT)) {
			consume();
			if (isKind(currentToken, ASSIGN)) {
				consume();
				if (isKind(currentToken, STRING_LIT))
					consume();
				// expr. So check for FIRST(expr)
				// = { IDENT, INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE, ( }
				else if (isKind(currentToken, IDENT, INT_LIT, BOOLEAN_LIT, x,
						y, Z, SCREEN_SIZE, LPAREN)) {
					expr();
				}
				// pixel. So check for FIRST(pixel) = { { }
				else if (isKind(currentToken, LBRACE)) {
					pixel();
				}
				match(SEMI);
			} else if (isKind(currentToken, DOT)) {
				consume();
				if (isKind(currentToken, pixels)) {
					consume();
					match(LSQUARE);
					expr();
					match(COMMA);
					expr();
					match(RSQUARE);
					if (isKind(currentToken, ASSIGN)) {
						consume();
						pixel();
					} else if (isKind(currentToken, red, green, blue)) {
						consume();
						match(ASSIGN);
						expr();
					} else {
						throw new SyntaxException(currentToken,
								"One of these tokens: "
										+ Arrays.asList(ASSIGN, red, green,
												blue) + " was expected.");
					}
				} else if (isKind(currentToken, shape, location)) {
					consume();
					match(ASSIGN);
					match(LSQUARE);
					expr();
					match(COMMA);
					expr();
					match(RSQUARE);
				} else if (isKind(currentToken, visible)) {
					consume();
					match(ASSIGN);
					expr();
				}
				match(SEMI);
			} else {
				throw new SyntaxException(currentToken, "Either of: "
						+ Arrays.asList(ASSIGN, DOT) + " was expected.");
			}
		} else {
			throw new SyntaxException(currentToken, "" + IDENT
					+ " was expected here.");
		}
	}

	private void pixel() throws SyntaxException {
		if (isKind(currentToken, LBRACE)) {
			consume();
			match(LBRACE);
			expr();
			match(COMMA);
			expr();
			match(COMMA);
			expr();
			match(RBRACE);
			match(RBRACE);
		} else {
			throw new SyntaxException(currentToken, "" + LBRACE
					+ " was expected here.");
		}
	}

	private void expr() throws SyntaxException {
		orExpr();

		if (isKind(currentToken, QUESTION)) {
			consume();
			expr();
			match(COLON);
			expr();
		} else {
			// Do Nothing -> consume epsilon
		}
	}

	private void orExpr() throws SyntaxException {
		andExpr();

		while (isKind(currentToken, OR)) {
			consume();
			andExpr();
		}
	}

	private void andExpr() throws SyntaxException {
		equalityExpr();

		while (isKind(currentToken, AND)) {
			consume();
			equalityExpr();
		}
	}

	private void equalityExpr() throws SyntaxException {
		relExpr();

		while (isKind(currentToken, EQ, NEQ)) {
			consume();
			relExpr();
		}
	}

	private void relExpr() throws SyntaxException {
		shiftExpr();

		while (isKind(currentToken, LT, GT, LEQ, GEQ)) {
			consume();
			shiftExpr();
		}
	}

	private void shiftExpr() throws SyntaxException {
		addExpr();

		while (isKind(currentToken, LSHIFT, RSHIFT)) {
			consume();
			addExpr();
		}
	}

	private void addExpr() throws SyntaxException {
		multExpr();

		while (isKind(currentToken, PLUS, MINUS)) {
			consume();
			multExpr();
		}
	}

	private void multExpr() throws SyntaxException {
		primaryExpr();

		while (isKind(currentToken, TIMES, DIV, MOD)) {
			consume();
			primaryExpr();
		}
	}

	private void primaryExpr() throws SyntaxException {
		if (isKind(currentToken, INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE))
			consume();
		else if (isKind(currentToken, LPAREN)) {
			consume();
			expr();
			match(RPAREN);
		} else if (isKind(currentToken, IDENT)) {
			consume();
			if (isKind(currentToken, LSQUARE)) {
				consume();
				expr();
				match(COMMA);
				expr();
				match(RSQUARE);
				match(red, green, blue);
			} else if (isKind(currentToken, DOT)) {
				consume();
				match(height, width, x_loc, y_loc);
			}
		} else {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE,
							LPAREN, IDENT, DOT) + " was expected here.");
		}
	}

	private void pauseStmt() throws SyntaxException {
		if (isKind(currentToken, pause)) {
			consume();
			expr();
			match(SEMI);
		} else
			throw new SyntaxException(currentToken, "" + pause
					+ " was expected here.");
	}

	private void iterationStmt() throws SyntaxException {
		if (isKind(currentToken, _while)) {
			consume();
			match(LPAREN);
			expr();
			match(RPAREN);

			match(LBRACE);
			// Stmt*. So check for FIRST(Stmt) = { ;, IDENT, pause, _while, _if
			// }
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				stmt();
			}
			match(RBRACE);
		}
	}

	private void alternativeStmt() throws SyntaxException {
		if (isKind(currentToken, _if)) {
			consume();
			match(LPAREN);
			expr();
			match(RPAREN);

			match(LBRACE);
			// Stmt*. So check for FIRST(Stmt) = {;, IDENT, pause, _while, _if}
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				stmt();
			}
			match(RBRACE);
		} else
			throw new SyntaxException(currentToken, "" + _if
					+ " was expected here.");

		if (isKind(currentToken, _else)) {
			consume();
			match(LBRACE);
			// Stmt*. So check for FIRST(Stmt) = {;, IDENT, pause, _while, _if}
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				stmt();
			}
			match(RBRACE);
		} else {
			// Do nothing - Accept epsilon
		}
	}

}

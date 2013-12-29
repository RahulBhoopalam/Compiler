package cop5555fa13;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.print.DocFlavor.STRING;
import javax.swing.text.StyledEditorKit.StyledTextAction;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.ast.AlternativeStmt;
import cop5555fa13.ast.AssignExprStmt;
import cop5555fa13.ast.AssignPixelStmt;
import cop5555fa13.ast.BinaryExpr;
import cop5555fa13.ast.BooleanLitExpr;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.ast.Dec;
import cop5555fa13.ast.Expr;
import cop5555fa13.ast.FileAssignStmt;
import cop5555fa13.ast.IdentExpr;
import cop5555fa13.ast.ImageAttributeExpr;
import cop5555fa13.ast.IntLitExpr;
import cop5555fa13.ast.IterationStmt;
import cop5555fa13.ast.PauseStmt;
import cop5555fa13.ast.Pixel;
import cop5555fa13.ast.PreDefExpr;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.SampleExpr;
import cop5555fa13.ast.ScreenLocationAssignmentStmt;
import cop5555fa13.ast.SetVisibleAssignmentStmt;
import cop5555fa13.ast.ShapeAssignmentStmt;
import cop5555fa13.ast.SinglePixelAssignmentStmt;
import cop5555fa13.ast.SingleSampleAssignmentStmt;
import cop5555fa13.ast.Stmt;
import static cop5555fa13.TokenStream.Kind.*;

public class Parser {

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
	Token progName; // keep the program name in case you don't generate
					// an AST
	List<SyntaxException> errorList; // save the error for grading
										// purposes

	/* You will need additional fields */
	int scannedTillThisIndex;
	Token currentToken;

	/**
	 * creates a simple parser.
	 * 
	 * @param initialized_stream
	 *            a TokenStream that has already been initialized by the Scanner
	 */
	public Parser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		/* You probably want to do more here */
		scannedTillThisIndex = 0;
		currentToken = stream.getToken(scannedTillThisIndex);
		errorList = new ArrayList<SyntaxException>();
	}

	public List<SyntaxException> getErrorList() {
		return errorList;
	}

	public String getProgName() {
		return (progName != null ? progName.getText() : "no program name");
	}

	/*
	 * This method parses the input from the given token stream. If the input is
	 * correct according to the phrase structure of the language, it returns
	 * normally. Otherwise it throws a SyntaxException containing the Token
	 * where the error was detected and an appropriate error message. The
	 * contents of your error message will not be graded, but the "kind" of the
	 * token will be.
	 */
	public Program parse() {
		/* You definitely need to do more here */
		Program p = null;
		try {
			p = program();
			if (!isKind(currentToken, EOF)) {
				throw new SyntaxException(currentToken, "An " + EOF
						+ " was expected here.");
			}
		} catch (SyntaxException e) {
			errorList.add(e);
		}
		// if (errorList.isEmpty()) {
		// return p;
		// } else
		// return null;
		return p;
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

	// Matches the 'kind' of currentToken with the 'kinds' passed as arg
	// Returns ON SUCCESS -> the 'matched' token if matched
	// ON FAILURE -> throws a SyntaxException
	private Token match(Kind... kinds) throws SyntaxException {
		if (isKind(currentToken, kinds)) {
			Token toBeReturned = currentToken;
			consume();
			return toBeReturned;
		}
		// This token did not match any kind in 'kinds'
		if (kinds.length > 1) {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(kinds) + " was expected here.");
		} else
			throw new SyntaxException(currentToken, "" + kinds[0]
					+ " was expected here.");
	}

	// Program ::= IDENT { Dec* Stmt* }
	private Program program() throws SyntaxException {
		Program program = null;
		progName = match(IDENT);
		match(LBRACE);

		// dec* . So check for FIRST(dec) = {image, pixel, int, boolean}
		List<Dec> decList = new ArrayList<Dec>();
		while (isKind(currentToken, image, pixel, _int, _boolean)) {
			try {
				decList.add(dec());
			} catch (SyntaxException e) {
				errorList.add(e);
				// skip tokens until next SEMI,
				// consume it, then continue parsing
				while (!isKind(currentToken, SEMI, image, _int, _boolean, pixel, EOF)) {
					consume();
				}
				if (isKind(currentToken, SEMI)) {
					consume();
				} // if a SEMI, consume it before continuing
			}
		}

		// Stmt*. So check for FIRST(Stmt) = { ;, IDENT, pause, _while, _if }
		List<Stmt> stmtList = new ArrayList<Stmt>();
		while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
			try {
				Stmt stmt = stmt();
				// If there are dangling semi-colons like ;;;;; then stmt()
				// would return null. Don't add such statements to the list.
				if (stmt != null)
					stmtList.add(stmt);
			} catch (SyntaxException e) {
				errorList.add(e);
				// skip tokens until next SEMI,
				// consume it, then continue parsing
				while (!isKind(currentToken, SEMI, /*IDENT,*/ pause, _while, _if,
						EOF)) {
					consume();
				}
				if (isKind(currentToken, SEMI)) {
					consume();
				} // if a SEMI, consume it before continuing
			}
		}

		match(RBRACE);

		// Parsing has finished.
		// if there were no errors, create and return a Program node, which is
		// the AST of the program
		if (errorList.isEmpty()) {
			program = new Program(progName, decList, stmtList);
		} else {
			// There were some errors. So print them and return null
			System.out.println("Error" + (errorList.size() > 1 ? "s" : "")
					+ " parsing the program " + "'" + getProgName() + "'.");
			for (SyntaxException e : errorList) {
				// System.out.println(e.getMessage() + " at line "
				// + e.t.getLineNumber());
				System.out.println("Line " + e.t.getLineNumber() + ": "
						+ e.getMessage());
			}
		}
		return program;
	}

	private Dec dec() throws SyntaxException {
		Dec toBeReturned = null;
		Kind type = type();
		if (isKind(currentToken, IDENT)) {
			Token ident = currentToken;
		}
		Token ident = match(IDENT);
		match(SEMI);
		toBeReturned = new Dec(type, ident);
		return toBeReturned;
	}

	private Kind type() throws SyntaxException {
		return match(image, pixel, _int, _boolean).kind;
	}

	private Stmt stmt() throws SyntaxException {
		Stmt toBeReturned = null;
		// AssignStmt. So check for FIRST(AssignStmt) = {IDENT}
		if (isKind(currentToken, IDENT)) {
			toBeReturned = assignStmt();
		}
		// PauseStmt. So check for FIRST(PauseStmt) = {pause}
		else if (isKind(currentToken, pause)) {
			toBeReturned = pauseStmt();
		}
		// IterationStmt. So check for FIRST(IterationStmt) = {while}
		else if (isKind(currentToken, _while)) {
			toBeReturned = iterationStmt();
		}
		// AlternativeStmt. So check for FIRST(AlternativeStmt) = {if}
		else if (isKind(currentToken, _if)) {
			toBeReturned = alternativeStmt();
		} else if (isKind(currentToken, SEMI)) {
			consume();
		} else {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(SEMI, IDENT, pause, _while, _if)
					+ " was expected here.");
		}
		return toBeReturned;
	}

	// AssignStmt ::= IDENT ( = ( Expr | Pixel | STRING_LIT ) )
	// | ( . ( pixels [ Expr , Expr ] ( = Pixel ) |
	// ( (red | green | blue ) = Expr ) )
	// | ( ( shape | location ) = [ Expr , Expr ] )
	// | ( visible = Expr )) ;
	private Stmt assignStmt() throws SyntaxException {
		Stmt toBeReturned = null;
		if (isKind(currentToken, IDENT)) {
			Token lhsIdent = currentToken;
			consume();
			if (isKind(currentToken, ASSIGN)) {
				consume();
				if (isKind(currentToken, STRING_LIT)) {
					Token fileName = currentToken;
					consume();
					toBeReturned = new FileAssignStmt(lhsIdent, fileName);
				}
				// expr. So check for FIRST(expr)
				// = { IDENT, INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE, ( }
				else if (isKind(currentToken, IDENT, INT_LIT, BOOLEAN_LIT, x,
						y, Z, SCREEN_SIZE, LPAREN)) {
					Expr expr = expr();
					toBeReturned = new AssignExprStmt(lhsIdent, expr);
				}
				// pixel. So check for FIRST(pixel) = { { }
				else if (isKind(currentToken, LBRACE)) {
					Pixel pixel = pixel();
					toBeReturned = new AssignPixelStmt(lhsIdent, pixel);
				}
				match(SEMI);
			} else if (isKind(currentToken, DOT)) {
				consume();
				if (isKind(currentToken, pixels)) {
					consume();
					match(LSQUARE);
					Expr xExpr = expr();
					match(COMMA);
					Expr yExpr = expr();
					match(RSQUARE);
					if (isKind(currentToken, ASSIGN)) {
						consume();
						Pixel pixel = pixel();
						toBeReturned = new SinglePixelAssignmentStmt(lhsIdent,
								xExpr, yExpr, pixel);
					} else if (isKind(currentToken, red, green, blue)) {
						Token color = currentToken;
						consume();
						match(ASSIGN);
						Expr rhsExpr = expr();
						toBeReturned = new SingleSampleAssignmentStmt(lhsIdent,
								xExpr, yExpr, color, rhsExpr);
					} else {
						throw new SyntaxException(currentToken,
								"One of these tokens: "
										+ Arrays.asList(ASSIGN, red, green,
												blue) + " was expected.");
					}
				} else if (isKind(currentToken, shape, location)) {
					boolean isShapeAssignment = isKind(currentToken, shape);
					consume();
					match(ASSIGN);
					match(LSQUARE);
					Expr e0 = expr();
					match(COMMA);
					Expr e1 = expr();
					match(RSQUARE);
					if (isShapeAssignment) {
						toBeReturned = new ShapeAssignmentStmt(lhsIdent, e0, e1);
					} else {
						toBeReturned = new ScreenLocationAssignmentStmt(
								lhsIdent, e0, e1);
					}
				} else if (isKind(currentToken, visible)) {
					consume();
					match(ASSIGN);
					Expr expr = expr();
					toBeReturned = new SetVisibleAssignmentStmt(lhsIdent, expr);
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
		return toBeReturned;
	}

	private Pixel pixel() throws SyntaxException {
		if (isKind(currentToken, LBRACE)) {
			consume();
			match(LBRACE);
			Expr redExpr = expr();
			match(COMMA);
			Expr greenExpr = expr();
			match(COMMA);
			Expr blueExpr = expr();
			match(RBRACE);
			match(RBRACE);
			return new Pixel(redExpr, greenExpr, blueExpr);
		} else {
			throw new SyntaxException(currentToken, "" + LBRACE
					+ " was expected here.");
		}
	}

	private Expr expr() throws SyntaxException {
		Expr trueValue = null;
		Expr falseValue = null;
		Expr condition = orExpr();
		if (isKind(currentToken, QUESTION)) {
			consume();
			trueValue = expr();
			match(COLON);
			falseValue = expr();
		} else {
			// Do Nothing -> consume epsilon
			return condition;
		}
		return new ConditionalExpr(condition, trueValue, falseValue);
	}

	private Expr orExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = andExpr();

		while (isKind(currentToken, OR)) {
			Token op = currentToken;
			consume();
			e1 = andExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr andExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = equalityExpr();

		while (isKind(currentToken, AND)) {
			Token op = currentToken;
			consume();
			e1 = equalityExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr equalityExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = relExpr();

		while (isKind(currentToken, EQ, NEQ)) {
			Token op = currentToken;
			consume();
			e1 = relExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr relExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = shiftExpr();

		while (isKind(currentToken, LT, GT, LEQ, GEQ)) {
			Token op = currentToken;
			consume();
			e1 = shiftExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr shiftExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = addExpr();

		while (isKind(currentToken, LSHIFT, RSHIFT)) {
			Token op = currentToken;
			consume();
			e1 = addExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr addExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = multExpr();

		while (isKind(currentToken, PLUS, MINUS)) {
			Token op = currentToken;
			consume();
			e1 = multExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	private Expr multExpr() throws SyntaxException {
		Expr e0 = null;
		Expr e1 = null;
		e0 = primaryExpr();

		while (isKind(currentToken, TIMES, DIV, MOD)) {
			Token op = currentToken;
			consume();
			e1 = primaryExpr();
			e0 = new BinaryExpr(e0, op, e1);
		}
		return e0;
	}

	// PrimaryExpr ::= INT_LIT | BOOLEAN_LIT | x | y | Z | SCREEN_SIZE
	// | ( Expr ) | IDENT ( epsilon |
	// ( [ Expr , Expr ] (red | green | blue ) )
	// | ( . ( height | width | x_loc | y_loc ) ) )
	private Expr primaryExpr() throws SyntaxException {
		Expr e = null; // The Expr object that is to be returned

		if (isKind(currentToken, INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE)) {
			switch (currentToken.kind) {
			case INT_LIT:
				e = new IntLitExpr(currentToken);
				break;
			case BOOLEAN_LIT:
				e = new BooleanLitExpr(currentToken);
				break;
			case x:
			case y:
			case Z:
			case SCREEN_SIZE:
				e = new PreDefExpr(currentToken);
				break;
			}
			consume();
		} else if (isKind(currentToken, LPAREN)) {
			consume();
			e = expr();
			match(RPAREN);
		} else if (isKind(currentToken, IDENT)) {
			Token ident = currentToken;
			consume();
			if (isKind(currentToken, LSQUARE)) {
				consume();
				Expr xLoc = expr();
				match(COMMA);
				Expr yLoc = expr();
				match(RSQUARE);

				Token color = null;
				switch (currentToken.kind) {
				case red:
				case green:
				case blue:
					color = currentToken;
					break;
				}
				match(red, green, blue);
				e = new SampleExpr(ident, xLoc, yLoc, color);
			} else if (isKind(currentToken, DOT)) {
				consume();
				Token selector = null;
				switch (currentToken.kind) {
				case height:
				case width:
				case x_loc:
				case y_loc:
					selector = currentToken;
					break;
				}
				match(height, width, x_loc, y_loc);
				e = new ImageAttributeExpr(ident, selector);
			} else {
				// Do Nothing -> consume epsilon
				e = new IdentExpr(ident);
			}
		} else {
			throw new SyntaxException(currentToken, "Either of "
					+ Arrays.asList(INT_LIT, BOOLEAN_LIT, x, y, Z, SCREEN_SIZE,
							LPAREN, IDENT, DOT) + " was expected here.");
		}
		return e;
	}

	private Stmt pauseStmt() throws SyntaxException {
		Stmt toBeReturned = null;
		if (isKind(currentToken, pause)) {
			consume();
			Expr expr = expr();
			match(SEMI);
			toBeReturned = new PauseStmt(expr);
		} else {
			throw new SyntaxException(currentToken, "" + pause
					+ " was expected here.");
		}
		return toBeReturned;
	}

	private Stmt iterationStmt() throws SyntaxException {
		Stmt toBeReturned = null;
		if (isKind(currentToken, _while)) {
			consume();
			match(LPAREN);
			Expr expr = expr();
			match(RPAREN);

			match(LBRACE);
			ArrayList<Stmt> stmtList = new ArrayList<Stmt>();
			// Stmt*. So check for
			// FIRST(Stmt) = { ;, IDENT, pause, _while, _if }
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				try {
					Stmt stmt = stmt();
					// If there are dangling semi-colons like ;;;;; then stmt()
					// would return null. Don't add such statements to the list.
					if (stmt != null)
						stmtList.add(stmt);
				} catch (SyntaxException e) {
					errorList.add(e);
					// skip tokens until next SEMI,
					// consume it, then continue parsing
					while (!isKind(currentToken, SEMI, /*IDENT,*/ pause, _while,
							_if, EOF)) {
						consume();
					}
					if (isKind(currentToken, SEMI)) {
						consume();
					} // if a SEMI, consume it before continuing
				}
			}
			match(RBRACE);
			toBeReturned = new IterationStmt(expr, stmtList);
		}
		return toBeReturned;
	}

	// AlternativeStmt ::== if ( Expr ) { Stmt* } ( epsilon | else { Stmt* } )
	private Stmt alternativeStmt() throws SyntaxException {
		Stmt toBeReturned = null;
		Expr expr = null;
		List<Stmt> ifStmtList = new ArrayList<Stmt>();
		List<Stmt> elseStmtList = new ArrayList<Stmt>();

		if (isKind(currentToken, _if)) {
			consume();
			match(LPAREN);
			expr = expr();
			match(RPAREN);

			match(LBRACE);
			// Stmt*. So check for FIRST(Stmt) = {;, IDENT, pause, _while, _if}
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				try {
					Stmt stmt = stmt();
					// If there are dangling semi-colons like ;;;;; then stmt()
					// would return null. Don't add such statements to the list.
					if (stmt != null)
						ifStmtList.add(stmt);
				} catch (SyntaxException e) {
					errorList.add(e);
					// skip tokens until next SEMI,
					// consume it, then continue parsing
					while (!isKind(currentToken, SEMI, /*IDENT,*/ pause, _while,
							_if, EOF)) {
						consume();
					}
					if (isKind(currentToken, SEMI)) {
						consume();
					} // if a SEMI, consume it before continuing
				}
			}
			match(RBRACE);
		} else {
			throw new SyntaxException(currentToken, "" + _if
					+ " was expected here.");
		}

		if (isKind(currentToken, _else)) {
			consume();
			match(LBRACE);
			// Stmt*. So check for FIRST(Stmt) = {;, IDENT, pause, _while, _if}
			while (isKind(currentToken, SEMI, IDENT, pause, _while, _if)) {
				try {
					Stmt stmt = stmt();
					// If there are dangling semi-colons like ;;;;; then stmt()
					// would return null. Don't add such statements to the list.
					if (stmt != null)
						elseStmtList.add(stmt);
				} catch (SyntaxException e) {
					errorList.add(e);
					// skip tokens until next SEMI,
					// consume it, then continue parsing
					while (!isKind(currentToken, SEMI, /*IDENT,*/ pause, _while,
							_if, EOF)) {
						consume();
					}
					if (isKind(currentToken, SEMI)) {
						consume();
					} // if a SEMI, consume it before continuing
				}
			}
			match(RBRACE);
		} //else {
			// Do nothing - Accept epsilon
//		}
		toBeReturned = new AlternativeStmt(expr, ifStmtList, elseStmtList);
		return toBeReturned;
	}

}

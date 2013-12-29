package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.AND;
import static cop5555fa13.TokenStream.Kind.ASSIGN;
import static cop5555fa13.TokenStream.Kind.BOOLEAN_LIT;
import static cop5555fa13.TokenStream.Kind.COLON;
import static cop5555fa13.TokenStream.Kind.COMMA;
import static cop5555fa13.TokenStream.Kind.COMMENT;
import static cop5555fa13.TokenStream.Kind.DIV;
import static cop5555fa13.TokenStream.Kind.DOT;
import static cop5555fa13.TokenStream.Kind.EOF;
import static cop5555fa13.TokenStream.Kind.EQ;
import static cop5555fa13.TokenStream.Kind.GEQ;
import static cop5555fa13.TokenStream.Kind.GT;
import static cop5555fa13.TokenStream.Kind.IDENT;
import static cop5555fa13.TokenStream.Kind.INT_LIT;
import static cop5555fa13.TokenStream.Kind.LEQ;
import static cop5555fa13.TokenStream.Kind.LPAREN;
import static cop5555fa13.TokenStream.Kind.LSHIFT;
import static cop5555fa13.TokenStream.Kind.LSQUARE;
import static cop5555fa13.TokenStream.Kind.LT;
import static cop5555fa13.TokenStream.Kind.MINUS;
import static cop5555fa13.TokenStream.Kind.MOD;
import static cop5555fa13.TokenStream.Kind.NEQ;
import static cop5555fa13.TokenStream.Kind.NOT;
import static cop5555fa13.TokenStream.Kind.OR;
import static cop5555fa13.TokenStream.Kind.PLUS;
import static cop5555fa13.TokenStream.Kind.QUESTION;
import static cop5555fa13.TokenStream.Kind.RPAREN;
import static cop5555fa13.TokenStream.Kind.RSHIFT;
import static cop5555fa13.TokenStream.Kind.RSQUARE;
import static cop5555fa13.TokenStream.Kind.SEMI;
import static cop5555fa13.TokenStream.Kind.STRING_LIT;
import static cop5555fa13.TokenStream.Kind.TIMES;
import static cop5555fa13.TokenStream.Kind.Z;
import static cop5555fa13.TokenStream.Kind._else;
import static cop5555fa13.TokenStream.Kind._if;
import static cop5555fa13.TokenStream.Kind._int;
import static cop5555fa13.TokenStream.Kind.image;
import static cop5555fa13.TokenStream.Kind.red;
import static cop5555fa13.TokenStream.Kind.x;
import static cop5555fa13.TokenStream.Kind.y;
import static cop5555fa13.TokenStream.Kind.*;

import java.util.HashMap;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {
	// Stores the input stream.
	private final TokenStream stream;

	// Represents the number of characters already scanned.
	private int scannedTillThisIndex;

	// Represents the mapping between the string representation of keywords to
	// their KIND
	private HashMap<String, Kind> keywordsHashMap = new HashMap<String, Kind>();

	public Scanner(TokenStream stream) {
		this.stream = stream;
		scannedTillThisIndex = -1;
		initializeKeywordsHashMap();
	}

	private void initializeKeywordsHashMap() {
		keywordsHashMap.put("image", image);
		keywordsHashMap.put("int", _int);
		keywordsHashMap.put("boolean", _boolean);
		keywordsHashMap.put("pixel", pixel);
		keywordsHashMap.put("pixels", pixels);
		keywordsHashMap.put("red", red);
		keywordsHashMap.put("green", green);
		keywordsHashMap.put("blue", blue);
		keywordsHashMap.put("Z", Z);
		keywordsHashMap.put("shape", shape);
		keywordsHashMap.put("width", width);
		keywordsHashMap.put("height", height);
		keywordsHashMap.put("location", location);
		keywordsHashMap.put("x_loc", x_loc);
		keywordsHashMap.put("y_loc", y_loc);
		keywordsHashMap.put("SCREEN_SIZE", SCREEN_SIZE);
		keywordsHashMap.put("visible", visible);
		keywordsHashMap.put("x", x);
		keywordsHashMap.put("y", y);
		keywordsHashMap.put("pause", pause);
		keywordsHashMap.put("while", _while);
		keywordsHashMap.put("if", _if);
		keywordsHashMap.put("else", _else);
	}

	public void scan() throws LexicalException {
		Token t;
		do {
			t = next();
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else
				stream.tokens.add(t);
			// System.out.println(t);
		} while (!t.kind.equals(EOF));
	}

	private Token next() throws LexicalException {
		// The stream.inputChars has the input as a character array.
		// Scan the characters in this array one by one and return the
		// longest-string that is a valid token.

		Token toBeReturned = null;

		// Step 1: Increment the index to the next character
		scannedTillThisIndex++;

		// Step 2: Return EOF if we have reached the end of input.
		if (scannedTillThisIndex >= stream.inputChars.length) {
			toBeReturned = stream.new Token(EOF, scannedTillThisIndex,
					scannedTillThisIndex);
			return toBeReturned;
		}
		char ch = stream.inputChars[scannedTillThisIndex];

		// Step 3 : Ignore all whitespaces.
		if (Character.isWhitespace(ch)) {
			do {
				if (scannedTillThisIndex + 1 < stream.inputChars.length)
					ch = stream.inputChars[++scannedTillThisIndex];
				else {
					// This index has now reached the end of the input stream.
					// So return EOF.
					toBeReturned = stream.new Token(EOF, scannedTillThisIndex,
							scannedTillThisIndex);
					return toBeReturned;
				}
			} while (Character.isWhitespace(ch));
		}

		// Step 4: All whitespaces have now been ignored. Now, analyze what is
		// the 'kind' of this token
		switch (ch) {
		// a) Handle all single-character tokens which can be recognized
		// immediately.
		case '.':
			toBeReturned = stream.new Token(DOT, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case ';':
			toBeReturned = stream.new Token(SEMI, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case ',':
			toBeReturned = stream.new Token(COMMA, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '(':
			toBeReturned = stream.new Token(LPAREN, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case ')':
			toBeReturned = stream.new Token(RPAREN, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '[':
			toBeReturned = stream.new Token(LSQUARE, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case ']':
			toBeReturned = stream.new Token(RSQUARE, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '{':
			toBeReturned = stream.new Token(LBRACE, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '}':
			toBeReturned = stream.new Token(RBRACE, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case ':':
			toBeReturned = stream.new Token(COLON, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '?':
			toBeReturned = stream.new Token(QUESTION, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '|':
			toBeReturned = stream.new Token(OR, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '&':
			toBeReturned = stream.new Token(AND, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '+':
			toBeReturned = stream.new Token(PLUS, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '-':
			toBeReturned = stream.new Token(MINUS, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '*':
			toBeReturned = stream.new Token(TIMES, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '%':
			toBeReturned = stream.new Token(MOD, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;
		case '0':
			toBeReturned = stream.new Token(INT_LIT, scannedTillThisIndex,
					scannedTillThisIndex + 1);
			break;

		// b) Handle all other tokens which can't be recognized immediately.
		// i) Some operators:
		case '=':
			if (scannedTillThisIndex + 1 < stream.inputChars.length
					&& stream.inputChars[scannedTillThisIndex + 1] == '=') {
				toBeReturned = stream.new Token(EQ, scannedTillThisIndex,
						scannedTillThisIndex + 2);
				++scannedTillThisIndex;
			}
			// Either the EOF has been reached or the next character isn't '='
			else
				toBeReturned = stream.new Token(ASSIGN, scannedTillThisIndex,
						scannedTillThisIndex + 1);
			break;

		case '!':
			if (scannedTillThisIndex + 1 < stream.inputChars.length
					&& stream.inputChars[scannedTillThisIndex + 1] == '=') {
				toBeReturned = stream.new Token(NEQ, scannedTillThisIndex,
						scannedTillThisIndex + 2);
				++scannedTillThisIndex;
			}
			// Either the EOF has been reached or the next character isn't '='
			else
				toBeReturned = stream.new Token(NOT, scannedTillThisIndex,
						scannedTillThisIndex + 1);
			break;

		case '<':
			if (scannedTillThisIndex + 1 < stream.inputChars.length) {
				if (stream.inputChars[scannedTillThisIndex + 1] == '=') {
					toBeReturned = stream.new Token(LEQ, scannedTillThisIndex,
							scannedTillThisIndex + 2);
					++scannedTillThisIndex;
				} else if (stream.inputChars[scannedTillThisIndex + 1] == '<') {
					toBeReturned = stream.new Token(LSHIFT,
							scannedTillThisIndex, scannedTillThisIndex + 2);
					++scannedTillThisIndex;
				}
				// the next character isn't '=' or '<'
				else
					toBeReturned = stream.new Token(LT, scannedTillThisIndex,
							scannedTillThisIndex + 1);
			}
			// The EOF has been reached
			else
				toBeReturned = stream.new Token(LT, scannedTillThisIndex,
						scannedTillThisIndex + 1);
			break;

		case '>':
			if (scannedTillThisIndex + 1 < stream.inputChars.length) {
				if (stream.inputChars[scannedTillThisIndex + 1] == '=') {
					toBeReturned = stream.new Token(GEQ, scannedTillThisIndex,
							scannedTillThisIndex + 2);
					++scannedTillThisIndex;
				} else if (stream.inputChars[scannedTillThisIndex + 1] == '>') {
					toBeReturned = stream.new Token(RSHIFT,
							scannedTillThisIndex, scannedTillThisIndex + 2);
					++scannedTillThisIndex;
				}
				// The next character isn't '=' or '>'
				else
					toBeReturned = stream.new Token(GT, scannedTillThisIndex,
							scannedTillThisIndex + 1);
			}
			// The EOF has been reached
			else
				toBeReturned = stream.new Token(GT, scannedTillThisIndex,
						scannedTillThisIndex + 1);
			break;

		case '/':
			if (scannedTillThisIndex + 1 < stream.inputChars.length
					&& stream.inputChars[scannedTillThisIndex + 1] == '/') {
				// Scan till the EOL and return the entire line as a COMMENT
				int begin = scannedTillThisIndex;
				++scannedTillThisIndex;

				char charInComment = stream.inputChars[scannedTillThisIndex];
				char prev = charInComment;
				while (!((charInComment == '\n' && prev != '\r')
						|| charInComment == '\r' || charInComment == '\u0085'
						|| charInComment == '\u2028' || charInComment == '\u2029')) {
					// charInComment is not a newline character
					prev = charInComment;
					if (scannedTillThisIndex + 1 < stream.inputChars.length)
						charInComment = stream.inputChars[++scannedTillThisIndex];
					// End of the input reached.
					else {
						toBeReturned = stream.new Token(COMMENT, begin,
								scannedTillThisIndex + 1);
						return toBeReturned;
					}
				}
				// The 'end' index of 'toBeReturned' is the index of the EOL
				// character
				toBeReturned = stream.new Token(COMMENT, begin,
						scannedTillThisIndex);
			}
			// The next character isn't '/'
			else
				toBeReturned = stream.new Token(DIV, scannedTillThisIndex,
						scannedTillThisIndex + 1);
			break;
		// ii) Strings:
		case '"':
			if (scannedTillThisIndex + 1 < stream.inputChars.length) {
				// Scan till the ending double-quote and return the part between
				// the double quotes as STRING_LIT
				int begin = ++scannedTillThisIndex;

				char charInString = stream.inputChars[scannedTillThisIndex];
				while (charInString != '"') {
					// charInString is not a double-quote
					if (scannedTillThisIndex + 1 < stream.inputChars.length)
						charInString = stream.inputChars[++scannedTillThisIndex];
					// The input has ended before reaching the ending
					// double-quote
					else
						throw stream.new LexicalException(begin,
								"The ending-double-quote is missing");
				}
				toBeReturned = stream.new Token(STRING_LIT, begin,
						scannedTillThisIndex);
			}
			// The input has ended before reaching the ending double-quote
			else
				throw stream.new LexicalException(scannedTillThisIndex,
						"The ending-double-quote is missing - " + ch);
			break;

		default:
			// iii) Identifers:
			if (Character.isJavaIdentifierStart(ch)) {
				int begin = scannedTillThisIndex;
				do {
					if (scannedTillThisIndex + 1 < stream.inputChars.length)
						ch = stream.inputChars[++scannedTillThisIndex];
					else {
						// This index has now reached the end of the input
						// stream. So break out of this loop
						++scannedTillThisIndex;
						break;
						// toBeReturned = stream.new Token(IDENT, begin,
						// scannedTillThisIndex + 1);
						// return toBeReturned;

						// So check if it's a BOOLEAN_LIT or KEYWORD or IDENT
						// and return this token as a token of that kind.
						// String thisToken = new String(stream.inputChars,
						// begin,
						// (scannedTillThisIndex + 1 - begin));
						// if (thisToken.equals("true") ||
						// thisToken.equals("false"))
						// toBeReturned = stream.new Token(BOOLEAN_LIT, begin,
						// scannedTillThisIndex + 1);
						// else if (keywordsHashMap.containsKey(thisToken))
						// toBeReturned = stream.new Token(
						// keywordsHashMap.get(thisToken), begin,
						// scannedTillThisIndex + 1);
						// // This token is an IDENT.
						// else
						// toBeReturned = stream.new Token(IDENT, begin,
						// scannedTillThisIndex + 1);
						// return toBeReturned;
					}
				} while (Character.isJavaIdentifierPart(ch));
				// The latest 'ch' was not a part of an IDENT.
				// So check if this token is a BOOLEAN_LIT or KEYWORD.
				String thisToken = new String(stream.inputChars, begin,
						(scannedTillThisIndex - begin));
				if (thisToken.equals("true") || thisToken.equals("false"))
					toBeReturned = stream.new Token(BOOLEAN_LIT, begin,
							scannedTillThisIndex);
				else if (keywordsHashMap.containsKey(thisToken))
					toBeReturned = stream.new Token(
							keywordsHashMap.get(thisToken), begin,
							scannedTillThisIndex);
				// This token is an IDENT.
				else
					toBeReturned = stream.new Token(IDENT, begin,
							scannedTillThisIndex);
				// Unread the last character that was read.
				--scannedTillThisIndex;
				break;
			}
			// iv) Integer_Literals:
			else if (Character.isDigit(ch)) {
				int begin = scannedTillThisIndex;
				do {
					if (scannedTillThisIndex + 1 < stream.inputChars.length)
						ch = stream.inputChars[++scannedTillThisIndex];
					else {
						// This index has now reached the end of the input
						// stream.
						// So break out of this loop and return this token as
						// IDENT.
						toBeReturned = stream.new Token(INT_LIT, begin,
								scannedTillThisIndex + 1);
						return toBeReturned;
						// break;
					}
				} while (Character.isDigit(ch));
				// ch is not a digit
				toBeReturned = stream.new Token(INT_LIT, begin,
						scannedTillThisIndex);
				// Unread the last character that was read.
				--scannedTillThisIndex;
			}

			// Return EOF for the last character - is this necessary ?
			// case '\0':
			// System.out.println("Detected EOF!!");
			// toBeReturned = stream.new Token(EOF, scannedTillThisIndex,
			// scannedTillThisIndex);
			break;
		}
		if (toBeReturned == null) {
			// This token could not be recognized - throw a LexicalException
			throw stream.new LexicalException(scannedTillThisIndex,
					"Invalid token - " + ch);
		}
		return toBeReturned;
	}
}

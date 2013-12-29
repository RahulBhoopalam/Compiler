package cop5555fa13.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cop5555fa13.TokenStream.Kind.*;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;

// * A TypeCheckVisitor object visits each node in the AST and does type-checking. *
// - It makes use of the 'symbolTable' to keep track of all declared variables.
// - If there're any errors while visiting nodes of the AST, 
//   it adds the node to the 'errorNodeList'.
// - The 'errorLog' contains messages detailing errors, if any.
public class TypeCheckVisitor implements ASTVisitor {
	HashMap<String, Dec> symbolTable;
	List<ASTNode> errorNodeList;
	StringBuilder errorLog;

	public TypeCheckVisitor() {
		symbolTable = new HashMap<String, Dec>();
		errorNodeList = new ArrayList<ASTNode>();
		errorLog = new StringBuilder();
	}

	public List getErrorNodeList() {
		return errorNodeList;
	}

	public boolean isCorrect() {
		return errorNodeList.size() == 0;
	}

	public String getLog() {
		return errorLog.toString();
	}

	private void checkConstraint(boolean constraintHolds, ASTNode currentNode,
			String errorMsg) {
		if (!constraintHolds) {
			errorNodeList.add(currentNode);
			errorLog.append(System.getProperty("line.separator")).append(
					errorMsg);
		}
	}

	// Looks up in the 'symbolTable' for the type of the 'key' given as arg.
	// Returns
	// a. the 'Kind' of the 'key' when the symbolTable contains the key.
	// b. 'null' when the symbolTable doesn't contain the key.
	private Kind lookupType(String key) {
		if (!symbolTable.containsKey(key)) {
			errorLog.append(System.getProperty("line.separator")).append("' ")
					.append(key).append(" ' must be declared before use.");
			return null;
		} else {
			return symbolTable.get(key).type;
		}
	}

	@Override
	public Object visitDec(Dec dec, Object arg) {
		String identInDec = dec.ident.getText();
		// Check that TypeOf(IDENT) = UNDEFINED
		checkConstraint(symbolTable.containsKey(identInDec) == false, dec, "' "
				+ identInDec + " ' already declared.");
		// Check that IDENT != programName
		checkConstraint( !identInDec.equals(arg), dec, identInDec
				+ " cannot be the same as the program's name.");
		// Add this IDENT to the symbolTable only if it hasn't been declared
		// before.
		if (!symbolTable.containsKey(identInDec))
			symbolTable.put(identInDec, dec);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		String progName = program.ident.getText();

		for (Dec dec : program.decList) {
			dec.visit(this, progName);
		}
		for (Stmt stmt : program.stmtList) {
			stmt.visit(this, null);
		}

		return null;
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		// Check that TypeOf(EXPR) = _boolean
		Kind exprType = (Kind) alternativeStmt.expr.visit(this, null);
		checkConstraint(exprType == _boolean, alternativeStmt, "' "
				+ alternativeStmt.expr
				+ " ' must evaluate to a 'boolean' value.");

		for (Stmt stmt : alternativeStmt.ifStmtList) {
			stmt.visit(this, null);
		}

		for (Stmt stmt : alternativeStmt.elseStmtList) {
			stmt.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		// Check that TypeOf(EXPR) = _int
		Kind exprType = (Kind) pauseStmt.expr.visit(this, null);
		checkConstraint(exprType == _int, pauseStmt, "' " + pauseStmt.expr
				+ " ' must evaluate to an 'int' value.");
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		// Check that TypeOf(EXPR) = _boolean
		Kind exprType = (Kind) iterationStmt.expr.visit(this, null);
		checkConstraint(exprType == _boolean, iterationStmt, "' "
				+ iterationStmt.expr + " ' must evaluate to a 'boolean' value.");

		for (Stmt stmt : iterationStmt.stmtList) {
			stmt.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		// Check that TypeOf(IDENT) = pixel
		checkConstraint(
				lookupType(assignPixelStmt.lhsIdent.getText()) == pixel,
				assignPixelStmt, "' " + assignPixelStmt.lhsIdent.getText()
						+ " ' must have type 'pixel'.");

		assignPixelStmt.pixel.visit(this, null);
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		// Check that TypeOf(RED_EXPR) = _int
		Kind redExprType = (Kind) pixel.redExpr.visit(this, null);
		checkConstraint(redExprType == _int, pixel, "' " + pixel.redExpr
				+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(GREEN_EXPR) = _int
		Kind greenExprType = (Kind) pixel.greenExpr.visit(this, null);
		checkConstraint(greenExprType == _int, pixel, "' "
				+ pixel.greenExpr + " ' must evaluate to an 'int' value");

		// Check that TypeOf(BLUE_EXPR) = _int
		Kind blueExprType = (Kind) pixel.blueExpr.visit(this, null);
		checkConstraint(blueExprType == _int, pixel, "' " + pixel.blueExpr
				+ " ' must evaluate to an 'int' value");
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(singlePixelAssignmentStmt.lhsIdent.getText()) == image,
				singlePixelAssignmentStmt, "' "
						+ singlePixelAssignmentStmt.lhsIdent.getText()
						+ " ' must have type 'image'.");

		// Check that TypeOf(X_EXPR) = _int
		Kind xExprType = (Kind) singlePixelAssignmentStmt.xExpr.visit(this,
				null);
		checkConstraint(xExprType == _int, singlePixelAssignmentStmt, "' "
				+ singlePixelAssignmentStmt.xExpr
				+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(Y_EXPR) = _int
		Kind yExprType = (Kind) singlePixelAssignmentStmt.yExpr.visit(this,
				null);
		checkConstraint(yExprType == _int, singlePixelAssignmentStmt, "' "
				+ singlePixelAssignmentStmt.yExpr
				+ " ' must evaluate to an 'int' value");

		singlePixelAssignmentStmt.pixel.visit(this, null);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(singleSampleAssignmentStmt.lhsIdent.getText()) == image,
				singleSampleAssignmentStmt, "' "
						+ singleSampleAssignmentStmt.lhsIdent
						+ " ' must have type 'image'");

		// Check that TypeOf(X_EXPR) = _int
		Kind xExprType = (Kind) singleSampleAssignmentStmt.xExpr.visit(this,
				null);
		checkConstraint(xExprType == _int, singleSampleAssignmentStmt, "' "
				+ singleSampleAssignmentStmt.xExpr
				+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(Y_EXPR) = _int
		Kind yExprType = (Kind) singleSampleAssignmentStmt.yExpr.visit(this,
				null);
		checkConstraint(yExprType == _int, singleSampleAssignmentStmt, "' "
				+ singleSampleAssignmentStmt.yExpr
				+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(RHS_EXPR) = _int
		Kind rhsExprType = (Kind) singleSampleAssignmentStmt.rhsExpr.visit(
				this, null);
		checkConstraint(rhsExprType == _int, singleSampleAssignmentStmt, "' "
				+ singleSampleAssignmentStmt.rhsExpr
				+ " ' must evaluate to an 'int' value");

		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(screenLocationAssignmentStmt.lhsIdent.getText()) == image,
				screenLocationAssignmentStmt, "' "
						+ screenLocationAssignmentStmt.lhsIdent
						+ " ' must have type 'image'");

		// Check that TypeOf(X_SCREEN_EXPR) = _int
		Kind xScreenExprType = (Kind) screenLocationAssignmentStmt.xScreenExpr
				.visit(this, null);
		checkConstraint(xScreenExprType == _int, screenLocationAssignmentStmt,
				"' " + screenLocationAssignmentStmt.xScreenExpr
						+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(Y_SCREEN_EXPR) = _int
		Kind yScreenExprType = (Kind) screenLocationAssignmentStmt.yScreenExpr
				.visit(this, null);
		checkConstraint(yScreenExprType == _int, screenLocationAssignmentStmt,
				"' " + screenLocationAssignmentStmt.yScreenExpr
						+ " ' must evaluate to an 'int' value");

		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(shapeAssignmentStmt.lhsIdent.getText()) == image,
				shapeAssignmentStmt, "' " + shapeAssignmentStmt.lhsIdent.getText()
						+ " ' must have type 'image'");

		// Check that TypeOf(WIDTH_EXPR) = _int
		Kind widthExprType = (Kind) shapeAssignmentStmt.width.visit(this, null);
		checkConstraint(widthExprType == _int, shapeAssignmentStmt, "' "
				+ shapeAssignmentStmt.width
				+ " ' must evaluate to an 'int' value");

		// Check that TypeOf(HEIGHT_EXPR) = _int
		Kind heightExprType = (Kind) shapeAssignmentStmt.height.visit(this,
				null);
		checkConstraint(heightExprType == _int, shapeAssignmentStmt, "' "
				+ shapeAssignmentStmt.height
				+ " ' must evaluate to an 'int' value");

		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(setVisibleAssignmentStmt.lhsIdent.getText()) == image,
				setVisibleAssignmentStmt, "' "
						+ setVisibleAssignmentStmt.lhsIdent
						+ " ' must have type 'image'");

		// Check that TypeOf(EXPR) = _boolean
		Kind exprType = (Kind) setVisibleAssignmentStmt.expr.visit(this, null);
		checkConstraint(exprType == _boolean, setVisibleAssignmentStmt, "' "
				+ setVisibleAssignmentStmt.expr
				+ " ' must evaluate to a 'boolean' value");
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(lookupType(fileAssignStmt.lhsIdent.getText()) == image,
				fileAssignStmt, "' " + fileAssignStmt.lhsIdent.getText()
						+ " ' must have type 'image'");
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		// Check that TypeOf(OR_EXPR) = _boolean
		Kind orExprType = (Kind) conditionalExpr.condition.visit(this, null);
		checkConstraint(orExprType == _boolean, conditionalExpr, "' "
				+ conditionalExpr.condition
				+ " ' must evaluate to a 'boolean' value");

		// Check that TypeOf(TRUE_EXPR) = TypeOf(FALSE_EXPR)
		Kind trueExprType = (Kind) conditionalExpr.trueValue.visit(this, null);
		Kind falseExprType = (Kind) conditionalExpr.falseValue
				.visit(this, null);
		checkConstraint(trueExprType == falseExprType, conditionalExpr, "' "
				+ conditionalExpr.trueValue + " ' and ' "
				+ conditionalExpr.falseValue + " ' must have the same type");

		conditionalExpr.type = trueExprType;
		return trueExprType;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		Kind returnValue = null;
		Kind e0ExprType = (Kind) binaryExpr.e0.visit(this, null);
		Kind e1ExprType = (Kind) binaryExpr.e1.visit(this, null);

		switch (binaryExpr.op.kind) {
		case AND:
		case OR:
			// Check that TypeOf(e0) = _boolean
			checkConstraint(e0ExprType == _boolean, binaryExpr, "' "
					+ binaryExpr.e0 + " ' must evaluate to a 'boolean' value");
			// Check that TypeOf(e1) = _boolean
			checkConstraint(e1ExprType == _boolean, binaryExpr, "' "
					+ binaryExpr.e1 + " ' must evaluate to a 'boolean' value");
			returnValue = _boolean;
			break;

		case PLUS:
		case MINUS:
		case TIMES:
		case DIV:
		case MOD:
		case LSHIFT:
		case RSHIFT:
			// Check that TypeOf(e0) = _int
			checkConstraint(e0ExprType == _int, binaryExpr, "' "
					+ binaryExpr.e0 + " ' must evaluate to a 'int' value");
			// Check that TypeOf(e1) = _int
			checkConstraint(e1ExprType == _int, binaryExpr, "' "
					+ binaryExpr.e1 + " ' must evaluate to a 'int' value");
			returnValue = _int;
			break;

		case EQ:
		case NEQ:
			// Check that TypeOf(e0) = TypeOf(e1)
			checkConstraint(e0ExprType == e1ExprType, binaryExpr, "' "
					+ binaryExpr.e0 + " ' and ' " + binaryExpr.e1
					+ " ' must have the same type");
			returnValue = _boolean;
			break;
		case LT:
		case GT:
		case LEQ:
		case GEQ:
			// Check that TypeOf(e0) = _int
			checkConstraint(e0ExprType == _int, binaryExpr, "' "
					+ binaryExpr.e0 + " ' must evaluate to a 'int' value");
			// Check that TypeOf(e1) = _int
			checkConstraint(e1ExprType == _int, binaryExpr, "' "
					+ binaryExpr.e1 + " ' must evaluate to a 'int' value");
			returnValue = _boolean;
			break;
		}

		binaryExpr.type = returnValue;
		return returnValue;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(lookupType(sampleExpr.ident.getText()) == image,
				sampleExpr, "' " + sampleExpr.ident
						+ " ' must have type 'image'");

		// Check that TypeOf(X_LOC_EXPR) = _int
		Kind xLocExprType = (Kind) sampleExpr.xLoc.visit(this, null);
		checkConstraint(xLocExprType == _int, sampleExpr, "' "
				+ sampleExpr.xLoc + " ' must evaluate to an 'int' value");

		// Check that TypeOf(Y_LOC_EXPR) = _int
		Kind yLocExprType = (Kind) sampleExpr.yLoc.visit(this, null);
		checkConstraint(yLocExprType == _int, sampleExpr, "' "
				+ sampleExpr.yLoc + " ' must evaluate to an 'int' value");

		sampleExpr.type = _int;
		return _int;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		// Check that TypeOf(IDENT) = image
		checkConstraint(
				lookupType(imageAttributeExpr.ident.getText()) == image,
				imageAttributeExpr, "' " + imageAttributeExpr.ident.getText()
						+ " ' must have type 'image'");
		imageAttributeExpr.type = _int;
		return _int;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		identExpr.type = lookupType(identExpr.ident.getText());
		return identExpr.type;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		intLitExpr.type = _int;
		return _int;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		booleanLitExpr.type = _boolean;
		return _boolean;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		PreDefExpr.type = _int;
		return _int;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		// Check that TypeOf(IDENT) = TypeOf(EXPR)
		Kind exprType = (Kind) assignExprStmt.expr.visit(this, null);
		checkConstraint(
				lookupType(assignExprStmt.lhsIdent.getText()) == exprType,
				assignExprStmt, "' " + assignExprStmt.lhsIdent.getText()
						+ " ' and ' " + assignExprStmt.expr
						+ " ' must have the same type.");

		return null;
	}

}

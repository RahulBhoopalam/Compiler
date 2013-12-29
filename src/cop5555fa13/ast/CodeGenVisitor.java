package cop5555fa13.ast;

import static cop5555fa13.TokenStream.Kind.*;
import static cop5555fa13.TokenStream.Kind;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5555fa13.runtime.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	private String progName;

	private int slot = 0;

	private int getSlot(String name) {
		Integer s = slotMap.get(name);
		if (s != null)
			return s;
		else {
			slotMap.put(name, slot);
			return slot++;
		}
	}

	HashMap<String, Integer> slotMap = new HashMap<String, Integer>();

	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	// Map to store 'program variable's and their 'type's.
	HashMap<String, String> varTypeMap = new HashMap<String, String>();

	@Override
	public Object visitDec(Dec dec, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		// insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(), l);
		// get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		String jvmType = typeMap.get(t);
		// Store the 'name' and 'type' of the variable in the varTypeMap.
		varTypeMap.put(varName, jvmType);

		Object initialValue = (t == _int || t == pixel || t == _boolean) ? Integer
				.valueOf(0) : null;
		// add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		// if this is an image, generate code to create an empty image
		if (t == image) {
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>",
					"()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		// Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null,
				null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);

		// visit children
		for (Dec dec : program.decList) {
			dec.visit(this, mv);
		}
		for (Stmt stmt : program.stmtList) {
			stmt.visit(this, mv);
		}

		// add a return statement to the main method
		mv.visitInsn(RETURN);

		// finish up
		Label end = new Label();
		mv.visitLabel(end);
		// visit local variables. The one in slot 0 is the formal parameter of
		// the main method.
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, start, end,
				getSlot("args"));
		// if there are any more local variables, visit them now.
		// ......

		// finish up method
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		// convert to bytearray and return
		return cw.toByteArray();
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method"); // TODO
		// Auto-generated
		// method
		// stub
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		// pause Expr ;
		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1:
		// Visit the expression to generate code so that
		// stack's top = expr's value
		pauseStmt.expr.visit(this, mv);

		// Step 3: Invoke the PLPImage.pause(int) method
		mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "pause",
				PLPImage.pauseDesc);
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method"); // TODO
																	// Auto-generated
																	// method
																	// stub
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method"); // TODO
																	// Auto-generated
																	// method
																	// stub
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		// {{ Expr , Expr , Expr }}
		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1: Visit the expressions to generate code to evaluate them.
		pixel.redExpr.visit(this, mv);
		pixel.greenExpr.visit(this, mv);
		pixel.blueExpr.visit(this, mv);

		// Invoke the Pixel.makePixel() to pack the above 3 into an int.
		mv.visitMethodInsn(INVOKESTATIC,
				cop5555fa13.runtime.Pixel.JVMClassName, "makePixel",
				cop5555fa13.runtime.Pixel.makePixelSig);
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		// IDENT . pixels [ Expr , Expr ] = Pixel ;
		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1:
		// Generate code to leave the address of the image on the stack top.
		String imageName = singlePixelAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		// Duplicate for invoking updateFrame()
		mv.visitInsn(DUP);

		// Step 2: Visit the expressions to generate code to evaluate them.
		// This indicates a location in the image indicated by the IDENT.
		singlePixelAssignmentStmt.xExpr.visit(this, mv);
		singlePixelAssignmentStmt.yExpr.visit(this, mv);

		// Step 3: Visit the Pixel to generate code to pack it into an integer.
		singlePixelAssignmentStmt.pixel.visit(this, mv);

		// Step 4: Set the pixel in the image
		// Invoke IDENT.setPixel(x, y, newPixel)
		// [consumes first copy of image's address]
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setPixel",
				PLPImageExtension.setPixelDesc);

		// Step 5: Invoke the image�s updateFrame() method.
		// [consumes second copy of image's address]
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame",
				PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		// IDENT . pixels [ Expr , Expr ] (red | green | blue) = Expr ;
		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1:
		// Generate code to leave the address of the image on the stack top.
		String imageName = singleSampleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);

		// Step 2:
		// Visit the expressions and leave their values on the stack top.
		singleSampleAssignmentStmt.xExpr.visit(this, mv);
		singleSampleAssignmentStmt.yExpr.visit(this, mv);
		switch (singleSampleAssignmentStmt.color.kind) {
		case red:
			mv.visitLdcInsn(ImageConstants.RED);
			break;
		case green:
			mv.visitLdcInsn(ImageConstants.GRN);
			break;
		case blue:
			mv.visitLdcInsn(ImageConstants.BLU);
			break;
		}
		singleSampleAssignmentStmt.rhsExpr.visit(this, mv);

		// Step 3: Invoke PLPImage.setSample() to update the given sample of the
		// indicated image.
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "setSample",
				PLPImageExtension.setSampleDesc);
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		// IDENT . location = [ Expr , Expr ] ;
		MethodVisitor mv = (MethodVisitor) arg;

		// Step1: Generate code to leave the image's address on the stack top.
		String imageName = screenLocationAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		// Duplicate to set PLPImage.y_loc
		mv.visitInsn(DUP);
		// Duplicate once more for invoking updateFrame()
		mv.visitInsn(DUP);

		// Step 2:
		// a. Visit the expressions.
		// b. Store the values in IDENT's x_loc and y_loc

		// Set the x_loc field [consumes first copy of image's address]
		screenLocationAssignmentStmt.xScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "x_loc", "I");
		// Set the y_loc field [consumes second copy of image's address]
		screenLocationAssignmentStmt.yScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "y_loc", "I");

		// Step 3: Invoke the image�s updateFrame method.
		// [consumes third copy of image's address]
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame",
				PLPImage.updateFrameDesc);

		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		// IDENT . shape = [ Expr , Expr ] ;
		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1: Generate code to leave image's address on top of stack.
		String imageName = shapeAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		// Duplicate to set PLPImage.height
		mv.visitInsn(DUP);
		// Duplicate once more for invoking updateImageSize()
		mv.visitInsn(DUP);
		// Duplicate once more for invoking updateFrame()
		mv.visitInsn(DUP);

		// Step 2:
		// a. Visit the expressions.
		// b. Store the values in the IDENT's width and height fields

		// Set the width field [consumes first copy of image's address]
		shapeAssignmentStmt.width.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "width", "I");

		// Set the height field [consumes second copy of image's address]
		shapeAssignmentStmt.height.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "height", "I");

		// Step 3: Invoke the image�s updateImageSize method.
		// [consumes third copy of image's address]
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,
				"updateImageSize", PLPImageExtension.updateImageSizeDesc);

		// Step 4: Invoke the image�s updateFrame method.
		// [consumes fourth copy of image's address]
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame",
				PLPImage.updateFrameDesc);

		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		// IDENT . visible = Expr ;
		MethodVisitor mv = (MethodVisitor) arg;
		// generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, PLPImage.classDesc);
		// duplicate address. Will consume one for updating setVisible field
		// and one for invoking updateFrame.
		mv.visitInsn(DUP);
		// visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this, mv);
		// set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", "Z");
		// generate code to update frame, consuming the second image address.
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame",
				PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		// IDENT = STRING_LIT ;
		MethodVisitor mv = (MethodVisitor) arg;
		// generate code to leave address of target image on top of stack
		String image_name = fileAssignStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
		// generate code to duplicate this address. We'll need it for loading
		// the image and again for updating the frame.
		mv.visitInsn(DUP);
		// generate code to leave address of String containing a filename or url
		mv.visitLdcInsn(fileAssignStmt.fileName.getText());
		// generate code to get the image by calling the loadImage method
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "loadImage",
				PLPImage.loadImageDesc);
		// generate code to update frame
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updateFrame",
				PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method"); // TODO
																	// Auto-generated
																	// method
																	// stub
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		// Expr1 OP Expr2

		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1: Visit the expressions to leave their values on top of
		// the stack.
		binaryExpr.e0.visit(this, mv);
		binaryExpr.e1.visit(this, mv);

		// Step 2: Evaluate e0 and e1 for these operators on ints:
		// +,-,*,/,%,<<, >>
		// and leave the result on top of the stack.
		switch (binaryExpr.op.kind) {
		case PLUS:
			mv.visitInsn(IADD);
			break;
		case MINUS:
			mv.visitInsn(ISUB);
			break;
		case TIMES:
			mv.visitInsn(IMUL);
			break;
		case DIV:
			mv.visitInsn(IDIV);
			break;
		case MOD:
			mv.visitInsn(IREM);
			break;
		case LSHIFT:
			mv.visitInsn(ISHL);
			break;
		case RSHIFT:
			mv.visitInsn(ISHR);
			break;
		}
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {

		// IDENT [ Expr , Expr ] (red | green | blue)
		// => Invoke PLPImage.getSample(int, int, int);

		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1. Generate code to leave image's address on top of stack.
		String imageName = sampleExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));

		// Step 2. Visit the expressions.
		// That will generate code to leave their values on the stack's top.
		sampleExpr.xLoc.visit(this, mv);
		sampleExpr.yLoc.visit(this, mv);

		if (sampleExpr.color.kind == red) {
			mv.visitLdcInsn(ImageConstants.RED);
		} else if (sampleExpr.color.kind == green) {
			mv.visitLdcInsn(ImageConstants.GRN);
		} else if (sampleExpr.color.kind == blue) {
			mv.visitLdcInsn(ImageConstants.BLU);
		}

		// Step 3: Invoke getSample() to return the value of the sample and
		// leave it on top of the stack
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getSample",
				PLPImageExtension.getSampleDesc);
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		// IDENT.height OR IDENT.width OR IDENT.x_loc OR IDENT.y_loc

		MethodVisitor mv = (MethodVisitor) arg;

		// Step 1: Generate code to leave image on top of stack
		String imageName = imageAttributeExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));

		// Step 2: Generate code to load the appropriate attribute of the image
		// indicated by IDENT, on top of the stack.
		mv.visitFieldInsn(GETFIELD, PLPImage.className,
				imageAttributeExpr.selector.getText(), "I");

		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		// IDENT
		MethodVisitor mv = (MethodVisitor) arg;

		// Generate code to load the value of IDENT on top of the stack.
		String nameOfIdent = identExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName, nameOfIdent,
				varTypeMap.get(nameOfIdent));

		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		// INT_LIT
		MethodVisitor mv = (MethodVisitor) arg;

		// Generate code to load the value INT_LIT on top of the stack.
		int val = Integer.parseInt(intLitExpr.intLit.getText());
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		// BOOLEAN_LIT
		MethodVisitor mv = (MethodVisitor) arg;

		// Generate code to load the value BOOLEAN_LIT on top of the stack.
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true") ? 1 : 0;
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		// x | y | Z | SCREEN_SIZE
		MethodVisitor mv = (MethodVisitor) arg;

		// Yet to implement the conditions for 'x' and 'y'
		if (PreDefExpr.constantLit.kind == Z) {
			mv.visitLdcInsn(ImageConstants.Z);
		} else if (PreDefExpr.constantLit.kind == SCREEN_SIZE) {
			mv.visitLdcInsn(PLPImage.SCREENSIZE);
		}
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		// IDENT = Expr ;
		MethodVisitor mv = (MethodVisitor) arg;
		// Step 1: Evaluate the expr and leave it's value on top of the stack.
		assignExprStmt.expr.visit(this, mv);

		// Step 2: Set IDENT's value to the value on the stack's top.
		String varName = assignExprStmt.lhsIdent.getText();
		mv.visitFieldInsn(PUTSTATIC, progName, varName, varTypeMap.get(varName));

		return null;
	}

}

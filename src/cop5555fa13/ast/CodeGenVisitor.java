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
	private int getSlot(String name){
		Integer s = slotMap.get(name);
		if (s != null) return s;
		else{
			slotMap.put(name, slot);
			return slot++;
		}		
	}


	HashMap<String,Integer> slotMap = new HashMap<String,Integer>();
	
	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	@Override
	public Object visitDec(Dec dec, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(),l);
		//get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		String jvmType = typeMap.get(t);
		Object initialValue = (t == _int || t==pixel || t== _boolean) ? Integer.valueOf(0) : null;
		//add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		//if this is an image, generate code to create an empty image
		if (t == image){
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		//Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);		
		
		
		//visit children
		for(Dec dec : program.decList){
			dec.visit(this,mv);
		}
		for (Stmt stmt : program.stmtList){
			stmt.visit(this, mv);
		}
		
		
		//add a return statement to the main method
		mv.visitInsn(RETURN);
		
		//finish up
		Label end = new Label();
		mv.visitLabel(end);
		//visit local variables. The one is slot 0 is the formal parameter of the main method.
		mv.visitLocalVariable("args","[Ljava/lang/String;",null, start, end, getSlot("args"));
		//if there are any more local variables, visit them now.
		// ......
		
		//finish up method
		mv.visitMaxs(1,1);
		mv.visitEnd();
		//convert to bytearray and return 
		return cw.toByteArray();
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//duplicate address.  Will consume one for updating setVisible field
		//and one for invoking updateFrame.
		mv.visitInsn(DUP);
		//visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this,mv);
		//set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", 
				"Z");	
	    //generate code to update frame, consuming the second image address.
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave address of target image on top of stack
	    String image_name = fileAssignStmt.lhsIdent.getText();
	    mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
	    //generate code to duplicate this address.  We'll need it for loading
	    //the image and again for updating the frame.
	    mv.visitInsn(DUP);
		//generate code to leave address of String containing a filename or url
	    mv.visitLdcInsn(fileAssignStmt.fileName.getText());
		//generate code to get the image by calling the loadImage method
	    mv.visitMethodInsn(INVOKEVIRTUAL, 
	    		PLPImage.className, "loadImage", PLPImage.loadImageDesc);
	    //generate code to update frame
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true")? 1 : 0;
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		System.out.println("visiting unimplemented visit method");  // TODO Auto-generated method stub
		return null;
	}

}

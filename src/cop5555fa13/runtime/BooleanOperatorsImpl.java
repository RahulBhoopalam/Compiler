package cop5555fa13.runtime;

/**
 * This class provides the implementation for the boolean operators for which
 * there is no corresponding single-bytecode-instructions.<br>
 * &, |, &lt, &gt, ==, !=, &lt=, &gt=
 * 
 * @author Rahul
 * 
 */
public class BooleanOperatorsImpl {

	public static final String JVMClassName = "cop5555fa13/runtime/BooleanOperatorsImpl";

	public static final String andSig = "(ZZ)Z";
	public static boolean and(boolean a, boolean b) {
		return a & b;
	}

	public static final String orSig = "(ZZ)Z";
	public static boolean or(boolean a, boolean b) {
		return a | b;
	}

	public static final String eqISig = "(II)Z";
	public static boolean eqI(int a, int b) {
		return a == b;
	}

	public static final String eqBSig = "(ZZ)Z";
	public static boolean eqB(boolean a, boolean b) {
		return a == b;
	}

	public static final String eqImageSig = new StringBuilder("(")
			.append(PLPImage.classDesc).append(PLPImage.classDesc).append(")Z")
			.toString();
	public static boolean eqImage(PLPImage a, PLPImage b) {
		return a == b;
	}

	public static final String neqISig = "(II)Z";
	public static boolean neqI(int a, int b) {
		return a != b;
	}

	public static final String neqBSig = "(ZZ)Z";
	public static boolean neqB(boolean a, boolean b) {
		return a != b;
	}

	public static final String neqImageSig = new StringBuilder("(")
			.append(PLPImage.classDesc).append(PLPImage.classDesc).append(")Z")
			.toString();
	public static boolean neqImage(PLPImage a, PLPImage b) {
		return a != b;
	}

	public static final String ltSig = "(II)Z";
	public static boolean lt(int a, int b) {
		return a < b;
	}

	public static final String ltEqSig = "(II)Z";
	public static boolean ltEq(int a, int b) {
		return a <= b;
	}

	public static final String gtSig = "(II)Z";
	public static boolean gt(int a, int b) {
		return a > b;
	}

	public static final String gtEqSig = "(II)Z";
	public static boolean gtEq(int a, int b) {
		return a >= b;
	}
}

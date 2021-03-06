package cop5555fa13.runtime;

/**
 * Assignment 5 requires us to not change the given classes in the 'runtime'
 * package because during grading, they will be replaced with other versions
 * that emit generated images and other data to allow comparison with items
 * generated by a reference compiler. Thus I have 'extended' the PLPImage class
 * to add additional fields
 */

public class PLPImageExtension extends PLPImage {
	public static final String updateImageSizeDesc = "()V";
	public static final String getSampleDesc = "(III)I";
	public static final String setSampleDesc = "(IIII)V";
	public static final String setWidthDesc = "(I)V";
	public static final String setHeightDesc = "(I)V";
	public static final String setPixelDesc = "(III)V";
}

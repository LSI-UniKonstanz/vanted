package org.vanted.scaling.resources;


import javax.swing.plaf.FontUIResource;

/**
 * Customized {@link FontUIResource} class.
 * 
 * @author dim8
 *
 */
public class ScaledFontUIResource extends FontUIResource {

	private static final long serialVersionUID = -4445131425991614502L;

	private int currentDPI = -1;
	
	public ScaledFontUIResource(String name, int style, int size) {
		super(name, style, size);
	}
	
	public void setDPI(int dpi) {
		currentDPI = dpi;
	}
	
	/**
	 * The corresponding DPI of the scaled resource.  
	 * @return current DPI or -1, if not set
	 */
	public int getDPI() {
		return currentDPI;
	}
}

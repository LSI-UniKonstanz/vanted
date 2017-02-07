package org.vanted.scaling;

import java.awt.Toolkit;

import javax.swing.Icon;
/**
 * Similar to the default scaler, with the only difference that
 * we have to adjust the requested scaling factor to the current.
 * Windows already performs some degree of rescaling of e.g. fonts.
 * 
 * @author dim8
 *
 */
public class WindowsScaler extends BasicScaler {
	
	public WindowsScaler(float scaleFactor) {
		super(scaleFactor / getCurrentScaling(scaleFactor));
	}

	private static float getCurrentScaling(float dpif) {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		
		return dpi / dpif;
	}

	public Icon modifyIcon(Object key, Icon original) {
		return original;
	}
}
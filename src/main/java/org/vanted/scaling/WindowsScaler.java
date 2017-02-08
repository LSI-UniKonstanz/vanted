package org.vanted.scaling;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.Icon;
/**
 * Similar to the default scaler, with the only difference that
 * we have to adjust the requested scaling factor to the current.
 * Windows already performs some degree of rescaling of e.g. fonts.
 * 
 * [NEEDS FURTHER TESTING: DISABLED FOR NOW!]
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

	//Only this one should (possibly) remain so
	@Override
	public Icon modifyIcon(Object key, Icon original) {
		return original;
	}

	@Override
	public Font modifyFont(Object key, Font original) {
		return original;
	}

	@Override
	public Integer modifyInteger(Object key, Integer original) {
		return original;
	}
	
	@Override
	public Insets modifyInsets(Object key, Insets original) {
		return original;
	}
}
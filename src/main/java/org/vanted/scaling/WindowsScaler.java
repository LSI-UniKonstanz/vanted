package org.vanted.scaling;

import javax.swing.Icon;
/**
 * Similar to the default scaler. 
 * Not necessary/able to scale LAF-Icons.  
 * 
 * @author dim8
 *
 */
public class WindowsScaler extends BasicScaler {
	
	public WindowsScaler(float scaleFactor) {
		super(scaleFactor);
		
	}

	@Override
	public Icon modifyIcon(Object key, Icon original) {
		return original;
	}
}
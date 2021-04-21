// ==============================================================================
//
// WindowsScaler.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers;

import javax.swing.Icon;

/**
 * Similar to the default scaler. Not necessary/able to scale LAF-Icons.
 * 
 * @author D. Garkov
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
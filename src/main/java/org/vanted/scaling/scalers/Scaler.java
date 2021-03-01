// ==============================================================================
//
// Scaler.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.Icon;

/**
 * All scalers should implement this interface. Alternatively, one could more
 * conveniently just extend the default scaler - {@link BasicScaler}.
 * 
 * @author D. Garkov
 */
public interface Scaler {
	
	/**
	 * Used for synchronized LAFs, where only one change (e.g. Font) is sufficient
	 * (e.g. Nimbus).
	 */
	void initialScaling();
	
	/**
	 * Implement to modify Fonts.
	 * 
	 * @param key
	 *           UIDefaults key
	 * @param original
	 *           instance to be scaled
	 * @return newly scaled instance
	 */
	Font modifyFont(Object key, Font original);
	
	/**
	 * Implement to modify Icons.
	 * 
	 * @param key
	 *           UIDefaults key
	 * @param original
	 *           instance to be scaled
	 * @return newly scaled instance
	 */
	Icon modifyIcon(Object key, Icon original);
	
	/**
	 * Implement to modify Integers.
	 * 
	 * @param key
	 *           UIDefaults key
	 * @param original
	 *           instance to be scaled
	 * @return newly scaled instance
	 */
	Integer modifyInteger(Object key, Integer original);
	
	/**
	 * Implement to modify Insets.
	 * 
	 * @param original
	 *           Insets instance to be scaled
	 * @param original
	 *           InsetsUIResource instance to be scaled
	 * @return newly scaled instance
	 */
	Insets modifyInsets(Insets original);
	
}
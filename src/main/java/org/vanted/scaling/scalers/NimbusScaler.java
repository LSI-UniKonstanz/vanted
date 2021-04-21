// ==============================================================================
//
// NimbusScaler.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.plaf.FontUIResource;

/**
 * It is sufficient enough to rescale only the default font, this fires a
 * property change and Nimbus does the rest alone. Icons are sometimes shown
 * with marks, so we return them unchanged.
 * 
 * @author D. Garkov
 */
public class NimbusScaler extends BasicScaler {
	
	public NimbusScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	@Override
	public void initialScaling() {
		
		Font font = uiDefaults.getFont("defaultFont");
		
		if (font != null)
			uiDefaults.put("defaultFont",
					new FontUIResource(font.getName(), font.getStyle(), Math.round(font.getSize() * scaleFactor)));
	}
	
	/**
	 * Nimbus scales the rest internally based on the above change.
	 */
	@Override
	public Font modifyFont(Object key, Font original) {
		return original;
	}
	
	/**
	 * Distortions & marks are visible, ergo no modifications.
	 */
	@Override
	public Icon modifyIcon(Object key, Icon original) {
		return original;
	}
}
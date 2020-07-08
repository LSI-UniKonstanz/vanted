// ==============================================================================
//
// HTMLScaler.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers.component;

import javax.swing.JComponent;

import org.vanted.scaling.scalers.Scaler;

/**
 * Implement this to make sure your HTML-styled component's text will be scaled.
 * 
 * @author D. Garkov
 *
 */
public interface HTMLScaler extends Scaler {

	void coscaleHTML(JComponent component);
}

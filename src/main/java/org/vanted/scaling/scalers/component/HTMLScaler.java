package org.vanted.scaling.scalers.component;

import javax.swing.JComponent;

import org.vanted.scaling.scalers.Scaler;

/**
 * Implement this to make sure your HTML-styled component's text will be scaled.
 * 
 * @author dim8
 *
 */
public interface HTMLScaler extends Scaler {

	void coscaleHTML(JComponent component);
}

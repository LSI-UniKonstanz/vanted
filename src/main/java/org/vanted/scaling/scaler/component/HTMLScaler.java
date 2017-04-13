package org.vanted.scaling.scaler.component;

import javax.swing.JComponent;

import org.vanted.scaling.scaler.Scaler;

/**
 * Implement this to make sure your HTML-styled component's text
 * will be scaled.
 * 
 * @author dim8
 *
 */
public interface HTMLScaler extends Scaler {

	public void coscaleHTML(JComponent component);
}

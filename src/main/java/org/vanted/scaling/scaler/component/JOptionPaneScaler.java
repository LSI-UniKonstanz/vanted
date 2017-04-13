package org.vanted.scaling.scaler.component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * A {@linkplain JOptionPane}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JOptionPaneScaler extends ComponentScaler {

	public JOptionPaneScaler(float scaleFactor) {
		super(scaleFactor);
	}

	@Override
	public void coscaleIcon(JComponent component) {
		modifyIcon((JOptionPane) component);
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JOptionPane.
	 *
	 * @param pane JOptionPane
	 */
	private void modifyIcon(JOptionPane pane) {
		Icon i; //any icon
		
		if ((i = pane.getIcon()) != null)
			pane.setIcon(modifyIcon(null, i));
			
		pane.validate();
	}
}
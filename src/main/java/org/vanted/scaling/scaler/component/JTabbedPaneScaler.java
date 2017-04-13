package org.vanted.scaling.scaler.component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * A {@linkplain JTabbedPane}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JTabbedPaneScaler extends ComponentScaler {

	public JTabbedPaneScaler(float scaleFactor) {
		super(scaleFactor);
	}

	
	@Override
	public void coscaleIcon(JComponent component) {
		modifyIcon((JTabbedPane) component);
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JTabbedPane.
	 *
	 * @param pane JTabbedPane
	 */
	private void modifyIcon(JTabbedPane pane) {
		Icon i;
		int j = 0;
		
		while(pane.getTabCount() > 0 && (i = pane.getIconAt(j)) != null) {
			pane.setIconAt(j, modifyIcon(null, i));
			j++;
		}
	}
}
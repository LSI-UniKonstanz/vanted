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
	
	/**
	 * A method to be called when this {@linkplain JTabbedPaneScaler} has been
	 * dispatched to some immediate Component to be scaled. This tackles the problem
	 * that after a complete application scaling, through the ScalingSlider, further
	 * components, initialized posterior, are not scaled. In order to do so, attach a
	 * scaler and call this method upon initialization.
	 *  
	 * @param immediateComponent to be scaled
	 */
	public void scaleComponents(JComponent immediateComponent) {
		coscaleFont(immediateComponent);
		coscaleInsets(immediateComponent);
		this.coscaleIcon(immediateComponent);
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
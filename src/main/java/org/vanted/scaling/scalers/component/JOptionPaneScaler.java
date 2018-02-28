package org.vanted.scaling.scalers.component;

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
	 * A method to be called when this {@linkplain JOptionPaneScaler} has been
	 * dispatched to some immediate Component to be scaled. This tackles the problem
	 * that after a complete application scaling, through the ScalingSlider, further
	 * components, initialized posterior, are not scaled. In order to do so, attach
	 * a scaler and call this method upon initialization.
	 * 
	 * @param immediateComponent
	 *            to be scaled
	 */
	public void scaleComponent(JComponent immediateComponent) {
		coscaleFont(immediateComponent);
		coscaleInsets(immediateComponent);
		this.coscaleIcon(immediateComponent);
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JOptionPane.
	 *
	 * @param pane
	 *            JOptionPane
	 */
	private void modifyIcon(JOptionPane pane) {
		Icon i; // any icon

		if ((i = pane.getIcon()) != null)
			pane.setIcon(modifyIcon(null, i));

		pane.validate();
	}

	// TODO HTML scaling
}
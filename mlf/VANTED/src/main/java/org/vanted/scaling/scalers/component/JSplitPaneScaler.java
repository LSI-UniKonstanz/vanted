package org.vanted.scaling.scalers.component;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 * A {@linkplain JSplitPane}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JSplitPaneScaler extends ComponentScaler {

	public JSplitPaneScaler(float scaleFactor) {
		super(scaleFactor);
	}

	@Override
	public void scaleComponent(JComponent immediateComponent) {
		super.scaleComponent(immediateComponent);

		coscaleDividerLocation(immediateComponent);
	}

	/**
	 * The divider location is indirectly "scaled", due to the scaling and packing
	 * of its direct children.
	 * 
	 * @param component
	 *            the JSplitPane, whose divider location should scale up
	 */
	private void coscaleDividerLocation(JComponent component) {
		JSplitPane pane = (JSplitPane) component;

		// scale children
		Component right = pane.getRightComponent();
		right.setPreferredSize(modifySize(right.getPreferredSize()));
		Component left = pane.getLeftComponent();
		left.setPreferredSize(modifySize(left.getPreferredSize()));

		// pack JSP according children's new preferred sizes
		pane.resetToPreferredSizes();
	}
}

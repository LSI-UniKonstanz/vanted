package org.vanted.scaling.scalers.component;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSplitPane;


public class JSplitPaneScaler extends ComponentScaler {

	public JSplitPaneScaler(float scaleFactor) {
		super(scaleFactor);
	}

	@Override
	public void scaleComponent(JComponent immediateComponent) {
		super.scaleComponent(immediateComponent);
		
		
		coscaleDividerLocation(immediateComponent);
	}
	
	public void coscaleDividerLocation(JComponent component) {
		JSplitPane pane = (JSplitPane) component;
		
		//scale children
		Component right = pane.getRightComponent();
		right.setPreferredSize(modifySize(right.getPreferredSize()));
		Component left = pane.getLeftComponent();
		left.setPreferredSize(modifySize(left.getPreferredSize()));
		
		//pack JSP according children's new preferred sizes
		pane.resetToPreferredSizes();
	}
	
	private Dimension modifySize(Dimension size) {
		return new Dimension(modifyInteger(size.width), modifyInteger(size.height));
	}
	
	

}

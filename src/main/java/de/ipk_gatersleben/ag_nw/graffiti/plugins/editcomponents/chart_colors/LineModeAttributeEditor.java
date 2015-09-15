/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class LineModeAttributeEditor extends AbstractValueEditComponent {
	private JComboBox<LineModeSetting> guiComp;
	
	private boolean changed = false;
	
	private final LineModeSetting[] knownLineModes = getPossibleLineModes();
	
	public LineModeAttributeEditor(Displayable disp) {
		super(disp);
		setGUI((LineModeAttribute) disp, false);
	}
	
	private LineModeSetting[] getPossibleLineModes() {
		return new LineModeSetting[] {
				null,
				new LineModeSetting(0, 0),
				new LineModeSetting(5, 5),
				new LineModeSetting(10, 10),
				new LineModeSetting(20, 20),
				new LineModeSetting(10, 5),
				new LineModeSetting(5, 10),
				new LineModeSetting(20, 10),
				new LineModeSetting(10, 20) };
	}
	
	private void setGUI(LineModeAttribute lma, boolean showEmpty) {
		guiComp = new JComboBox<LineModeSetting>(getLineModes()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMinimumSize() {
				Dimension res = super.getMinimumSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension res = super.getPreferredSize();
				res.setSize(20, res.getHeight());
				return res;
			}
		};
		guiComp.setRenderer(new LineModeCellRenderer());
		guiComp.setOpaque(false);
		
	}
	
	private LineModeSetting[] getLineModes() {
		return knownLineModes;
	}
	
	public JComponent getComponent() {
		return guiComp;
	}
	
	public void setEditFieldValue() {
		LineModeAttribute lma = (LineModeAttribute) getDisplayable();
		LineModeSetting matching = getMatchingLineMode(lma);
		if (matching != null)
			guiComp.setSelectedItem(matching);
		else
			guiComp.setSelectedIndex(1);
	}
	
	private LineModeSetting getMatchingLineMode(LineModeAttribute lma) {
		float[] da = lma.getDashArray();
		if (da == null || da.length != 2)
			return null;
		for (LineModeSetting lms : knownLineModes) {
			if (lms == null)
				continue;
			float[] testVal = lms.getDashArray();
			if (testVal != null) {
				if ((Math.abs(testVal[0] - da[0]) + Math.abs(testVal[1] - da[1])) < LineModeSetting.epsilon)
					return lms;
			}
		}
		return null;
	}
	
	private LineModeSetting getActiveLineMode() {
		return (LineModeSetting) guiComp.getSelectedItem();
	}
	
	public void setValue() {
		LineModeAttribute lma = (LineModeAttribute) getDisplayable();
		// check, if attribute value is different from this selected linemodeentry
		// before firing costly transaction changes
		float[] attrDashArray = lma.getDashArray();
		float[] currentDashArray = getCurrentDashArray();
		changed = false;
		if (attrDashArray != null && currentDashArray != null && attrDashArray.length == currentDashArray.length) {
			for (int i = 0; i < attrDashArray.length; i++)
				if (attrDashArray[i] != currentDashArray[i])
					changed = true;
		}
		else {
			changed = true;
		}
		
		if (changed && getActiveLineMode() != null && !getActiveLineMode().isEmptyValue()) {
			lma.setDashArray(getCurrentDashArray());
			lma.setValue(lma.getValue());
		}
	}
	
	@Override
	public void setShowEmpty(boolean showEmpty) {
		super.setShowEmpty(showEmpty);
		if (showEmpty)
			guiComp.setSelectedIndex(0);
	}
	
	private float[] getCurrentDashArray() {
		return getActiveLineMode().getDashArray();
	}
}

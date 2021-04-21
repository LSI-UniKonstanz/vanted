// ==============================================================================
//
// ImmutableCheckbox.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.resources;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * A JCheckBox that remains unchanged regardless of the scaling. To change, you
 * have to re-instantiate it.
 * <p>
 * Used for the HighDPISupport pane, where it is vital to preserve state, so
 * that the user can continue to work with its components.
 * 
 * @author D. Garkov
 */
public class ImmutableCheckBox extends JCheckBox {
	
	private static final long serialVersionUID = 2820781278205876605L;
	private Dimension dimension;
	private Icon icon;
	private FontUIResource font;
	
	public ImmutableCheckBox() {
		super();
		dimension = this.getSize();
		icon = (Icon) UIManager.get("CheckBox.icon");
		font = (FontUIResource) UIManager.get("CheckBox.font");
	}
	
	@Override
	public Font getFont() {
		return font;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return dimension;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return dimension;
	}
	
	@Override
	public Dimension getMinimumSize() {
		return dimension;
	}
}

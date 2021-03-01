/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 27.06.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.set_background_color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas To change the template for this generated type
 *         comment go to Window>Preferences>Java>Code Generation>Code and
 *         Comments
 */
public class SetBackgroundColorAlgorithm extends AbstractEditorAlgorithm
		implements ProvidesGeneralContextMenu, ActionListener, NeedsSwingThread {
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Set Background Color";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "menu.window"; // View
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.VISUAL));
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('B', InputEvent.ALT_DOWN_MASK);
	}
	
	public void execute() {
		Color oldC = AttributeHelper.getColorFromAttribute(graph, "", "graphbackgroundcolor", Color.white);
		
		Color newC = JColorChooser.showDialog(getMainFrame(), "Select Background-Color", oldC);
		
		if (newC != null)
			AttributeHelper.setColorFromAttribute(graph, "", "graphbackgroundcolor", newC);
		
	}
	
	public JMenuItem[] getCurrentContextMenuItem() {
		return null; /*
							* JMenuItem menuItem = new JMenuItem(getName());
							* menuItem.addActionListener(this); return new JMenuItem[] { menuItem };
							*/
	}
	
	public void actionPerformed(ActionEvent e) {
		execute();
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
}

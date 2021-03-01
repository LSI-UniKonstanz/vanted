// ==============================================================================
//
// GraffitiMenu.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiMenu.java,v 1.5 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.JMenu;

import org.graffiti.editor.MainFrame;

/**
 * DOCUMENT ME!
 */
public class GraffitiMenu extends JMenu implements GraffitiComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2448420434844225820L;
	
	// ~ Methods ================================================================
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	 */
	public void setMainFrame(MainFrame mf) {
	}
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return "menu";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------

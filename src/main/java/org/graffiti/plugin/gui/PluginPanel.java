// ==============================================================================
//
// PluginPanel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginPanel.java,v 1.6 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.gui;

import java.awt.Component;

/**
 * The panel to which plugins can add bigger view-like components.
 * 
 * @version $Revision: 1.6 $
 */
public class PluginPanel extends AbstractGraffitiContainer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4163512739506314629L;

	// ~ Constructors ===========================================================
	/**
	 * Creates a new PluginPanel object.
	 */
	public PluginPanel() {
		id = "pluginPanel";
		preferredComponent = "";
	}

	// ~ Methods ================================================================

	/**
	 * @see java.awt.Container#addImpl(Component, Object, int)
	 */
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		super.addImpl(comp, constraints, index);
		this.setVisible(true);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
